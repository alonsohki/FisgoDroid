package net.meneame.fisgodroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ChatLineView extends LinearLayout
{
    private ChatMessage mChatMsg = null;
    private AvatarStorage mAvatarStorage;
    private TextView mUsername;
    private TextView mMessage;
    private ImageView mAvatar;
    
    // Runable to asynchronously set the avatar image
    private Runnable mSetAvatarRunnable = new Runnable ()
    {
        @Override
        public void run()
        {
            if ( mChatMsg != null )
            {
                Bitmap bitmap = mAvatarStorage.getAvatar(mChatMsg.getIcon());
                if ( bitmap != null )
                    mAvatar.setImageBitmap(bitmap);
            }
        }
    };
    
    public ChatLineView(Context context, AvatarStorage avatarStorage)
    {
        super(context);
        mAvatarStorage = avatarStorage;
        
        // Create the view
        LayoutInflater.from(getContext()).inflate(R.layout.chat_line, this, true);
        mMessage = (TextView)findViewById(R.id.chat_message);
        mUsername = (TextView)findViewById(R.id.chat_username);
        mAvatar = (ImageView)findViewById(R.id.chat_avatar);
    }

    public void setChatMessage ( ChatMessage chatMsg )
    {
        mChatMsg = chatMsg;
        if ( mChatMsg != null )
        {
            mMessage.setText(mChatMsg.getMessage());
            mUsername.setText(mChatMsg.getUser());
            mAvatarStorage.request(mChatMsg.getIcon(), mSetAvatarRunnable);
        }
    }
}
