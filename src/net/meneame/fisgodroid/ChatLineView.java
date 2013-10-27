package net.meneame.fisgodroid;

import java.text.SimpleDateFormat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Selection;
import android.text.Spannable;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class ChatLineView extends LinearLayout
{
    private ChatMessage mChatMsg = null;
    private AvatarStorage mAvatarStorage;
    private TextView mUsername;
    private TextView mMessage;
    private ImageView mAvatar;
    private TextView mTimestamp;

    // From
    // http://stackoverflow.com/questions/15836306/can-a-textview-be-selectable-and-contain-links
    // Thanks oakleaf!
    private static class CustomMovementMethod extends LinkMovementMethod
    {
        @Override
        public boolean canSelectArbitrarily()
        {
            return true;
        }

        @Override
        public void initialize(TextView widget, Spannable text)
        {
            Selection.setSelection(text, text.length());
        }

        @Override
        public void onTakeFocus(TextView view, Spannable text, int dir)
        {
            if ( (dir & (View.FOCUS_FORWARD | View.FOCUS_DOWN)) != 0 )
            {
                if ( view.getLayout() == null )
                {
                    // This shouldn't be null, but do something sensible if it
                    // is.
                    Selection.setSelection(text, text.length());
                }
            }
            else
            {
                Selection.setSelection(text, text.length());
            }
        }
    }

    // Runable to asynchronously set the avatar image
    private Runnable mSetAvatarRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            if ( mChatMsg != null )
            {
                Drawable drawable = mAvatarStorage.getAvatar(mChatMsg.getIcon());
                if ( drawable != null )
                    mAvatar.setImageDrawable(drawable);
            }
        }
    };
    
    public ChatLineView(Context context, AvatarStorage avatarStorage)
    {
        super(context);
        mAvatarStorage = avatarStorage;
        // Create the view
        LayoutInflater.from(getContext()).inflate(R.layout.chat_line, this, true);
        mMessage = (TextView) findViewById(R.id.chat_message);
        mUsername = (TextView) findViewById(R.id.chat_username);
        mAvatar = (ImageView) findViewById(R.id.chat_avatar);
        mTimestamp = (TextView) findViewById(R.id.chat_timestamp);

        setSelectableText();
        mMessage.setMovementMethod(new CustomMovementMethod());
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setSelectableText()
    {
        // Set the text selectable on API level >= 11
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB )
        {
            mMessage.setTextIsSelectable(true);
        }
    }
    
    public void setChatMessage(ChatMessage chatMsg, boolean highlight )
    {
        mChatMsg = chatMsg;
        if ( mChatMsg != null )
        {
            String parsedMessage = Smileys.parseMessage(mChatMsg.getMessage());

            // Highlight when they mention our name
            if ( highlight )
            {
                parsedMessage = "<b>" + parsedMessage + "</b>";
            }
            
            Spanned message = Html.fromHtml(parsedMessage, Smileys.getImageGetter(getContext()), null);
            
            // Timestamp formatting
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            
            mMessage.setText(message);
            mUsername.setText(mChatMsg.getUser());
            mAvatarStorage.request(mChatMsg.getIcon(), mSetAvatarRunnable);
            
            // Used for showing the user's profile
            mAvatar.setTag(mChatMsg.getUser());
            
            mTimestamp.setText(dateFormat.format(mChatMsg.getWhen()));

            if ( mChatMsg.getType() == ChatType.PUBLIC )
                mMessage.setTextColor(getResources().getColor(R.color.text_chat_general));
            else if ( mChatMsg.getType() == ChatType.FRIENDS )
                mMessage.setTextColor(getResources().getColor(R.color.text_chat_friends));
            else if ( mChatMsg.getType() == ChatType.ADMIN )
                mMessage.setTextColor(getResources().getColor(R.color.text_chat_admin));
        }
    }
}
