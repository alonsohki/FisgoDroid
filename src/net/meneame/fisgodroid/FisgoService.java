package net.meneame.fisgodroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FisgoService extends Service
{
	private IBinder mBinder = new FisgoBinder ();
	private Thread mThread = null;
	private boolean mIsLoggedIn = false;
	
	@Override
	public void onCreate ()
	{
		mThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				synchronized (this)
				{
					while ( true ) 
					{
						Log.i("FisgoService", "Thread tick");
						try {
							wait(2000);
						}
						catch (InterruptedException e) {
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
		public boolean isLoggedIn ()
		{
			return mIsLoggedIn;
		}
		
		public boolean logIn ( String username, String password )
		{
			return mIsLoggedIn;
		}
	}
}
