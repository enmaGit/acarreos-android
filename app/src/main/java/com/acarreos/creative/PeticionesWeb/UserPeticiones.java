package com.acarreos.creative.PeticionesWeb;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by EnmanuelPc on 30/08/2015.
 */
public interface UserPeticiones {

    @POST(UrlsServer.LOGIN)
    void iniciarSession(@Body SessionModel loginInfo, Callback<SessionModel> respuestaServidor);

    @FormUrlEncoded
    @POST(UrlsServer.RESET_PASSWORD)
    void resetPassword(@Field("email") String email, Callback<Object> respuestaServidor);

    @POST(UrlsServer.REGISTRO)
    void registrarUsuario(@Body UserModel usuarioInfo, Callback<SessionModel> respuestaServidor);

    @FormUrlEncoded
    @POST(UrlsServer.SPAM)
    void mandarSpam(@Field("mensaje") String mensaje, Callback<UserModel> respuestaServidor);

    @GET(UrlsServer.LOGOUT)
    void cerrarSession(Callback<Object> respuestaServidor);

    @PATCH(UrlsServer.USER)
    void actualizarDatos(@Path("id") int idUsuario, @Body UserModel userInfo, Callback<SessionModel> respuestaServidor);

}
