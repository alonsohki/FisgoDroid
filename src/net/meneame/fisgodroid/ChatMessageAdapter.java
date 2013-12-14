package net.meneame.fisgodroid;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ChatMessageAdapter extends BaseAdapter
{
    private List<ChatMessage> mMessages = null;
    private Context mContext;
    private String mUsername;
    private boolean mIsAdmin = false;
    
    public ChatMessageAdapter ( Context context )
    {
        mContext = context;
    }
    
    public void setUsername ( String username )
    {
        mUsername = username;
    }
    
    public void setIsAdmin ( boolean isAdmin )
    {
        mIsAdmin = isAdmin;
    }
    
    public void setMessages ( List<ChatMessage> messages )
    {
        mMessages = messages;
        this.notifyDataSetChanged();
    }

    @Override
    public int getCount()
    {
        if ( mMessages != null )
            return mMessages.size();
        return 0;
    }

    @Override
    public Object getItem(int position)
    {
        return mMessages.get(getCount() - position - 1);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChatLineView line = (ChatLineView)convertView;
        if ( line == null )
        {
            line = new ChatLineView(mContext);
        }
        ChatMessage chatmsg = (ChatMessage)getItem(position);
        String lowercaseMsg = chatmsg.getMessage().toLowerCase();
        
        boolean highlight = lowercaseMsg.contains(mUsername.toLowerCase()) ||
                            ( mIsAdmin && lowercaseMsg.contains("admin") );
        line.setChatMessage(chatmsg, highlight);
                
        return line;
    }
}
