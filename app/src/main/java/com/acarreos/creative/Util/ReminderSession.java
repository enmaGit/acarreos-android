package com.acarreos.creative.Util;

import android.content.Context;
import android.content.SharedPreferences;

import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.google.gson.Gson;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class ReminderSession {

    private static final String NOMBRE_KEY_STATUS_CC = "carga_continua_key";
    Context context;
    private final String NOMBRE_SHARED_SESSION = "session_reminder_shared";
    private final String NOMBRE_KEY_SESSION = "session_key";
    private final String NOMBRE_KEY_ID_PUSH = "push_key";
    private final String NOMBRE_SHARED_ID_PUSH = "push_reminder_shared";
    private final String ACTION_GUARDAR_SESSION = "ACTION_GUARDAR_SESSION";
    private final String ACTION_GUARDAR_ID_PUSH = "ACTION_GUARDAR_ID_PUSH";

    public ReminderSession(Context context) {
        this.context = context;
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_GUARDAR_SESSION);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == ACTION_GUARDAR_SESSION) {
                    String jsonSession = intent.getStringExtra(NOMBRE_KEY_SESSION);
                    Gson conversorJson = new Gson();
                    SessionModel sessionInfo = conversorJson.fromJson(jsonSession, SessionModel.class);
                    setContext(context);
                    guardarSession(sessionInfo);
                } else if (intent.getAction() == ACTION_GUARDAR_ID_PUSH) {
                    String idPush = intent.getStringExtra(NOMBRE_KEY_ID_PUSH);
                    setContext(context);
                    guardarIdPush(idPush);
                }
            }
        }, filter);*/
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void guardarSession(SessionModel sessionInfo) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson conversorJson = new Gson();
        String jsonSession = conversorJson.toJson(sessionInfo);
        editor.putString(NOMBRE_KEY_SESSION, jsonSession);
        editor.apply();
    }

    public void guardarIdPush(String idPush) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_ID_PUSH, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NOMBRE_KEY_ID_PUSH, idPush);
        editor.apply();
    }

    public SessionModel obtenerInfoSession() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        String jsonSession = sharedPreferences.getString(NOMBRE_KEY_SESSION, " ");
        if (jsonSession.compareTo(" ") == 0) {
            return null;
        }
        Gson conversorJson = new Gson();
        SessionModel sessionInfo = conversorJson.fromJson(jsonSession, SessionModel.class);
        return sessionInfo;
    }

    public String obtenerIdPush() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_ID_PUSH, Context.MODE_PRIVATE);
        String idPush = sharedPreferences.getString(NOMBRE_KEY_ID_PUSH, " ");
        return idPush;
    }

    public void borrarSession() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void updateToken(String token) {
        SessionModel sessionInfo = obtenerInfoSession();
        sessionInfo.setToken(token);
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson conversorJson = new Gson();
        String jsonSession = conversorJson.toJson(sessionInfo);
        editor.putString(NOMBRE_KEY_SESSION, jsonSession);
        editor.apply();
    }

    public boolean getCargaContinuaStatus() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        boolean status = sharedPreferences.getBoolean(NOMBRE_KEY_STATUS_CC, false);
        return status;
    }

    public void setCargaContinuaStatus(boolean status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NOMBRE_KEY_STATUS_CC, status);
        editor.apply();
    }


    public void updateUser(UserModel userInfo) {
        SessionModel sessionInfo = obtenerInfoSession();
        UserModel userActual = sessionInfo.getUser();
        userActual.actualizarDatos(userInfo);
        sessionInfo.setUser(userActual);
        SharedPreferences sharedPreferences = context.getSharedPreferences(NOMBRE_SHARED_SESSION, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson conversorJson = new Gson();
        String jsonSession = conversorJson.toJson(sessionInfo);
        editor.putString(NOMBRE_KEY_SESSION, jsonSession);
        editor.apply();
    }
}
