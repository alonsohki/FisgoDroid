package net.meneame.fisgodroid;

import java.io.InputStream;

import jp.tomorrowkey.android.GifDecoder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AnimatedGifDrawable extends AnimationDrawable
{
    private int mCurrentIndex = 0;

    public AnimatedGifDrawable(Context context, InputStream source, float scale)
    {
        GifDecoder decoder = new GifDecoder();
        decoder.read(source);

        // Iterate through the gif frames, add each as animation frame
        for (int i = 0; i < decoder.getFrameCount(); i++)
        {
            Bitmap bitmap = decoder.getFrame(i);
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            // Explicitly set the bounds in order for the frames to display
            drawable.setBounds(0, 0, (int)(bitmap.getWidth()*scale), (int)(bitmap.getHeight() * scale));
            addFrame(drawable, decoder.getDelay(i));
            if ( i == 0 )
            {
                // Also set the bounds for this container drawable
                setBounds(0, 0, (int)(bitmap.getWidth()*scale), (int)(bitmap.getHeight() * scale));
            }
        }
    }

    /**
     * Naive method to proceed to next frame. Also notifies listener.
     */
    public void nextFrame()
    {
        mCurrentIndex = (mCurrentIndex + 1) % getNumberOfFrames();
    }

    /**
     * Return display duration for current frame
     */
    public int getFrameDuration()
    {
        return getDuration(mCurrentIndex);
    }

    /**
     * Return drawable for current frame
     */
    public Drawable getDrawable()
    {
        return getFrame(mCurrentIndex);
    }

}
