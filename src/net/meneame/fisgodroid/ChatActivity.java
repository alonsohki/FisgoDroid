package net.meneame.fisgodroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ChatActivity extends Activity
{   
    // Request codes for activities
    private static final int REQUEST_PICTURE = 1;
    
    // Shared preferences settings.
    private static final String PREFS_NAME = "ChatActivity";
    private static final String PREF_SENDAS = "send as";
    
    
    private CheckBox mCheckboxFriends;
    private ListView mMessages;
    private EditText mMessagebox;
    private ImageButton mSendButton;
    private ImageButton mCameraButton;
    private ProgressBar mCameraProgress;
    private Spinner mChatSpinner;
    private ChatType mType = ChatType.PUBLIC;
    private ChatType mSendAs = ChatType.PUBLIC;
    private ChatMessageAdapter mAdapter;
    private Date mLastMessage = null;
    private File mCameraTempFile = null;
    
    
    // Create a handler to update the view from the UI thread
    // when the message list changes.
    private Handler mHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
            updateMessages(mFisgoBinder.getMessages());
        }
    };

    // Reference to the service binder
    private FisgoService.FisgoBinder mFisgoBinder = null;
    private ServiceConnection mServiceConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder binder)
        {
            mFisgoBinder = (FisgoService.FisgoBinder) binder;

            // If the service is not logged in, we should go back to the login
            // screen.
            if ( mFisgoBinder.isLoggedIn() == false )
            {
                finish();
                startActivity(new Intent(ChatActivity.this, LoginActivity.class));
            }
            else
            {
                mFisgoBinder.setType(mType);
                mAdapter = new ChatMessageAdapter(ChatActivity.this, mFisgoBinder.getAvatarStorage());
                mMessages.setAdapter(mAdapter);
                mFisgoBinder.addHandler(mHandler);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get views
        mCheckboxFriends = (CheckBox) findViewById(R.id.checkbox_friends);
        mMessages = (ListView) findViewById(R.id.chat_messages);
        mMessagebox = (EditText) findViewById(R.id.chat_messagebox);
        mSendButton = (ImageButton) findViewById(R.id.button_send);
        mCameraButton = (ImageButton) findViewById(R.id.camera_button);
        mCameraProgress = (ProgressBar) findViewById(R.id.camera_progress);
        mChatSpinner = (Spinner) findViewById(R.id.chat_spinner);
        
        // Restore stuff from shared prefs
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int optionOrdinal = prefs.getInt(PREF_SENDAS, ChatType.PUBLIC.ordinal());
        if ( optionOrdinal >= 0 && optionOrdinal < ChatType.values().length )
            mSendAs = ChatType.values()[optionOrdinal];

        // Setup
        setType(mType);
        setSendAs(mSendAs);

        // Handle key presses for the nick completion feature
        mMessagebox.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                String str = s.toString();
                if ( count == 1 && str.charAt(start) == '\t' )
                {
                    // Remove the tab from the string
                    str = str.substring(0, start) + str.substring(start + 1);

                    doNickCompletion(str, start);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
            }

            @Override
            public void afterTextChanged(Editable s)
            {
            }
        });

        // Set the different types of chat options
        mChatSpinner.setAdapter(new ArrayAdapter<String>(this, R.layout.chat_spinner_item)
        {
            @Override
            public int getCount()
            {
                return ChatType.values().length;
            }

            @Override
            public String getItem(int position)
            {
                int stringId = -1;
                switch (ChatType.values()[position])
                {
                case PUBLIC:
                    stringId = R.string.general;
                    break;
                case FRIENDS:
                    stringId = R.string.friends;
                    break;
                }

                if ( stringId != -1 )
                    return getResources().getString(stringId);
                return "";
            }
        });
        mChatSpinner.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long arg3)
            {
                setType(ChatType.values()[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
                setType(ChatType.PUBLIC);
            }
        });

        // Connect with the chat service
        Intent intent = new Intent(this, FisgoService.class);
        startService(intent);
        bindService(intent, mServiceConn, BIND_AUTO_CREATE);

        // Make pressing enter in the message box send the chat
        mMessagebox.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if ( id == EditorInfo.IME_NULL )
                {
                    if ( keyEvent.getAction() == KeyEvent.ACTION_UP )
                        sendChat();
                    return true;
                }

                return false;
            }
        });

        // Also send messages with the send button
        mSendButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendChat();
            }
        });
        
        
        // Send an intent to pick an image when they tap the camera button
        mCameraButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takePicture ();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        // Save the shared prefs
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        .edit()
        .putInt(PREF_SENDAS, getSendAs().ordinal())
        .commit();
        
        super.onDestroy();
        mFisgoBinder.removeHandler(mHandler);
        unbindService(mServiceConn);
    }
    
    @Override
    protected void onPause ()
    {
        super.onPause();
        Notifications.setOnForeground(getApplicationContext(), false);
    }
    
    @Override
    protected void onResume ()
    {
        super.onResume();
        Notifications.setOnForeground(getApplicationContext(), true);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if ( resultCode != Activity.RESULT_OK )
            return;
        
        if ( requestCode == REQUEST_PICTURE )
        {
            if ( data != null )
            {
                final String action = data.getAction();
                if ( action != null && action.equals("inline-data") )
                {
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    processTakenPicture(bitmap);
                }
                else
                {
                    try
                    {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                        processTakenPicture(bitmap);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else if ( mCameraTempFile != null )
            {
                try
                {
                    Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(mCameraTempFile));
                    mCameraTempFile.delete();
                    mCameraTempFile = null;
                    processTakenPicture(bitmap);
                }
                catch (FileNotFoundException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat()
    {
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
        {
            return getActionBar().getThemedContext();
        }
        else
        {
            return this;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        if ( savedInstanceState.containsKey("send as") )
        {
            mSendAs = (ChatType) savedInstanceState.getSerializable("send as");
            setSendAs(mSendAs);
        }
        if ( savedInstanceState.containsKey("type") )
        {
            mType = (ChatType) savedInstanceState.getSerializable("type");
            setType(mType);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // Serialize the current dropdown position.
        outState.putSerializable("type", mType);
        outState.putSerializable("send as", getSendAs());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
        case R.id.action_logout:
            mFisgoBinder.logOut();
            stopService(new Intent(this, FisgoService.class));
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }

        return false;
    }

    private void sendChat()
    {
        String text = mMessagebox.getText().toString();
        if ( text.length() > 0 )
        {
            Date now = new Date();
            Resources res = getResources();
            int delayBetweenMessages = res.getInteger(R.integer.time_between_messages);

            // Check for too small messages
            if ( text.length() < res.getInteger(R.integer.min_message_length) )
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error).setMessage(R.string.message_too_short).setIcon(android.R.drawable.ic_dialog_alert).setNeutralButton(android.R.string.ok, null).create().show();
            }
            else if ( mLastMessage != null && (now.getTime() - mLastMessage.getTime()) < (delayBetweenMessages * 1000) )
            {
                String errMsg = String.format(res.getString(R.string.message_too_soon), delayBetweenMessages);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.error)
                       .setMessage(errMsg)
                       .setIcon(android.R.drawable.ic_dialog_alert)
                       .setNeutralButton(android.R.string.ok, null).create().show();
            }
            else
            {
                // If it's a friends chat, prefix the message with '@'
                if ( mType == ChatType.FRIENDS || getSendAs() == ChatType.FRIENDS )
                    text = "@" + text;

                mFisgoBinder.sendChat(text);
                mMessagebox.setText("");
                mLastMessage = now;
            }
        }
    }

    public ChatType getSendAs()
    {
        mSendAs = mCheckboxFriends.isChecked() ? ChatType.FRIENDS : ChatType.PUBLIC;
        return mSendAs;
    }

    public void setSendAs(ChatType type)
    {
        mSendAs = type;
        if ( mCheckboxFriends != null )
            mCheckboxFriends.setChecked(type == ChatType.FRIENDS);
    }

    public void setType(ChatType type)
    {
        // This might seem a weird way to do this, but this way
        // we avoid an infinite recursion from setSelection
        // calling the spinner handler, and calling this function
        // back.
        if ( type != mType && mChatSpinner != null )
        {
            mType = type;
            mChatSpinner.setSelection(type.ordinal(), false);
        }

        mType = type;

        if ( mCheckboxFriends != null )
            mCheckboxFriends.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);

        if ( mFisgoBinder != null )
            mFisgoBinder.setType(mType);
    }

    public void updateMessages(List<ChatMessage> messages)
    {
        if ( mAdapter != null )
        {
            mAdapter.setUsername(mFisgoBinder.getUsername());
            mAdapter.setMessages(messages);
        }
    }

    private void doNickCompletion(String str, int start)
    {
        // We will need to restore the cursor position after replacements
        int cursorPos = start;

        if ( str.length() > 0 )
        {
            int wordBegin = str.lastIndexOf(' ', Math.max(0, start - 1));
            wordBegin = Math.max(0, wordBegin);
            if ( str.charAt(wordBegin) == ' ' )
                ++wordBegin;

            int wordEnd = str.indexOf(' ', wordBegin);
            if ( wordEnd == -1 )
                wordEnd = str.length();

            // Get the partial name from the detected word
            String partialName = str.substring(wordBegin, wordEnd);

            // Perform nick completion if we at least got two characters of the
            // name
            if ( (wordEnd - wordBegin) >= 2 )
            {
                // Search a matching nickname in the last 15 minutes messages,
                // using
                // at least 10 messages.
                Date now = new Date();
                long timeThreshold = now.getTime() - 15 * 60 * 1000;

                String nameReplacement = null;
                partialName = partialName.toLowerCase();
                int messageCount = 0;
                for (ChatMessage msg : mFisgoBinder.getMessages())
                {
                    ++messageCount;
                    if ( messageCount > 10 && msg.getWhen().getTime() < timeThreshold )
                        break;

                    if ( msg.getUser().toLowerCase().startsWith(partialName) )
                    {
                        nameReplacement = msg.getUser();
                        break;
                    }
                }

                // If we didn't find anything in the last messages, search in
                // the
                // friend names.
                if ( nameReplacement == null )
                {
                    for (String friendName : mFisgoBinder.getFriendNames())
                    {
                        if ( friendName.toLowerCase().startsWith(partialName) )
                        {
                            nameReplacement = friendName;
                            break;
                        }
                    }
                }

                // Replace it!
                if ( nameReplacement != null )
                {
                    str = str.substring(0, wordBegin) + nameReplacement + str.substring(wordEnd);
                    cursorPos = wordBegin + nameReplacement.length();

                    // If we were at the end of the string, add an extra space.
                    if ( cursorPos == str.length() )
                    {
                        str = str + " ";
                        cursorPos++;
                    }
                }
            }
        }

        mMessagebox.setText(str);
        mMessagebox.setSelection(cursorPos);
    }
    
    private void takePicture ()
    {
        // Create a temporary file for in case they decide to use the camera
        File cacheDir = getExternalCacheDir();
        mCameraTempFile = null;
        try
        {
            mCameraTempFile = File.createTempFile("fisgodroid", "capture.jpg", cacheDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        // http://stackoverflow.com/questions/4455558/allow-user-to-select-camera-or-gallery-for-image
        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        if ( mCameraTempFile != null )
            captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraTempFile));

        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for(ResolveInfo res : listCam)
        {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(packageName);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        
        startActivityForResult(chooserIntent, REQUEST_PICTURE);
    }
    
    private void processTakenPicture ( final Bitmap bitmap )
    {
        // Hide the camera button and display a progress bar
        mCameraButton.setVisibility(View.GONE);
        mCameraProgress.setVisibility(View.VISIBLE);
        
        // Create a handler for when the thread finishes
        final Handler handler = new Handler ()
        {
            @Override
            public void handleMessage(Message msg)
            {
                // Restore the camera button
                mCameraButton.setVisibility(View.VISIBLE);
                mCameraProgress.setVisibility(View.GONE);
                
                // Did everything go ok?
                String pictureUrl = (String)msg.obj;
                if ( pictureUrl != null )
                {
                    mMessagebox.getText().append(" " + pictureUrl + " ");
                }
            }
        };
        
        Thread thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Bitmap bmp = bitmap;
                
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress(CompressFormat.JPEG, 90, stream);
                
                // Send a message to the handler with the picture url
                Message msg = new Message();
                msg.obj = mFisgoBinder.sendPicture(new ByteArrayInputStream(stream.toByteArray()));
                handler.sendMessage(msg);
            }
        });
        thread.start();
    }
}
