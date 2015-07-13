package net.meneame.fisgodroid;

import java.text.SimpleDateFormat;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
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

import com.squareup.picasso.Picasso;

public class ChatBaseView extends LinearLayout
{
    private ChatMessage mChatMsg = null;
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
    
    public ChatBaseView(Context context, int resourceId)
    {
        super(context);

        // Create the view
        LayoutInflater.from(getContext()).inflate(resourceId, this, true);
        mMessage = (TextView) findViewById(R.id.chat_message);
        mUsername = (TextView) findViewById(R.id.chat_username);
        mAvatar = (ImageView) findViewById(R.id.chat_avatar);
        mTimestamp = (TextView) findViewById(R.id.chat_timestamp);

        setSelectableText();
        mMessage.setMovementMethod(new CustomMovementMethod());
        
        // Used for showing the user's profile
        mAvatar.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                intent.putExtra("userid", mChatMsg.getUserid());
                getContext().startActivity(intent);
            }
        });
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

        Picasso.with(getContext()).cancelRequest(mAvatar);
        
        if ( mChatMsg != null )
        {
            String parsedMessage = Smileys.parseMessage(mChatMsg.getMessage());

            // Highlight when they mention our name
            if ( highlight )
            {
                parsedMessage = "<b>" + parsedMessage + "</b>";
            }
            
            // Timestamp formatting
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            
            Spanned message = Html.fromHtml(parsedMessage, null, Smileys.getTagHandler(getContext(), mMessage));
            mMessage.setText(message);
            mUsername.setText(mChatMsg.getUser());
            Picasso.with(getContext()).load(mChatMsg.getIcon()).placeholder(R.drawable.default_avatar).into(mAvatar);
            
            mTimestamp.setText(dateFormat.format(mChatMsg.getWhen()));
        }
    }
}
