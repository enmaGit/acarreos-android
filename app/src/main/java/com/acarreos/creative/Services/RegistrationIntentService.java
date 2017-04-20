package com.acarreos.creative.Services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 03/11/2015.
 */

public class RegistrationIntentService extends IntentService {


    private static final String TAG = "RegIntentService";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ReminderSession reminderSession = new ReminderSession(this);

        InstanceID instanceID = InstanceID.getInstance(this);
        String token = null;
        try {
            token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "Nuevo token: " + token);

            reminderSession.guardarIdPush(token);
            actualizarTokenServer(token);

        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "no se pudo hacer nada");
        }
        // [END get_token]
        Log.i(TAG, "GCM Registration Token: " + token);
    }

    private void actualizarTokenServer(String token) {
        ReminderSession reminderSession = new ReminderSession(this);
        if (reminderSession.obtenerInfoSession() != null) {
            Log.d(TAG, "token hay que actualizar");

            UserModel userActual = reminderSession.obtenerInfoSession().getUser();
            Log.d(TAG, new Gson().toJson(userActual));
            userActual.setIdPush(token);

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setRequestInterceptor(new RequestInterceptor() {
                        @Override
                        public void intercept(RequestFacade request) {
                            request.addQueryParam("token", new ReminderSession(RegistrationIntentService.this).obtenerInfoSession().getToken());
                        }
                    })
                    .setEndpoint(UrlsServer.RUTA_SERVER)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setLog(new RestAdapter.Log() {
                        @Override
                        public void log(String message) {
                            Log.d(TAG, message);
                        }
                    })
                    .build();
            UserPeticiones servicioActDatos = restAdapter.create(UserPeticiones.class);

            servicioActDatos.actualizarDatos(userActual.getId(), userActual, new Callback<SessionModel>() {
                @Override
                public void success(SessionModel sessionInfo, Response response) {
                    UserModel userInfo = sessionInfo.getUser();
                    Log.d(TAG, "token actualizado en el servidor");
                    ReminderSession reminderSession = new ReminderSession(RegistrationIntentService.this);
                    if (reminderSession.obtenerInfoSession() != null) {
                        reminderSession.updateUser(userInfo);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    /*Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());*/
                }
            });
        }
    }

    private class DisplayToast implements Runnable {
        String mText;

        public DisplayToast(String text) {
            mText = text;
        }

        public void run() {
            Toast.makeText(RegistrationIntentService.this, mText, Toast.LENGTH_SHORT).show();
        }
    }

}
