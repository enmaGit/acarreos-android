package com.acarreos.creative.Constants;

/**
 * Created by EnmanuelPc on 30/08/2015.
 */
public class UrlsServer {

    public static String RUTA_SERVER = "http://acarreospanama.info/api/v1";

    public static String RUTA_SERVER_RESET = "http://acarreospanama.info";

    public final static String LOGIN = "/auth/login";

    public final static String CLIENTE_ENVIOS = "/cliente/{id}/envio";

    public final static String CLIENTE_ENVIOS_MODIF = "/cliente/{id}/envio/{id_envio}";

    public final static String NOTIF_FIN_ENVIO = "/notificacion/{id}";

    public final static String TRANSPOR_ENVIOS = "/envio";

    public final static String ENVIO_DETALLE = "/envio/{id}";

    public final static String REGISTRO = "/auth/register";

    public final static String SPAM = "/spam";

    public final static String LOGOUT = "/auth/logout";

    public final static String USER = "/user/{id}";

    public final static String ENVIO_PRODUCTO = "/envio/{id}/producto";

    public final static String ENVIO_PRODUCTO_DETALLE = "/envio/{id}/producto/{producto_id}";

    public final static String ENVIO_UBICACION = "/envio/{id}/ubicacion";

    public final static String ENVIO_OFERTA_DETALLE = "/envio/{id}/oferta/{oferta_id}";

    public final static String ENVIO_OFERTA = "/envio/{id}/oferta";

    public final static String ENVIO_GANADOR = "/envio/{id}/ganador";

    public final static String TIPO_PRODUCTO = "/tipo_producto";

    public final static String TIPO_PRODUCTO_DETALLE = "/tipo_producto/{id}";

    public final static String TIPO_TRANSPORTE = "/tipo_transporte";

    public final static String USER_TRANSPORTE = "/transportista/{id}/transporte";

    public final static String RESET_PASSWORD = "/password/email";

}
