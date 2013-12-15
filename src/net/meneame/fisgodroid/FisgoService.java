package net.meneame.fisgodroid;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class FisgoService extends Service
{
    private static final String TAG = "FisgoService";
    private static final String LOGIN_GET_URL = "http://www.meneame.net/login.php";
    private static final String LOGIN_URL = "https://www.meneame.net/login.php";
    private static final String SNEAK_URL = "http://www.meneame.net/sneak.php";
    private static final String SNEAK_BACKEND_URL = "http://www.meneame.net/backend/sneaker2.php";
    private static final String FRIEND_LIST_URL = "http://www.meneame.net/user/?username/friends";
    private static final String UPLOAD_URL = "http://www.meneame.net/backend/tmp_upload.php";
    private static final String GET_FRIEND_URL = "http://www.meneame.net/backend/get_friend.php";
    private static final String GET_USER_INFO_URL = "http://www.meneame.net/backend/get_user_info.php";

    private FisgoBinder mBinder = new FisgoBinder();
    private boolean mIsLoggedIn = false;
    private boolean mIsAdmin = false;
    private IHttpService mHttp = new HttpService();
    private List<ChatMessage> mMessages = new LinkedList<ChatMessage>();
    private List<String> mFriendNames = new ArrayList<String>();
    private String mLastMessageTime = "";
    private String mUsername;
    private String mMyKey;
    private String mBaseKey;
    private List<String> mOutgoingMessages = new LinkedList<String>();
    private ChatType mType = ChatType.PUBLIC;
    private int mNumRequests = 0;
    private int mTimeToWait = 5000;
    private int mTimeToWaitWhenFailed = 10000;
    private int mTimeToWaitWhenOnBackground = 15000;
    private int mDelayBetweenMessages = 5000;
    private boolean mIsOnForeground = false;
    private BroadcastReceiver mConnectivityReceiver;
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private Executor mExecutor;

    @Override
    public void onCreate()
    {
        mTimeToWait = getResources().getInteger(R.integer.time_to_wait);
        mTimeToWaitWhenFailed = getResources().getInteger(R.integer.time_to_wait_when_failed);
        mTimeToWaitWhenOnBackground = getResources().getInteger(R.integer.time_to_wait_when_on_background);
        mDelayBetweenMessages = getResources().getInteger(R.integer.time_between_messages) * 1000 + 500;

        mExecutor = Executors.newSingleThreadExecutor();

        // Register a BroadcastReceiver to detect connectivity changes
        final IntentFilter connectivityIntentFilter = new IntentFilter();
        connectivityIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mConnectivityReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                wakeUp();
            }
        };
        registerReceiver(mConnectivityReceiver, connectivityIntentFilter);

        // Setup the alarm stuff
        Intent intent = new Intent(this, FisgoScheduler.class);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        reschedule(0);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if ( intent != null )
        {
            final boolean doReschedule = intent.getBooleanExtra("reschedule", false);
            if ( doReschedule )
            {
                final int delay = intent.getIntExtra("rescheduleDelay", 0);
                reschedule(delay);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public void reschedule(final int delay)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                mAlarmManager.cancel(mPendingIntent);
                if ( delay == 0 )
                {
                    doPulse();
                }
                else
                {
                    mAlarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, mPendingIntent);
                }
            }
        });
    }

    private void doPulse()
    {
        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        int delayForNextPulse = mIsOnForeground ? mTimeToWait : mTimeToWaitWhenOnBackground;

        try
        {
            if ( !mIsLoggedIn )
            {
                clearSession();
                return;
            }

            final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
            if ( !isConnected )
            {
                return;
            }

            boolean failed = false;
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            boolean containsChat = mOutgoingMessages.size() > 0;

            if ( containsChat )
            {
                Map<String, Object> params = new HashMap<String, Object>();

                params.put("k", mMyKey);
                params.put("v", 5);
                params.put("r", ++mNumRequests);
                params.put("chat", mOutgoingMessages.get(0));
                params.put("nopost", 1);
                params.put("novote", 1);
                params.put("noproblem", 1);
                params.put("nocomment", 1);
                params.put("nonew", 1);
                params.put("nopublished", 1);
                params.put("nopubvotes", 1);

                // Request the appropiate chat type
                if ( mType == ChatType.FRIENDS )
                    params.put("friends", 1);
                else if ( mType == ChatType.ADMIN )
                    params.put("admin", 1);

                if ( mLastMessageTime.equals("") == false )
                    params.put("time", mLastMessageTime);

                failed = !mHttp.post(SNEAK_BACKEND_URL, params, result);

                if ( !failed && result.size() > 0 )
                    mOutgoingMessages.remove(0);
            }
            else
            {
                // Build the request parameters
                String uri = SNEAK_BACKEND_URL + "?nopost=1&novote=1&noproblem=1&nocomment=1" + "&nonew=1&nopublished=1&nopubvotes=1&v=5&r=" + (++mNumRequests);
                // If we have previous messages, get only the
                // new ones
                if ( mLastMessageTime.equals("") == false )
                {
                    uri += "&time=" + mLastMessageTime;
                }

                // Request the appropiate chat type
                if ( mType == ChatType.FRIENDS )
                    uri += "&friends=1";
                else if ( mType == ChatType.ADMIN )
                    uri += "&admin=1";

                failed = !mHttp.get(uri, result);
            }

            // Get the response JSON value and construct the
            // chat messages from it
            if ( result.size() > 0 )
            {
                JSONObject root = new JSONObject(result.toString("UTF-8"));
                final boolean isFirstRequest = mLastMessageTime.equals("");
                mLastMessageTime = root.getString("ts");

                JSONArray events = root.getJSONArray("events");
                if ( events.length() > 0 )
                {
                    // Create a new list with the new messages
                    List<ChatMessage> newList = new LinkedList<ChatMessage>();
                    for (int i = 0; i < events.length(); ++i)
                    {
                        JSONObject event = events.getJSONObject(i);
                        String icon = event.getString("icon");
                        String title = event.getString("title");
                        int ts = event.getInt("ts");
                        String status = event.getString("status");
                        String who = event.getString("who");
                        String userid = event.getString("uid");

                        // Remove the escaped slashes from the
                        // icon path
                        icon = icon.replace("\\/", "/");

                        // Parse the date
                        Date when = new Date(ts * 1000L);

                        // Construct the message and add it to
                        // the message list
                        ChatType type = ChatType.PUBLIC;
                        if ( status.equals("amigo") )
                            type = ChatType.FRIENDS;
                        else if ( status.equals("admin") )
                            type = ChatType.ADMIN;
                        ChatMessage msg = new ChatMessage(when, who, userid, title, type, icon);
                        newList.add(0, msg);

                        // Send a notification if they mentioned us
                        final Locale locale = getResources().getConfiguration().locale;
                        String lowercaseMsg = msg.getMessage().toLowerCase(locale);
                        boolean notify = lowercaseMsg.contains(mUsername.toLowerCase(locale));
                        notify = notify || (mIsAdmin && lowercaseMsg.contains("admin"));
                        if ( !isFirstRequest && notify )
                        {
                            Notifications.theyMentionedMe(FisgoService.this, msg);
                        }
                    }

                    // Append all the previous messages
                    newList.addAll(0, mMessages);
                    mMessages = newList;

                    // Notify the handlers
                    notifyHandlers();
                }
            }

            // Make a small delay to poll again
            if ( failed )
            {
                delayForNextPulse = mTimeToWaitWhenFailed;
            }
            else if ( mOutgoingMessages.size() > 0 )
            {
                delayForNextPulse = mDelayBetweenMessages;
            }
        }

        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        reschedule(delayForNextPulse);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(mConnectivityReceiver);
    }

    private void clearSession()
    {
        Notifications.stopOnForeground(this);
        mLastMessageTime = "";
        mMessages.clear();
        mOutgoingMessages.clear();
        mFriendNames.clear();
        notifyHandlers();
    }

    private void notifyHandlers()
    {
        Message msg = new Message();
        for (Handler handler : mBinder.getHandlers())
        {
            handler.sendMessage(msg);
        }
    }

    public void wakeUp()
    {
        Log.i(TAG, "Waking up FisgoService");
        reschedule(0);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    public class FisgoBinder extends Binder
    {
        private Set<Handler> mHandlers = new HashSet<Handler>();

        private final Pattern mUseripPattern = Pattern.compile("<input type=\"hidden\" name=\"userip\" value=\"([^\"]+)\"/>");
        private final Pattern mIpcontrolPattern = Pattern.compile("<input type=\"hidden\" name=\"useripcontrol\" value=\"([^\"]+)\"/>");
        private final Pattern mAdminPattern = Pattern.compile("<a href=\"/admin/bans\\.php\">admin</a>");
        private final Pattern mMykeyPattern = Pattern.compile("var mykey = (\\d+);");
        private final Pattern mBasekeyPattern = Pattern.compile("base_key=\"([^\"]+)\"");
        private final Pattern mFriendPattern = Pattern.compile("<div class=\"friends-item\"><a href=\"\\/user\\/([^\"]+)\"");

        public void doPulse()
        {
            reschedule(0);
        }

        public boolean isAdmin()
        {
            return mIsAdmin;
        }

        public boolean isLoggedIn()
        {
            return mIsLoggedIn;
        }

        public void logOut()
        {
            mIsLoggedIn = false;
            clearSession();
            reschedule(0);
        }

        public LoginStatus logIn(String username, String password)
        {
            if ( username.equalsIgnoreCase("whizzo") )
                return LoginStatus.INVALID_PASSWORD;

            String step1 = mHttp.get(LOGIN_GET_URL);
            if ( "".equals(step1) )
                return LoginStatus.NETWORK_FAILED;

            // Get the userip field
            Matcher m = mUseripPattern.matcher(step1);
            if ( !m.find() )
                Log.e(TAG, "Couldn't find the userip form field");
            String userip = m.group(1);

            // Get the ip control field
            m = mIpcontrolPattern.matcher(step1);
            if ( !m.find() )
                Log.e(TAG, "Couldn't find the ip control form field");
            String ipcontrol = m.group(1);

            // Prepare the POST request
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("username", username);
            params.put("password", password);
            params.put("userip", userip);
            params.put("useripcontrol", ipcontrol);
            params.put("persistent", 1);
            params.put("processlogin", 1);
            params.put("return", "");
            String step2 = mHttp.post(LOGIN_URL, params);
            if ( "".equals(step2) )
                return LoginStatus.NETWORK_FAILED;

            // Did we log in correctly?
            mIsLoggedIn = true;
            mIsAdmin = false;

            if ( mIsLoggedIn )
            {
                mUsername = username;

                // Are we administrators?
                m = mAdminPattern.matcher(step2);
                mIsAdmin = m.find();

                // Get the base key value to be able to interact with the
                // backend
                m = mBasekeyPattern.matcher(step2);
                if ( m.find() )
                {
                    mBaseKey = m.group(1);
                }

                // Get the mykey value to be able to send messages
                String step3 = mHttp.get(SNEAK_URL);
                m = mMykeyPattern.matcher(step3);
                if ( m.find() )
                {
                    mMyKey = m.group(1);
                }

                // Get the friend list
                String friendsUrl = FRIEND_LIST_URL.replace("?username", mUsername);
                String friendList = mHttp.get(friendsUrl);
                if ( friendList.equals("") == false )
                {
                    m = mFriendPattern.matcher(friendList);
                    while (m.find())
                    {
                        mFriendNames.add(m.group(1));
                    }
                }

                // Start this service on foreground
                Notifications.startOnForeground(FisgoService.this);
            }

            reschedule(0);

            return mIsLoggedIn ? LoginStatus.OK : LoginStatus.INVALID_PASSWORD;
        }

        public String getUsername()
        {
            return mUsername;
        }

        public List<ChatMessage> getMessages()
        {
            return mMessages;
        }

        public List<String> getFriendNames()
        {
            return mFriendNames;
        }

        public Set<Handler> getHandlers()
        {
            return mHandlers;
        }

        public void addHandler(Handler handler)
        {
            mHandlers.add(handler);
            handler.dispatchMessage(new Message());
        }

        public void removeHandler(Handler handler)
        {
            mHandlers.remove(handler);
        }

        public void sendChat(String msg)
        {
            if ( mIsLoggedIn )
            {
                mOutgoingMessages.add(msg);
                reschedule(0);
            }
        }

        public void setType(ChatType type)
        {
            if ( type != mType )
            {
                // Set the new chat type, and reset all the message lists
                mType = type;
                clearSession();
                reschedule(0);
            }
        }

        public String sendPicture(InputStream data, IHttpService.ProgressUpdater progressUpdater)
        {
            String url = null;

            String result = mHttp.postData(UPLOAD_URL, data, progressUpdater);
            if ( result.equals("") == false )
            {
                JSONObject root;
                try
                {
                    root = new JSONObject(result);
                    if ( root.has("url") )
                    {
                        url = root.getString("url").replace("\\/", "/");
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }

            return url;
        }

        public void setOnForeground(boolean isOnForeground)
        {
            Log.i(TAG, "Setting FisgoService on " + (isOnForeground ? "foreground" : "background"));
            mIsOnForeground = isOnForeground;
        }

        public FriendshipStatus swapFriendship(String userid)
        {
            FriendshipStatus status = FriendshipStatus.NONE;
            Pattern pattern = Pattern.compile("title=\"([^\"]+)\"");

            String url = GET_FRIEND_URL + "?id=" + userid + "&key=" + mBaseKey + "&type=99674";
            String response = mHttp.get(url);

            Matcher m = pattern.matcher(response);
            if ( m.find() )
            {
                String statusName = m.group(1);
                status = FriendshipStatus.fromName(statusName);
            }

            return status;
        }

        public String getUserInfo(String userid)
        {
            String url = GET_USER_INFO_URL + "?id=" + userid;
            return mHttp.get(url);
        }
    }
}
