package net.meneame.fisgodroid.notifications;

import java.util.Map;
import java.util.Map.Entry;

import net.meneame.fisgodroid.FisgoService;

public class NotificationsPoller
{
    public interface Listener
    {
        public void onNotificationsUpdate(String type, int count);
    }

    private Poller mPoller;
    private Listener mListener;

    public NotificationsPoller()
    {
    }

    public void setListener(Listener listener)
    {
        mListener = listener;
    }

    public void stop()
    {
        if ( mPoller != null )
        {
            mPoller.cancel();
            mPoller = null;
        }
    }

    public void start(FisgoService.FisgoBinder service, int interval)
    {
        mPoller = new Poller(service, interval);
        new Thread(mPoller).start();
    }

    private class Poller implements Runnable
    {
        private boolean mCancelled = false;
        private FisgoService.FisgoBinder mService;
        private int mInterval;

        public Poller(FisgoService.FisgoBinder service, int interval)
        {
            mService = service;
            mInterval = interval;
        }

        @Override
        public void run()
        {
            while (!mCancelled)
            {
                try
                {
                    if ( mListener != null )
                    {
                        Map<String, Integer> notifications = mService.getNotifications();
                        for (Entry<String, Integer> entry : notifications.entrySet())
                        {
                            mListener.onNotificationsUpdate(entry.getKey(), entry.getValue());
                        }
                    }
                    Thread.sleep(mInterval);
                }
                catch (InterruptedException e)
                {
                }
            }
        }

        public void cancel()
        {
            mCancelled = true;
        }
    }
}
