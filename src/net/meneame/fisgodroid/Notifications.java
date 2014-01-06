package net.meneame.fisgodroid;

import java.util.ArrayList;
import java.util.List;

import net.meneame.fisgodroid.R;
import net.meneame.fisgodroid.R.drawable;
import net.meneame.fisgodroid.R.string;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.Html;

public class Notifications
{
    private static final int STICKY_NOTIFICATION_ID = 1;
    private static final int NOTIFICATION_NOTIFICATION_ID = 2;
    private static int msNotificationId = 3;
    private static int msLastNotificationId = 3;

    private static boolean msOnForeground = false;
    private static List<ChatMessage> msNotifications = new ArrayList<ChatMessage>();

    private static boolean isRunningInForeground()
    {
        return msOnForeground;
    }

    public static void setOnForeground(Context context, boolean onForeground)
    {
        msOnForeground = onForeground;
        if ( msOnForeground == true )
        {
            msNotifications.clear();

            // Update the notification
            Notification notification = buildNotification(context, false, null);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(STICKY_NOTIFICATION_ID, notification);

            // Clean all the notifications
            for (; msLastNotificationId < msNotificationId; ++msLastNotificationId)
            {
                mNotificationManager.cancel(msLastNotificationId);
            }

            mNotificationManager.cancel(NOTIFICATION_NOTIFICATION_ID);
        }
    }

    public static void startOnForeground(Service service)
    {
        startOnForeground(service, false);
    }

    private static void startOnForeground(Service service, boolean playSound)
    {
        service.startForeground(STICKY_NOTIFICATION_ID, buildNotification(service.getApplicationContext(), playSound, null));
    }

    public static void stopOnForeground(Service service)
    {
        service.stopForeground(true);
    }

    private static Notification buildNotification(Context context, boolean playSound, ChatMessage chatMsg)
    {
        Resources res = context.getResources();
        String message;
        String title = res.getString(R.string.app_name);
        String tapToOpen = res.getString(R.string.click_to_open);
        boolean hasNewMessages = msNotifications.size() > 0;

        // Build a different message depending on wether we have notifications
        if ( chatMsg != null )
        {
            message = chatMsg.getUser() + ": " + chatMsg.getMessage();
        }
        else if ( !hasNewMessages )
        {
            message = tapToOpen;
        }
        else
        {
            message = String.format(res.getString(R.string.you_have_pending_notifications), msNotifications.size()) + "\n";
            for (int i = msNotifications.size() - 1; i >= 0; --i)
            {
                ChatMessage msg = msNotifications.get(i);
                message = message + "<" + msg.getUser() + "> " + msg.getMessage() + "\n";
            }
        }

        // Make it Android 4 stylish
        NotificationCompat.InboxStyle bigTextStyle = new NotificationCompat.InboxStyle();
        bigTextStyle.setBigContentTitle(title);
        if ( chatMsg != null )
        {
            String msg = "<b>" + chatMsg.getUser() + "</b> " + chatMsg.getMessage();
            bigTextStyle.addLine(Html.fromHtml(msg));
        }
        else if ( !hasNewMessages )
        {
            bigTextStyle.addLine(tapToOpen);
        }
        else
        {
            bigTextStyle.addLine(String.format(res.getString(R.string.you_have_pending_notifications), msNotifications.size()));
            for (int i = msNotifications.size() - 1; i >= 0; --i)
            {
                ChatMessage msg = msNotifications.get(i);
                bigTextStyle.addLine(Html.fromHtml("<b>" + msg.getUser() + "</b> " + msg.getMessage()));
            }
        }

        boolean useLights = hasNewMessages || chatMsg != null;
        int icon = (hasNewMessages || chatMsg != null) ? R.drawable.ic_new_messages : R.drawable.ic_launcher;
        return buildNotification(context, playSound, useLights, icon, message, bigTextStyle);
    }

    private static Notification buildNotification(Context context, boolean playSound, boolean useLights, int icon, String message, NotificationCompat.InboxStyle bigTextStyle)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources res = context.getResources();
        String title = res.getString(R.string.app_name);

        int defaults = 0;

        // Build the compatible notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setContentTitle(title).setAutoCancel(true);
        if ( useLights )
            builder.setLights(0xffff6000, 500, 1000);
        builder.setSmallIcon(icon);

        if ( playSound )
        {
            String ringtone = prefs.getString("notifications_new_message_ringtone", null);
            Uri ringtoneUri = null;
            if ( ringtone == null )
            {
                ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
            else
            {
                ringtoneUri = Uri.parse(ringtone);
            }
            builder.setSound(ringtoneUri);

            // Vibrate only if we are playing a sound
            if ( prefs.getBoolean("notifications_new_message_vibrate", true) )
            {
                defaults |= Notification.DEFAULT_VIBRATE;
            }
        }

        builder.setContentText(message);
        if ( bigTextStyle != null )
        {
            builder.setStyle(bigTextStyle);
        }
        builder.setDefaults(defaults);

        // Creates an explicit intent for ChatActivity
        Intent resultIntent = new Intent(context, ChatActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        resultIntent.setAction("android.intent.action.MAIN");

        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }

    private static void singleNotification(Context context, ChatMessage chatMsg)
    {
        // Update the notification
        Notification notification = buildNotification(context, true, chatMsg);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // This is a small hack to reuse the sticky notification for the first
        // one.
        int notificationId = msNotificationId;
        if ( notificationId == msLastNotificationId )
        {
            notificationId = STICKY_NOTIFICATION_ID;
        }

        mNotificationManager.notify(notificationId, notification);
        ++msNotificationId;
    }

    public static void theyMentionedMe(Service service, ChatMessage message)
    {
        if ( !isRunningInForeground() )
        {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(service);
            if ( prefs.getBoolean("notifications_new_message", true) )
            {
                if ( groupNotifications(service) )
                {
                    msNotifications.add(message);
                    startOnForeground(service, true);
                }
                else
                {
                    singleNotification(service, message);
                }
            }
        }
    }

    public static void newNotificationCount(Context context, int count)
    {
        if ( !isRunningInForeground() )
        {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if ( count > 0 )
            {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                if ( prefs.getBoolean("notifications_web_notifications", true) )
                {
                    String message = context.getResources().getString(R.string.you_have_notifications);
                    message = String.format(message, Integer.valueOf(count));
                    notificationManager.notify(NOTIFICATION_NOTIFICATION_ID, buildNotification(context, true, true, R.drawable.ic_launcher, message, null));
                }
            }
            else
            {
                notificationManager.cancel(NOTIFICATION_NOTIFICATION_ID);
            }
        }
    }

    private static boolean groupNotifications(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean group = prefs.getBoolean("notifications_group", false);
        return group;
    }
}
