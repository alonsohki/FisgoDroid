package net.meneame.fisgodroid.adapters;

import net.meneame.fisgodroid.ChatBaseView;
import net.meneame.fisgodroid.ChatBubbleView;
import android.content.Context;

public class BubblesChatAdapter extends ChatMessageAdapter
{

    public BubblesChatAdapter(Context context)
    {
        super(context);
    }

    @Override
    public ChatBaseView createView(Context context)
    {
        return new ChatBubbleView(context);
    }
}
