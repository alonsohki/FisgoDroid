package net.meneame.fisgodroid.adapters;

import java.util.ArrayList;
import java.util.List;

import net.meneame.fisgodroid.ChatBaseView;
import net.meneame.fisgodroid.ChatBubbleView;
import net.meneame.fisgodroid.ChatMessage;
import android.content.Context;
import android.util.Log;

public class BubblesChatAdapter extends ChatMessageAdapter
{
    boolean mJoinBubbles = true;

    public BubblesChatAdapter(Context context)
    {
        super(context);
    }

    public void setJoinBubbles(boolean set)
    {
        mJoinBubbles = set;
    }

    public boolean getJoinBubbles()
    {
        return mJoinBubbles;
    }

    @Override
    public void setMessages(List<ChatMessage> messages)
    {
        if ( !mJoinBubbles || messages.size() < 2 )
        {
            super.setMessages(messages);
        }
        else
        {
            // Create a new message list by joining messages from same user that
            // are together.
            List<ChatMessage> newList = new ArrayList<ChatMessage>(messages.size() / 2);
            ChatMessage previous = messages.get(0);
            ChatMessage current;
            boolean cloned = false;

            newList.add(previous);

            for (int pos = 1; pos < messages.size(); ++pos)
            {
                current = messages.get(pos);
                if ( current.getUserid().equals(previous.getUserid()) && (current.getWhen().getTime() - previous.getWhen().getTime()) < 60000 )
                {
                    // Join the messages together
                    if ( !cloned )
                    {
                        // Clone the last message and replace it in the new list
                        previous = new ChatMessage(previous);
                        newList.remove(newList.size() - 1);
                        newList.add(previous);
                        cloned = true;
                    }
                    previous.setWhen(current.getWhen());
                    previous.setMessage(previous.getMessage() + "<br/>" + current.getMessage());
                }
                else
                {
                    newList.add(current);
                    previous = current;
                    cloned = false;
                }
            }

            super.setMessages(newList);
        }
    }

    @Override
    public ChatBaseView createView(Context context)
    {
        return new ChatBubbleView(context);
    }
}
