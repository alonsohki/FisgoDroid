package net.meneame.fisgodroid.notifications;

import java.util.ArrayList;
import java.util.List;

import net.meneame.fisgodroid.R;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

public class NotificationsLayout extends LinearLayout
{
    private ListView mListView;
    private final List<NotificationElement> mElements = new ArrayList<NotificationElement>();
    private ElementAdapter mAdapter;

    public NotificationsLayout(Context context)
    {
        super(context);

        if ( !isInEditMode() )
        {
            init();
        }
    }

    public NotificationsLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        if ( !isInEditMode() )
        {
            init();
        }
    }

    private void init()
    {
        LayoutInflater.from(getContext()).inflate(R.layout.notifications_layout, this);

        mListView = (ListView) findViewById(R.id.list);

        mAdapter = new ElementAdapter(getContext());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                Object tag = arg1.getTag();
                if ( tag instanceof NotificationElement )
                {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(((NotificationElement) tag).getBaseUrl()));
                    getContext().startActivity(i);
                }
            }
        });
    }

    public void addNotificationsElement(String type, String baseUrl, int title)
    {
        mElements.add(new NotificationElement(type, baseUrl, title, 0));
        mAdapter.setElements(mElements);
    }

    public void setNotificationCount(String type, int count)
    {
        for (NotificationElement element : mElements)
        {
            if ( element.getType().equals(type) )
            {
                element.setCount(count);
                mAdapter.setElements(mElements);
                break;
            }
        }
    }
}
