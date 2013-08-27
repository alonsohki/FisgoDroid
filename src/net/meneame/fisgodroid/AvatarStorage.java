package net.meneame.fisgodroid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;

public class AvatarStorage
{
    @SuppressWarnings("unused")
    private Context mContext;
    private final Handler mHandler = new Handler();
    
    private Map<String, Drawable> mDrawables = new HashMap<String, Drawable>();
    private Map<String, Thread> mDownloadTasks = new HashMap<String, Thread>();
    private Map<String, List<Runnable>> mCallbacks = new HashMap<String, List<Runnable>>();
    
    public AvatarStorage ( Context context )
    {
        mContext = context;
    }
    
    public Drawable getAvatar ( String path )
    {
        if ( mDrawables.containsKey(path) )
            return mDrawables.get(path);
        return null;
    }
    
    public Drawable request ( final String path, Runnable callback )
    {
        // Do we already have this bitmap?
        if ( mDrawables.containsKey(path) )
        {
            callback.run();
            return mDrawables.get(path);
        }
        
        // Start a new async task to download it only if
        // there is not an already ongoing one.
        Thread thread;
        if ( mDownloadTasks.containsKey(path) == false )
        {
            thread = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    IHttpService service = new HttpService();
                    ByteArrayOutputStream imageData = new ByteArrayOutputStream();
                    if ( service.get(path, imageData) && imageData.size() > 0 )
                    {
                        InputStream is = new ByteArrayInputStream(imageData.toByteArray());
                        Drawable drawable = Drawable.createFromStream(is, path);
                        if ( drawable != null )
                            mDrawables.put(path, drawable);
                    }
                    
                    // Call all the callbacks
                    for ( Runnable cbk : mCallbacks.get(path) )
                        mHandler.post(cbk);
                    
                    // Cleanup async control data
                    mDownloadTasks.remove(path);
                    mCallbacks.remove(path);
                }
            });
            mDownloadTasks.put(path, thread);
        }
        else
        {
            thread = mDownloadTasks.get(path);
        }
        
        // Add the callback to the callback list
        List<Runnable> callbacks;
        if ( mCallbacks.containsKey(path) )
        {
            callbacks = mCallbacks.get(path);
        }
        else
        {
            callbacks = new ArrayList<Runnable>();
            mCallbacks.put(path, callbacks);
        }
        callbacks.add(callback);
        
        // Start the async thread if it's not already started
        if ( thread.isAlive() == false )
            thread.start();
        
        return null;
    }
}
