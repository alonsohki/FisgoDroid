package net.meneame.fisgodroid.adapters;

import java.util.List;

import net.meneame.fisgodroid.ChatBaseView;
import net.meneame.fisgodroid.ChatMessage;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ChatMessageAdapter extends BaseAdapter
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
        return mMessages.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return getItem(position).hashCode();
    }
    
    public abstract ChatBaseView createView(Context context);

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChatBaseView line = (ChatBaseView)convertView;
        if ( line == null )
        {
            line = createView(mContext);
        }
        ChatMessage chatmsg = (ChatMessage)getItem(position);
        String lowercaseMsg = chatmsg.getMessage().toLowerCase();
        
        boolean highlight = lowercaseMsg.contains(mUsername.toLowerCase()) ||
                            ( mIsAdmin && lowercaseMsg.contains("admin") );
        line.setChatMessage(chatmsg, highlight);
                
        return line;
    }
}
