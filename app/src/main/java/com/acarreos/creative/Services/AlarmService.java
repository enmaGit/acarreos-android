package com.acarreos.creative.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.AppConstants;
import com.acarreos.creative.R;

/**
 * Created by EnmanuelPc on 22/01/2016.
 */
public class AlarmService extends Service {
    private final static String NOTIF_SPAM = "5";

    public final static int ALARM_ID = 1253;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_notif)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText("Mira las novedades en Acarreos")
                        .setSound(alarmSound)
                        .setAutoCancel(true);

        Bundle data = new Bundle();
        data.putString(AppConstants.KEY_TIPO, NOTIF_SPAM);
        Intent notIntent = new Intent(this, BaseActivity.class);
        notIntent.putExtras(data);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contIntent);
        mNotificationManager.notify(Integer.valueOf(NOTIF_SPAM), mBuilder.build());
    }
}
