package net.meneame.fisgodroid;

import android.app.Application;
import android.content.pm.ApplicationInfo;

import com.bugsense.trace.BugSenseHandler;

public class FisgodroidApplication extends Application
{
    @Override
    public void onCreate()
    {
        if ( 0 == (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) )
        {
            BugSenseHandler.initAndStartSession(this, getResources().getString(R.string.bugsense_api_key));
        }
        super.onCreate();
    }
}
