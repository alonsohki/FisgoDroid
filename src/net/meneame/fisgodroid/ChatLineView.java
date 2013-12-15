package net.meneame.fisgodroid;

import android.content.Context;
import android.widget.TextView;

public class ChatLineView extends ChatBaseView
{
    private TextView mMessage;

    public ChatLineView(Context context)
    {
        super(context, R.layout.chat_line);

        if ( !isInEditMode() )
        {
            init();
        }
    }

    private void init()
    {
        mMessage = (TextView) findViewById(R.id.chat_message);
    }

    @Override
    public void setChatMessage(ChatMessage message, boolean highlight)
    {
        super.setChatMessage(message, highlight);

        if ( message.getType() == ChatType.PUBLIC )
            mMessage.setTextColor(getResources().getColor(R.color.text_chat_general));
        else if ( message.getType() == ChatType.FRIENDS )
            mMessage.setTextColor(getResources().getColor(R.color.text_chat_friends));
        else if ( message.getType() == ChatType.ADMIN )
            mMessage.setTextColor(getResources().getColor(R.color.text_chat_admin));
    }
}
