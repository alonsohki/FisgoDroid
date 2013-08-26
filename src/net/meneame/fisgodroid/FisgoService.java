package net.meneame.fisgodroid;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Intent;
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

    private FisgoBinder mBinder = new FisgoBinder();
    private Thread mThread = null;
    private boolean mIsLoggedIn = false;
    private IHttpService mHttp = new HttpService();
    private List<ChatMessage> mMessages = new ArrayList<ChatMessage>();
    private double mLastMessageTime = 0.0;
    private String mUsername;
    private String mMyKey;
    private List<String> mOutgoingMessages = new LinkedList<String>();
    private AvatarStorage mAvatars;

    @Override
    public void onCreate()
    {
        mAvatars = new AvatarStorage(getApplicationContext());
        
        mThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                synchronized (this)
                {
                    while (true)
                    {
                        try
                        {
                            if ( !mIsLoggedIn )
                            {
                                mMessages.clear();
                                mLastMessageTime = 0.0;
                                wait();
                            }

                            String result;
                            boolean failed = false;
                            boolean containsChat = mOutgoingMessages.size() > 0;
                            
                            if ( containsChat )
                            {
                                Map<String, Object> params = new HashMap<String, Object>();
                                
                                params.put("k", mMyKey);
                                params.put("chat", mOutgoingMessages.get(0));
                                params.put("time", new DecimalFormat("0.00").format(mLastMessageTime));
                                params.put("nopost", 1);
                                params.put("novote", 1);
                                params.put("noproblem", 1);
                                params.put("nocomment", 1);
                                params.put("nonew", 1);
                                params.put("nopublished", 1);
                                params.put("nopubvotes", 1);
                                if ( mLastMessageTime > 0.0 )
                                    params.put("time", new DecimalFormat("0.00").format(mLastMessageTime));
                                result = mHttp.post(SNEAK_BACKEND_URL, params);
                                
                                if ( result != "" )
                                    mOutgoingMessages.remove(0);
                                else
                                    failed = true;
                            }
                            else
                            {
                                // Build the request parameters
                                String uri = SNEAK_BACKEND_URL + "?nopost=1&novote=1&noproblem=1&nocomment=1&nonew=1&nopublished=1&nopubvotes=1";
                                // If we have previous messages, get only the new ones
                                if ( mLastMessageTime > 0.0 )
                                {
                                    DecimalFormat df = new DecimalFormat("0.00");
                                    uri += "&time=" + df.format(mLastMessageTime);
                                }
                                
                                result = mHttp.get(uri);
                            }
                            
                            // Get the response JSON value and construct the chat messages from it
                            if ( result.equals("") )
                            {
                                failed = true;
                            }
                            else
                            {
                                JSONObject root = new JSONObject(result);
                                mLastMessageTime = root.getDouble("ts");
                                
                                JSONArray events = root.getJSONArray("events");
                                if ( events.length() > 0 )
                                {
                                    // Create a new list with the new messages
                                    List<ChatMessage> newList = new ArrayList<ChatMessage>();
                                    for ( int i = 0; i < events.length(); ++i )
                                    {
                                        JSONObject event = events.getJSONObject(i);
                                        String icon = event.getString("icon");
                                        String title = event.getString("title");
                                        int ts = event.getInt("ts");
                                        String status = event.getString("status");
                                        String who = event.getString("who");
                                        
                                        // Remove the escaped slashes from the icon path
                                        icon = icon.replace("\\/", "/");
                                        
                                        // Parse the date
                                        Date when = new Date(ts * 1000L);
                                        
                                        // Construct the message and add it to the message list
                                        ChatType type = ( status.equals("amigo") ? ChatType.FRIENDS : ChatType.PUBLIC );
                                        ChatMessage msg = new ChatMessage(when, who, title, type, icon);
                                        newList.add(msg);
                                    }
                                    
                                    // Append all the previous messages
                                    newList.addAll(mMessages);
                                    mMessages = newList;

                                    // Notify the handlers
                                    for ( Handler handler : mBinder.getHandlers() )
                                    {
                                        handler.sendMessage(new Message());
                                    }
                                }
                            }
                            
                            if ( !failed && mOutgoingMessages.size() == 0 )
                                wait(5000);
                        }
                        catch (InterruptedException e)
                        {
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mThread.start();
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
        private final Pattern mLogoutPattern = Pattern.compile("<a href=\"/login\\.php\\?op=logout");
        private final Pattern mMykeyPattern = Pattern.compile("var mykey = (\\d+);");
        
        public boolean isLoggedIn()
        {
            return mIsLoggedIn;
        }

        public boolean logIn(String username, String password)
        {
            String step1 = mHttp.get(LOGIN_GET_URL);
            if ( "".equals(step1) )
                return false;
            
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
                return false;
            
            // Did we log in correctly?
            m = mLogoutPattern.matcher(step2);
            mIsLoggedIn = m.find();
            
            if ( mIsLoggedIn )
            {
                mUsername = username;
                
                // Get the mykey value to be able to send messages
                String step3 = mHttp.get(SNEAK_URL);
                m = mMykeyPattern.matcher(step3);
                if ( m.find() )
                {
                    mMyKey = m.group(1);
                }
            }

            mThread.interrupt();
            
            
            return mIsLoggedIn;
        }
        
        public String getUsername ()
        {
            return mUsername;
        }
        
        public List<ChatMessage> getMessages ()
        {
            return mMessages;
        }
        
        public Set<Handler> getHandlers ()
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
        
        public void sendChat ( String msg )
        {
            if ( mIsLoggedIn )
            {
                mOutgoingMessages.add(msg);
                mThread.interrupt();
            }
        }
        
        public AvatarStorage getAvatarStorage ()
        {
            return mAvatars;
        }
    }
}
