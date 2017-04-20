package com.acarreos.creative.Util;

import android.util.Log;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.ServerConstants;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class AuthRequestInterceptor implements Interceptor {

    private BaseActivity controladorVista;

    public AuthRequestInterceptor(BaseActivity controladorVista) {
        this.controladorVista = controladorVista;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        ReminderSession reminderSession = new ReminderSession(controladorVista);
        Response response = chain.proceed(request);
        String nuevoToken = response.header(ServerConstants.HEADER_AUTH);
        if (nuevoToken != null) {
            if (nuevoToken.contains("Bearer")) {
                Log.d("TOKEN", nuevoToken);
                int indexBlank = nuevoToken.lastIndexOf(" ");
                nuevoToken = nuevoToken.substring(indexBlank + 1);
                reminderSession.updateToken(nuevoToken);
            }
        }
        if (response.code() == ServerConstants.STATUS_UNAUTHORIZED) {
            //controladorVista.cerrarSesion();
        }
        return response;
    }

}
