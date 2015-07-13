package net.meneame.fisgodroid.notifications;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

class ElementAdapter extends BaseAdapter
{
    private Context mContext;
    private List<NotificationElement> mElements;
    
    public ElementAdapter(Context context)
    {
        mContext = context;
    }
    
    public void setElements(List<NotificationElement> elements) {
        mElements = elements;
        notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        return mElements != null ? mElements.size() : 0;
    }

    @Override
    public Object getItem(int position)
    {
        return mElements.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        NotificationView view = (NotificationView)convertView;
        if (view == null) {
            view = new NotificationView(mContext);
        }
        view.setElement((NotificationElement)getItem(position));
        return view;
    }

}