package net.meneame.fisgodroid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class FisgoScheduler extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent serviceIntent = new Intent(context, FisgoService.class);
        serviceIntent.putExtra("reschedule", true);
        serviceIntent.putExtra("rescheduleDelay", 0);
        context.startService(serviceIntent);
    }
}