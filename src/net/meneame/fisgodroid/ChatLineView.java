package net.meneame.fisgodroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatLineView extends LinearLayout
{
    private ChatMessage mMessage = null;
    
    public ChatLineView(Context context)
    {
        super(context);
    }

    public void setChatMessage ( ChatMessage chatMsg )
    {
        removeAllViewsInLayout();
        mMessage = chatMsg;
        if ( mMessage != null )
        {
            LayoutInflater.from(getContext()).inflate(R.layout.chat_line, this);
            TextView message = (TextView)findViewById(R.id.chat_message);
            TextView username = (TextView)findViewById(R.id.chat_username);
            
            message.setText(mMessage.getMessage());
            username.setText(mMessage.getUser());
        }
    }
}
