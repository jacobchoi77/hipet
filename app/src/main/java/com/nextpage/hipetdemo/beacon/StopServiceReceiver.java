package com.nextpage.hipetdemo.beacon;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static com.nextpage.hipetdemo.beacon.BeaconService.NOTI_ID_STOP;

/**
 * Created by jacobsFactory on 2017-11-13.
 */

public class StopServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BeaconService.class);
        context.stopService(service);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTI_ID_STOP);
    }
}