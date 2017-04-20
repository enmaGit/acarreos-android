package com.acarreos.creative.PeticionesWeb;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.OfertasModel;
import com.acarreos.creative.Models.ProductoEnvioModel;
import com.acarreos.creative.Models.UbicacionModel;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public interface EnvioPeticiones {

    @GET(UrlsServer.CLIENTE_ENVIOS)
    void obtenerEnviosUser(@Path("id") int idUsuario, Callback<List<EnvioModel>> respuestaServidor);

    @GET(UrlsServer.TRANSPOR_ENVIOS)
    void obtenerEnviosTransportista(Callback<List<EnvioModel>> respuestaServidor);

    @GET(UrlsServer.ENVIO_DETALLE)
    void obtenerDetalleEnvio(@Path("id") int idUsuario, Callback<EnvioModel> respuestaServidor);

    @GET(UrlsServer.NOTIF_FIN_ENVIO)
    void notificarFinEnvio(@Path("id") int idEnvio, Callback<EnvioModel> respuestaServidor);

    @POST(UrlsServer.CLIENTE_ENVIOS)
    void solicitarEnvio(@Path("id") int idUsuario, @Body EnvioModel envioInfo, Callback<EnvioModel> respuestaServidor);

    @PUT(UrlsServer.CLIENTE_ENVIOS_MODIF)
    void modificarEnvio(@Path("id") int idUsuario, @Path("id_envio") int idEnvio, @Body EnvioModel envioInfo, Callback<EnvioModel> respuestaServidor);

    @GET(UrlsServer.ENVIO_PRODUCTO)
    void obtenerProductosEnvio(@Path("id") int idEnvio, Callback<List<ProductoEnvioModel>> respuestaServidor);

    @Multipart
    @POST(UrlsServer.ENVIO_PRODUCTO)
    void agregarProductoEnvio(
            @Path("id") int idEnvio,
            @Part("producto_id") TypedString tipoProductoId,
            @Part("cantidad") TypedString cantidad,
            @Part("largo") TypedString largo,
            @Part("alto") TypedString alto,
            @Part("ancho") TypedString ancho,
            @Part("peso") TypedString peso,
            @Part("photo") TypedFile fotoProducto,
            Callback<ProductoEnvioModel> respuestaServidor);

    @PATCH(UrlsServer.ENVIO_PRODUCTO_DETALLE)
    void modificarFotoProducto(
            @Path("id") int idEnvio,
            @Path("producto_id") int tipoProductoId,
            @Part("photo") TypedFile fotoProducto,
            Callback<ProductoEnvioModel> respuestaServidor);

    @GET(UrlsServer.ENVIO_UBICACION)
    void obtenerEnvioUbicaciones(@Path("id") int idEnvio, Callback<List<UbicacionModel>> respuestaServidor);

    @POST(UrlsServer.ENVIO_UBICACION)
    void actualizarUbicacion(@Path("id") int idEnvio, @Body UbicacionModel ubicacionInfo, Callback<UbicacionModel> respuestaServidor);

    @GET(UrlsServer.ENVIO_OFERTA)
    void obtenerEnvioOfertas(@Path("id") int idEnvio, Callback<List<OfertasModel>> respuestaServidor);

    @POST(UrlsServer.ENVIO_OFERTA)
    void agregarOferta(@Path("id") int idEnvio, @Body OfertasModel ofertaInfo, Callback<OfertasModel> respuestaServidor);

    @PUT(UrlsServer.ENVIO_OFERTA_DETALLE)
    void modificarOferta(@Path("id") int idEnvio, @Path("oferta_id") int idOferta, @Body OfertasModel ofertaInfo, Callback<OfertasModel> respuestaServidor);

    @POST(UrlsServer.ENVIO_GANADOR)
    void definirEnvioGanador(@Path("id") int idEnvio, @Body Object o, Callback<Object> respuestaServidor);

}
