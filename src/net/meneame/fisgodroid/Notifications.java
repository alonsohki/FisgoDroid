package net.meneame.fisgodroid;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

public class Notifications
{
    private static final int NOTIFICATION_ID = 1;
    
    private static boolean msOnForeground = false;
    private static List<ChatMessage> msNotifications = new ArrayList<ChatMessage>();
    
    private static boolean isRunningInForeground ()
    {
        return msOnForeground;
    }
    
    public static void setOnForeground ( Context context, boolean onForeground )
    {
        msOnForeground = onForeground;
        if ( msOnForeground == true )
        {
            msNotifications.clear();
            
            // Update the notification
            Notification notification = buildNotification ( context, false );
            NotificationManager mNotificationManager =
                    (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
    
    public static void startOnForeground ( Service service )
    {
        startOnForeground ( service, false );
    }
    
    private static void startOnForeground ( Service service, boolean playSound )
    {
        service.startForeground(NOTIFICATION_ID, buildNotification(service.getApplicationContext(), playSound));
    }
    
    public static void stopOnForeground ( Service service )
    {
        service.stopForeground(true);
    }
    
    private static Notification buildNotification ( Context context, boolean playSound )
    {
        Resources res = context.getResources();
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.ding);
        
        String title = res.getString(R.string.app_name);
        String tapToOpen = res.getString(R.string.click_to_open);
        boolean hasNewMessages = msNotifications.size() > 0;
        
        // Build the compatible notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                .setSmallIcon(hasNewMessages ? R.drawable.ic_new_messages : R.drawable.ic_launcher)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setLights(0xffff8c00, 500, 1000);
        if ( playSound )
            builder.setSound(soundUri);
        
        // Build a different message depending on wether we have notifications
        if ( !hasNewMessages )
        {
            builder.setContentText(tapToOpen);
        }
        else
        {
            String message = String.format(res.getString(R.string.you_have_pending_notifications), msNotifications.size()) + "\n";
            for ( int i = msNotifications.size() - 1; i >= 0; --i )
            {
                ChatMessage msg = msNotifications.get(i);
                message = message + "<" + msg.getUser() + "> " + msg.getMessage() + "\n";
            }
            builder.setContentText(message);
        }
        
        // Make it Android 4 stylish
        NotificationCompat.InboxStyle bigTextStyle = new NotificationCompat.InboxStyle();
        bigTextStyle.setBigContentTitle(title);
        if ( !hasNewMessages )
        {
            bigTextStyle.addLine(tapToOpen);
        }
        else
        {
            bigTextStyle.addLine(String.format(res.getString(R.string.you_have_pending_notifications), msNotifications.size()));
            for ( int i = msNotifications.size() - 1; i >= 0; --i )
            {
                ChatMessage msg = msNotifications.get(i);
                bigTextStyle.addLine(Html.fromHtml("<b>" + msg.getUser() + "</b> " + msg.getMessage()));
            }
        }
        builder.setStyle(bigTextStyle);
        
        // Creates an explicit intent for ChatActivity
        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.setAction("android.intent.action.MAIN");
        
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

    public static void theyMentionedMe ( Service service, ChatMessage message )
    {
        if ( !isRunningInForeground() )
        {
            msNotifications.add(message);
            startOnForeground(service, true);
        }
    }
}
