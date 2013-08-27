package net.meneame.fisgodroid;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Movie;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

public class AnimatedGIFDrawable extends Drawable
{
    private Movie mMovie = null;
    private Context mContext;
    private boolean mRunning = false;
    private int mElapsedTime = 0;
    private long mLastUpdate = 0L;
    private Bitmap mBitmap = null;
    private Canvas mCanvas;
    private Rect mRect;
    
    public AnimatedGIFDrawable ( Context context, int resourceId )
    {
        mContext = context;
        Resources res = mContext.getResources();
        mMovie = Movie.decodeStream(res.openRawResource(resourceId));
        if ( mMovie != null )
        {
            mRect = new Rect(0, 0, mMovie.width(), mMovie.height());
            mBitmap = Bitmap.createBitmap(getIntrinsicWidth(), getIntrinsicHeight(), Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        else
        {
            mRect = new Rect(0, 0, 0, 0);
        }
    }
    
    public void start ()
    {
        if ( mMovie != null && mMovie.duration() > 0 )
        {
            mRunning = true;
            mLastUpdate = SystemClock.uptimeMillis();
        }
    }
    
    public void pause ()
    {
        mRunning = false;
    }
    
    public void stop ()
    {
        mRunning = false;
        mElapsedTime = 0;
    }
    
    @Override
    public int getIntrinsicWidth ()
    {
        return mRect.width();
    }

    @Override
    public int getIntrinsicHeight ()
    {
        return mRect.height();
    }

    @Override
    public void draw(Canvas canvas)
    {
        if ( mRunning )
        {
            long now = SystemClock.uptimeMillis();
            mElapsedTime += now - mLastUpdate;
            mLastUpdate = now;
            
            // Loop
            int movieDuration = mMovie.duration();
            if ( movieDuration > 0 && mElapsedTime >= movieDuration )
            {
                mElapsedTime = mElapsedTime % movieDuration;
            }
            
            mMovie.setTime(mElapsedTime);
            invalidateSelf();
        }
        
        if ( mBitmap != null )
        {
            mMovie.draw(mCanvas, 0, 0);
            canvas.drawBitmap(mBitmap, mRect, getBounds(), null );
        }
    }

    @Override
    public int getOpacity()
    {
        if ( mMovie == null )
            return PixelFormat.UNKNOWN;
        return mMovie.isOpaque() ? PixelFormat.OPAQUE : PixelFormat.TRANSPARENT;
    }

    @Override
    public void setAlpha(int alpha)
    {
    }

    @Override
    public void setColorFilter(ColorFilter cf)
    {
    }
}
