package net.meneame.fisgodroid;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.style.DynamicDrawableSpan;

class SmileySpan extends DynamicDrawableSpan
{
    public interface Listener
    {
        public void update();
    }

    private AnimatedGifDrawable mDrawable;
    private Listener mListener;
    private Handler mHandler = new Handler();

    public SmileySpan(Listener listener)
    {
        super();
        mListener = listener;
    }

    public SmileySpan(Listener listener, AnimatedGifDrawable d)
    {
        super();

        setDrawable(d);
        mListener = listener;
    }

    public void setDrawable(AnimatedGifDrawable d)
    {
        mDrawable = (AnimatedGifDrawable) d;

        if ( mDrawable.getNumberOfFrames() < 2 )
        {
            mDrawable.nextFrame();
        }
        else
        {
            // Use handler for 'ticks' to proceed to next frame
            mHandler.post(new Runnable()
            {
                public void run()
                {
                    if ( mListener != null )
                    {
                        mListener.update();
                    }

                    mDrawable.nextFrame();
                    // Set next with a delay depending on the duration for this
                    // frame
                    mHandler.postDelayed(this, mDrawable.getFrameDuration());
                }
            });
        }
    }

    /*
     * Return current frame from animated drawable. Also acts as replacement for
     * super.getCachedDrawable(), since we can't cache the 'image' of an
     * animated image.
     */
    @Override
    public Drawable getDrawable()
    {
        return mDrawable.getDrawable();
    }

    /*
     * Copy-paste of super.getSize(...) but use getDrawable() to get the
     * image/frame to calculate the size, in stead of the cached drawable.
     */
    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm)
    {
        Drawable d = getDrawable();
        Rect rect = d.getBounds();

        if ( fm != null )
        {
            fm.ascent = -rect.bottom;
            fm.descent = 0;

            fm.top = fm.ascent;
            fm.bottom = 0;
        }

        return rect.right;
    }

    /*
     * Copy-paste of super.draw(...) but use getDrawable() to get the
     * image/frame to draw, in stead of the cached drawable.
     */
    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint)
    {
        Drawable b = getDrawable();
        canvas.save();

        int transY = bottom - b.getBounds().bottom;
        if ( mVerticalAlignment == ALIGN_BASELINE )
        {
            transY -= paint.getFontMetricsInt().descent;
        }

        canvas.translate(x, transY);
        b.draw(canvas);
        canvas.restore();

    }
}