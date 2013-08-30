/**
 DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
                    Version 2, December 2004 

 Copyright (C) 2013 TheWonderWall 

 Everyone is permitted to copy and distribute verbatim or modified 
 copies of this license document, and changing it is allowed as long 
 as the name is changed. 

            DO WHAT THE FUCK YOU WANT TO PUBLIC LICENSE 
   TERMS AND CONDITIONS FOR COPYING, DISTRIBUTION AND MODIFICATION 

  0. You just DO WHAT THE FUCK YOU WANT TO.
 */

package net.meneame.fisgodroid;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatMessageAdapter extends BaseAdapter
{
    private List<ChatMessage> mMessages = null;
    private Context mContext;
    private AvatarStorage mAvatarStorage;
    private String mUsername;
    
    public ChatMessageAdapter ( Context context, AvatarStorage avatarStorage )
    {
        mContext = context;
        mAvatarStorage = avatarStorage;
    }
    
    public void setUsername ( String username )
    {
        mUsername = username;
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ChatLineView line = (ChatLineView)convertView;
        if ( line == null )
        {
            line = new ChatLineView(mContext, mAvatarStorage);
        }
        ChatMessage chatmsg = (ChatMessage)getItem(position);
        line.setChatMessage(mUsername, chatmsg);
                
        return line;
    }
}
