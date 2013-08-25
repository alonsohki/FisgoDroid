package net.meneame.fisgodroid;

import org.xml.sax.XMLReader;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
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
            String parsedMessage = Smileys.parseMessage(mChatMsg.getMessage());
            Spanned spannedMessage = Html.fromHtml(parsedMessage, Smileys.getImageGetter(getContext()), null);
            mMessage.setText(spannedMessage);
            mUsername.setText(mChatMsg.getUser());
            mAvatarStorage.request(mChatMsg.getIcon(), mSetAvatarRunnable);
            
            if ( mChatMsg.getType() == ChatType.PUBLIC )
                mMessage.setTextColor(getResources().getColor(R.color.text_chat_general));
            else if ( mChatMsg.getType() == ChatType.FRIENDS )
                mMessage.setTextColor(getResources().getColor(R.color.text_chat_friends));
        }
    }
}
