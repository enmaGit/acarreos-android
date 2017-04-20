package com.acarreos.creative.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.gson.Gson;

import static com.acarreos.creative.Constants.AppConstants.KEY_TIPO;

/**
 * Created by EnmanuelPc on 03/11/2015.
 */
public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private static final int NOTIF_ENVIO_FINALIZADO = 1;
    public static final int NOTIF_NUEVA_OFERTA = 2;
    public static final int NOTIF_OFERTA_ACEPTADA = 3;
    public static final int NOTIF_ACT_UBICACION = 4;
    public static final int NOTIF_SPAM = 5;
    public static final int NOTIF_ENVIO_CANCELADO = 6;


    @Override
    public void onMessageReceived(String from, Bundle data) {
        Log.d(TAG, "Llego algo");
        //Toast.makeText(this, "Llego algo", Toast.LENGTH_SHORT).show();
        /*TODO IR REVISANDO LOS TIPOS DE NOTIFICACIONES
        Mostrar l pantalla de recogido valorar y comentar.
         */

        SessionModel sessionInfo = new ReminderSession(this).obtenerInfoSession();
        if (sessionInfo != null) {

            int tipoNotificacion = Integer.valueOf(data.getString(KEY_TIPO));
            switch (tipoNotificacion) {
                case NOTIF_ENVIO_FINALIZADO:
                    notificarRecibido(data, NOTIF_ENVIO_FINALIZADO);
                    break;
                case NOTIF_NUEVA_OFERTA:
                    notificarRecibido(data, NOTIF_NUEVA_OFERTA);
                    break;
                case NOTIF_OFERTA_ACEPTADA:
                    notificarRecibido(data, NOTIF_OFERTA_ACEPTADA);
                    break;
                case NOTIF_ACT_UBICACION:
                    notificarRecibido(data, NOTIF_ACT_UBICACION);
                    break;
                case NOTIF_SPAM:
                    notificarSpam(data, NOTIF_SPAM);
                    break;
                case NOTIF_ENVIO_CANCELADO:
                    notificarRecibido(data, NOTIF_ENVIO_CANCELADO);
                    break;
                default:
                    //Toast.makeText(this, "Valor ninguno: " + tipoNotificacion, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    }

    private void notificarSpam(Bundle data, int idNotif) {
        String message = data.getString("msg");
        for (String value : data.keySet()) {
            Log.d(TAG, "Value " + value + ": " + data.get(value));
        }
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_notif)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(message)
                        .setSound(alarmSound)
                        .setAutoCancel(true);

        Intent notIntent = new Intent(this, BaseActivity.class);
        notIntent.putExtras(data);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contIntent);
        mNotificationManager.notify(idNotif, mBuilder.build());
    }

    private void notificarRecibido(Bundle data, int idNotif) {
        String message = data.getString("msg");
        for (String value : data.keySet()) {
            Log.d(TAG, "Value " + value + ": " + data.get(value));
        }
        String envioJson = data.getString("envio");
        Gson conversorJson = new Gson();
        final EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
        SessionModel sessionInfo = new ReminderSession(this).obtenerInfoSession();
        UserModel userInfo = sessionInfo.getUser();
        if (idNotif == NOTIF_OFERTA_ACEPTADA) {
            if (envioInfo.getGanador().getTransportista().getLogin().compareTo(userInfo.getLogin()) != 0) {
                message = "Su oferta ha sido rechazada";
            }
        }

        if (idNotif == NOTIF_ENVIO_CANCELADO) {
            if (userInfo.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
                message = "Un envío ofertado ha sido cancelado";
            }
        }

        if (idNotif == NOTIF_NUEVA_OFERTA) {
            message = "Han hecho una puja sobre una oferta";
        }

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher_notif)
                        .setContentTitle(getString(R.string.app_name))
                        .setSound(alarmSound)
                        .setContentText(message)
                        .setAutoCancel(true);

        Intent notIntent = new Intent(this, BaseActivity.class);
        notIntent.putExtras(data);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Log.d("Prueba2", envioJson);
        PendingIntent contIntent = PendingIntent.getActivity(
                this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contIntent);
        mNotificationManager.notify(idNotif, mBuilder.build());
    }

}
