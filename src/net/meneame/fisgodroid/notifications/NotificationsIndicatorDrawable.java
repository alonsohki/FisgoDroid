package net.meneame.fisgodroid.notifications;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

public class NotificationsIndicatorDrawable extends Drawable
{
    private int mNumNotifs = 0;
    private Drawable mDefaultDrawable;
    private Paint mBorderPaint;
    private Paint mCirclePaint;
    private Paint mTextPaint;
    private Rect mTextBounds;

    public NotificationsIndicatorDrawable(int borderColor, int backgroundColor, int textColor, Drawable defaultDrawable)
    {
        mDefaultDrawable = defaultDrawable.getConstantState().newDrawable();

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(backgroundColor);
        mCirclePaint.setStyle(Style.FILL);

        mBorderPaint = new Paint();
        mBorderPaint.setAntiAlias(true);
        mBorderPaint.setColor(borderColor);
        mBorderPaint.setStyle(Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setColor(textColor);
        mTextPaint.setStyle(Style.FILL);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        mTextBounds = new Rect();
    }

    public void setNotificationCount(int count)
    {
        mNumNotifs = count;
    }

    public int getNotificationCount()
    {
        return mNumNotifs;
    }

    @Override
    public void draw(Canvas canvas)
    {
        Rect bounds = getBounds();
        if ( mNumNotifs == 0 )
        {
            mDefaultDrawable.draw(canvas);
        }
        else
        {
            final float width = bounds.width();
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), width * 0.5f, mCirclePaint);
            canvas.drawCircle(bounds.exactCenterX(), bounds.exactCenterY(), width * 0.5f, mBorderPaint);

            // Get the target width from the square that fits the circle
            final float targetWidth = (float) Math.sqrt(width * width * 0.5f);

            final String text = "" + mNumNotifs;
            setTextSizeToFit(text, mTextPaint, targetWidth, targetWidth);
            mTextPaint.getTextBounds(text, 0, text.length(), mTextBounds);
            canvas.drawText(text, bounds.exactCenterX(), bounds.exactCenterY() + (mTextBounds.height() / 2), mTextPaint);
        }
    }

    private static void setTextSizeToFit(String text, Paint textPaint, float targetWidth, float targetHeight)
    {
        float sizeHigh = 100.0f;
        float sizeLow = 2.0f;
        float current;
        final Rect bounds = new Rect();

        textPaint.getTextBounds(text, 0, text.length(), bounds);

        // Specialize the algorithm depending on the biggest dimension
        if ( bounds.width() > bounds.height() )
        {
            float width;
            do
            {
                current = (float) Math.floor((sizeLow + sizeHigh) * 0.5f);
                textPaint.setTextSize(current);
                width = textPaint.measureText(text);

                if ( Math.abs(targetWidth - width) < 2.0f )
                {
                    break;
                }

                if ( width > targetWidth )
                {
                    sizeHigh = current;
                }
                else
                {
                    sizeLow = current;
                }
            }
            while (true);
        }
        else
        {
            float height;
            do
            {
                current = (float) Math.floor((sizeLow + sizeHigh) * 0.5f);
                textPaint.setTextSize(current);
                textPaint.getTextBounds(text, 0, text.length(), bounds);
                height = bounds.height();

                if ( Math.abs(targetHeight - height) < 2.0f )
                {
                    break;
                }

                if ( height > targetHeight )
                {
                    sizeHigh = current;
                }
                else
                {
                    sizeLow = current;
                }
            }
            while (true);
        }
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSPARENT;
    }

    @Override
    public void setAlpha(int arg0)
    {
        mDefaultDrawable.setAlpha(arg0);
        mCirclePaint.setAlpha(arg0);
        mTextPaint.setAlpha(arg0);
    }

    @Override
    public void setColorFilter(ColorFilter arg0)
    {
        mDefaultDrawable.setColorFilter(arg0);
        mCirclePaint.setColorFilter(arg0);
        mTextPaint.setColorFilter(arg0);
    }

    @Override
    public int getIntrinsicWidth()
    {
        return mDefaultDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight()
    {
        return mDefaultDrawable.getIntrinsicHeight();
    }

    @Override
    public void setBounds(Rect bounds)
    {
        super.setBounds(bounds);
        mDefaultDrawable.setBounds(bounds);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom)
    {
        super.setBounds(left, top, right, bottom);
        mDefaultDrawable.setBounds(left, top, right, bottom);
    }
}
