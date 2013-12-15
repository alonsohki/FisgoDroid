package net.meneame.fisgodroid;

import android.content.Context;
import android.view.View;

public class ChatBubbleView extends ChatBaseView
{
    private View mMessageLayout;

    public ChatBubbleView(Context context)
    {
        super(context, R.layout.chat_bubble_line);

        if ( !isInEditMode() )
        {
            init();
        }
    }
    
    private void init() {
        mMessageLayout = findViewById(R.id.message_layout);
    }

    @Override
    public void setChatMessage(ChatMessage chatMsg, boolean highlight)
    {
        super.setChatMessage(chatMsg, highlight);

        switch (chatMsg.getType())
        {
        case ADMIN:
            mMessageLayout.setBackgroundResource(R.drawable.chatbubble_admin);
            break;
        case FRIENDS:
            mMessageLayout.setBackgroundResource(R.drawable.chatbubble_friends);
            break;
        case PUBLIC:
            mMessageLayout.setBackgroundResource(R.drawable.chatbubble_public);
            break;
        }
    }
}
