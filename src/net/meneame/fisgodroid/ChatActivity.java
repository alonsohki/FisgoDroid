package net.meneame.fisgodroid;

import java.util.Date;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ChatActivity extends FragmentActivity implements ActionBar.OnNavigationListener
{
    private ChatFragment mFragment = null;
    private ChatType mSendAsType = ChatType.PUBLIC;

    /**
     * The serialization (saved instance state) Bundle key representing the
     * current dropdown position.
     */
    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Set up the action bar to show a dropdown list.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
        // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter<String>(getActionBarThemedContextCompat(), android.R.layout.simple_list_item_1, android.R.id.text1, new String[] {
                        getString(R.string.title_general), getString(R.string.title_friends), }), this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        mFragment = null;
    }

    /**
     * Backward-compatible version of {@link ActionBar#getThemedContext()} that
     * simply returns the {@link android.app.Activity} if
     * <code>getThemedContext</code> is unavailable.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Context getActionBarThemedContextCompat()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            return getActionBar().getThemedContext();
        } else
        {
            return this;
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        // Restore the previously serialized current dropdown position.
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM))
        {
            getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
            if ( savedInstanceState.containsKey("send as") )
            {
                mSendAsType = (ChatType)savedInstanceState.getSerializable("send as");
                if ( mFragment != null )
                {
                    mFragment.setSendAs(mSendAsType);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        // Serialize the current dropdown position.
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
        if ( mFragment != null )
            outState.putSerializable("send as", mFragment.getSendAs());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(int position, long id)
    {
        // When the given dropdown item is selected, show its contents in the
        // container view.
        if (mFragment == null)
        {
            mFragment = new ChatFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
        }
        mFragment.setType(position == 0 ? ChatType.PUBLIC : ChatType.FRIENDS);
        mFragment.setSendAs(mSendAsType);
        return true;
    }

    /**
     * A dummy fragment representing a section of the app, but that simply
     * displays dummy text.
     */
    public static class ChatFragment extends Fragment
    {
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
                mFisgoBinder.addHandler(mHandler);
                updateMessages(mFisgoBinder.getMessages());
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0)
            {
            }
        };

        private RadioGroup mRadioGroup;
        private ListView mMessages;
        private EditText mMessagebox;
        private Button mSendButton;
        private ChatType mType;
        private ChatType mSendAs;
        private ChatMessageAdapter mAdapter;
        private AvatarStorage mAvatarStorage;
        private Date mLastMessage = null;

        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        public static final String ARG_SECTION_NUMBER = "section_number";

        public ChatFragment()
        {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View rootView = inflater.inflate(R.layout.chat_layout, container, false);

            // Get views
            mRadioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group_chattype);
            mMessages = (ListView) rootView.findViewById(R.id.chat_messages);
            mMessagebox = (EditText) rootView.findViewById(R.id.messagebox);
            mSendButton = (Button) rootView.findViewById(R.id.button_send);

            // Setup
            mAvatarStorage = new AvatarStorage(getActivity());
            mAdapter = new ChatMessageAdapter(getActivity(), mAvatarStorage);
            mMessages.setAdapter(mAdapter);
            setType(mType);
            setSendAs(mSendAs);

            // Connect with the chat service
            Intent intent = new Intent(getActivity(), FisgoService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, mServiceConn, BIND_AUTO_CREATE);

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

            return rootView;
        }

        private void sendChat ()
        {
            String text = mMessagebox.getText().toString();
            if ( text.length() > 0 )
            {
                Date now = new Date();
                Resources res = getActivity().getResources();
                int delayBetweenMessages = res.getInteger(R.integer.time_between_messages);
                
                // Check for too small messages
                if ( text.length() < res.getInteger(R.integer.min_message_length) )
                {   
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error)
                           .setMessage(R.string.message_too_short)
                           .setIcon(android.R.drawable.ic_dialog_alert)
                           .setNeutralButton(android.R.string.ok, null)
                           .create().show();
                }
                else if ( mLastMessage != null && (now.getTime() - mLastMessage.getTime()) < (delayBetweenMessages*1000) )
                {
                    String errMsg = String.format(res.getString(R.string.message_too_soon), delayBetweenMessages);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.error)
                           .setMessage(errMsg)
                           .setIcon(android.R.drawable.ic_dialog_alert)
                           .setNeutralButton(android.R.string.ok, null)
                           .create().show();
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

        @Override
        public void onDestroyView()
        {
            mFisgoBinder.removeHandler(mHandler);
            getActivity().unbindService(mServiceConn);
            super.onDestroyView();
        }
        
        public ChatType getSendAs ()
        {
            return mRadioGroup.getCheckedRadioButtonId() == R.id.radio_friends ? ChatType.FRIENDS : ChatType.PUBLIC;
        }
        
        public void setSendAs ( ChatType type )
        {
            mSendAs = type;
            if ( mRadioGroup != null )
                mRadioGroup.check( type == ChatType.FRIENDS ? R.id.radio_friends : R.id.radio_general );
        }

        public void setType(ChatType type)
        {
            mType = type;

            if (mRadioGroup != null)
                mRadioGroup.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);

            if (mAdapter != null)
                mAdapter.setType(mType);
        }

        public void updateMessages(List<ChatMessage> messages)
        {
            mAdapter.setUsername(mFisgoBinder.getUsername());
            mAdapter.setMessages(messages);
        }
    }

}
