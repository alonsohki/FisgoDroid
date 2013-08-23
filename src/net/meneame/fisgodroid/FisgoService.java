package net.meneame.fisgodroid;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FisgoService extends Service
{
    private static final String TAG = "FisgoService";

    private IBinder mBinder = new FisgoBinder();
    private Thread mThread = null;
    private boolean mIsLoggedIn = false;
    private IHttpService mHttp = new HttpService();

    @Override
    public void onCreate()
    {
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
                            wait(2000);
                        } catch (InterruptedException e)
                        {
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
        private final Pattern mUseripPattern = Pattern.compile("<input type=\"hidden\" name=\"userip\" value=\"([^\"]+)\"/>");
        private final Pattern mIpcontrolPattern = Pattern.compile("<input type=\"hidden\" name=\"useripcontrol\" value=\"([^\"]+)\"/>");
        private final Pattern mLogoutPattern = Pattern.compile("<a href=\"/login\\.php\\?op=logout");
        
        public boolean isLoggedIn()
        {
            return mIsLoggedIn;
        }

        public boolean logIn(String username, String password)
        {
            String step1 = mHttp.get("https://www.meneame.net/login.php");
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
            String step2 = mHttp.post("https://www.meneame.net/login.php", params);
            if ( "".equals(step2) )
                return false;
            
            // Did we log in correctly?
            m = mLogoutPattern.matcher(step2);
            mIsLoggedIn = m.find();
            
            return mIsLoggedIn;
        }
    }
}
