package com.acarreos.creative.PeticionesWeb;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.TipoTransModel;
import com.acarreos.creative.Models.UserTransModel;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by EnmanuelPc on 11/10/2015.
 */
public interface TransportePeticiones {

    @GET(UrlsServer.TIPO_TRANSPORTE)
    void obtenerTipoTransporte(
            Callback<List<TipoTransModel>> respuestaServidor
    );

    @GET(UrlsServer.USER_TRANSPORTE)
    void obtenerTransportesTranspor(
            @Path("id") int idUser,
            Callback<List<UserTransModel>> respuestaServidor
    );

    @Multipart
    @POST(UrlsServer.USER_TRANSPORTE)
    void addTransportesTranspor(
            @Path("id") int idUser,
            @Part("transporte_id") TypedString transporId,
            @Part("condicion") TypedString condicion,
            @Part("placa") TypedString placa,
            @Part("poliza_compa") TypedString polizaCompa,
            @Part("poliza_numero") TypedString polizaNumero,
            @Part("photo") TypedFile fotoTransporte,
            Callback<UserTransModel> respuestaServidor
    );
}
