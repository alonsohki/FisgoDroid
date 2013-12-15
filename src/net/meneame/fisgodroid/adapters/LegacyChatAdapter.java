package net.meneame.fisgodroid.adapters;

import net.meneame.fisgodroid.ChatBaseView;
import net.meneame.fisgodroid.ChatLineView;
import net.meneame.fisgodroid.ChatMessageAdapter;
import android.content.Context;

public class LegacyChatAdapter extends ChatMessageAdapter
{

    public LegacyChatAdapter(Context context)
    {
        super(context);
    }

    @Override
    public ChatBaseView createView(Context context)
    {
        return new ChatLineView(context);
    }

}
