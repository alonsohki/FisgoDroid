package net.meneame.fisgodroid.notifications;

import net.meneame.fisgodroid.R;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationView extends LinearLayout
{
    private TextView mCountView;
    private TextView mTitleView;

    public NotificationView(Context context)
    {
        super(context);

        if ( !isInEditMode() )
        {
            init();
        }
    }

    public NotificationView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if ( !isInEditMode() )
        {
            init();
        }
    }

    private void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.notification_view, this);

        mCountView = (TextView) findViewById(R.id.count);
        mTitleView = (TextView) findViewById(R.id.title);
    }

    public void setElement(NotificationElement element)
    {
        setTag(element);
        mTitleView.setText(element.getTitle());
        mCountView.setText("" + element.getCount());
        if ( element.getCount() > 0 )
        {
            mCountView.setBackgroundColor(getResources().getColor(R.color.notifications_color));
        }
        else
        {
            mCountView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        }
    }

}
