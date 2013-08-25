package net.meneame.fisgodroid;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatMessageAdapter extends BaseAdapter
{
    private List<ChatMessage> mMessages = null;
    private Context mContext;
    
    public ChatMessageAdapter ( Context context )
    {
        mContext = context;
    }
    
    public void setMessages ( List<ChatMessage> messages )
    {
        mMessages = messages;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        if ( mMessages == null )
            return 0;
        return mMessages.size();
    }

    @Override
    public Object getItem(int position)
    {
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView view = new TextView(mContext);
        ChatMessage message = (ChatMessage)getItem(position);
        view.setText(message.getMessage());
        return view;
    }
}
