package net.meneame.fisgodroid;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class DynamicTextView extends TextView implements Drawable.Callback
{
    private Handler mHandler = new Handler();

    public DynamicTextView(Context context)
    {
        super(context);
    }

    public DynamicTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    @Override
    public void onTextChanged ( CharSequence text, int start, int lengthBefore, int lengthAfter )
    {
        super.onTextChanged ( text, start, lengthBefore, lengthAfter );
        if ( text instanceof Spanned )
        {
            Spanned span = (Spanned) text;
            ImageSpan[] spans = span.getSpans(0, span.length() - 1, ImageSpan.class);
            for (ImageSpan s : spans)
            {
                Drawable drawable = s.getDrawable();
                drawable.setCallback(this);
            }
        }
    }

    @Override
    public void invalidateDrawable(Drawable dr)
    {
        mHandler.post(new Runnable() {
            @Override public void run()
            {
                setText(getText());
            }
        });
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when)
    {
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what)
    {
    }
}
