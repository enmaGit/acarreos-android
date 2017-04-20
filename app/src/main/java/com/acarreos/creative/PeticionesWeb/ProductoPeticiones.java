package com.acarreos.creative.PeticionesWeb;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.CategoriaProductoModel;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public interface ProductoPeticiones {

    @GET(UrlsServer.TIPO_PRODUCTO)
    void obtenerCategoriasProducto(Callback<List<CategoriaProductoModel>> respuestaServidor);

    @PUT(UrlsServer.TIPO_PRODUCTO_DETALLE)
    void modificarCategoriaProducto(@Path("id") int idCategoria, @Body CategoriaProductoModel cateInfo, Callback<CategoriaProductoModel> respuestaServidor);
}
