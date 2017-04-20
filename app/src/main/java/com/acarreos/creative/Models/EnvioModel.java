package com.acarreos.creative.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */

public class EnvioModel {

    public static int STATUS_SUBASTA = 1;
    public static int STATUS_CANCELADO = 2;
    public static int STATUS_DESARROLLO = 3;
    public static int STATUS_FINALIZADO = 4;

    public Integer id;

    @SerializedName("user")
    public UserModel cliente;

    public EstatusEnvioModel estatus;

    @SerializedName("estatus_id")
    public int estatusId;

    @SerializedName("short_descripcion")
    public String title;

    @SerializedName("ref_origen")
    public String origenRef;
    public double lat_origen;
    public double lon_origen;
    public LatLng origen;


    @SerializedName("ref_destino")
    public String destinoRef;
    public double lat_destino;
    public double lon_destino;
    public LatLng destino;


    @SerializedName("max_dias")
    public String max_dias;

    @SerializedName("fecha_pub")
    public String fecha_pub;

    @SerializedName("fecha_sug")
    public String fechaSugerencia;

    @SerializedName("hora_sug")
    public String horaSugerencia;

    @SerializedName("fecha_res")
    public String fecha_res;

    @SerializedName("fecha_fin")
    public String fecha_fin;

    @SerializedName("foto")
    public String urlFoto;

    @SerializedName("comision_final")
    public int comisionFinal;

    @SerializedName("dias_puja")
    public int diasPuja;

    public Integer valoracion;

    public String comentario;

    public ArrayList<OfertasModel> ofertas = null;

    public ArrayList<ProductoEnvioModel> productos;

    public ArrayList<TipoTransModel> transportes;

    public OfertasModel ganador;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getComisionFinal() {
        return comisionFinal;
    }

    public void setComisionFinal(int comisionFinal) {
        this.comisionFinal = comisionFinal;
    }

    public EstatusEnvioModel getEstatus() {
        return estatus;
    }

    public void setEstatus(EstatusEnvioModel estatus) {
        this.estatus = estatus;
    }

    public String getDestinoRef() {
        return destinoRef;
    }

    public void setDestinoRef(String destinoRef) {
        this.destinoRef = destinoRef;
    }

    public String getOrigenRef() {
        return origenRef;
    }

    public void setOrigenRef(String origenRef) {
        this.origenRef = origenRef;
    }

    public UserModel getCliente() {
        return cliente;
    }

    public void setCliente(UserModel cliente) {
        this.cliente = cliente;
    }

    public int getEstatusId() {
        return estatusId;
    }

    public void setEstatusId(int estatusId) {
        this.estatusId = estatusId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getOrigen() {
        return new LatLng(lat_origen, lon_origen);
    }

    public void setOrigen(LatLng origen) {
        this.lat_origen = origen.latitude;
        this.lon_origen = origen.longitude;
    }

    public LatLng getDestino() {
        return new LatLng(lat_destino, lon_destino);
    }

    public void setDestino(LatLng destino) {
        this.lat_destino = destino.latitude;
        this.lon_destino = destino.longitude;
    }

    public String getMax_dias() {
        return max_dias;
    }

    public void setMax_dias(String max_dias) {
        this.max_dias = max_dias;
    }

    public String getFecha_pub() {
        return fecha_pub.replace("-", "/");
    }

    public void setFecha_pub(String fecha_pub) {
        this.fecha_pub = fecha_pub;
    }

    public String getFechaSugerencia() {
        return fechaSugerencia;
    }

    public void setFechaSugerencia(String fechaSugerencia) {
        this.fechaSugerencia = fechaSugerencia;
    }

    public String getHoraSugerencia() {
        return horaSugerencia;
    }

    public void setHoraSugerencia(String horaSugerencia) {
        this.horaSugerencia = horaSugerencia;
    }

    public String getFecha_res() {
        return fecha_res.replace("-", "/");
    }

    public void setFecha_res(String fecha_res) {
        this.fecha_res = fecha_res;
    }

    public String getFecha_fin() {
        return fecha_fin.replace("-", "/");
    }

    public void setFecha_fin(String fecha_fin) {
        this.fecha_fin = fecha_fin;
    }

    public Integer getValoracion() {
        return valoracion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public void setValoracion(Integer valoracion) {
        this.valoracion = valoracion;
    }

    public int getDiasPuja() {
        return diasPuja;
    }

    public void setDiasPuja(int diasPuja) {
        this.diasPuja = diasPuja;
    }

    public ArrayList<OfertasModel> getOfertas() {
        return ofertas;
    }

    public void setOfertas(ArrayList<OfertasModel> ofertas) {
        this.ofertas = ofertas;
    }

    public ArrayList<ProductoEnvioModel> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<ProductoEnvioModel> productos) {
        this.productos = productos;
    }

    public OfertasModel getGanador() {
        return ganador;
    }

    public void setGanador(OfertasModel ganador) {
        this.ganador = ganador;
    }

    public ArrayList<TipoTransModel> getTransportes() {
        return transportes;
    }

    public void setTransportes(ArrayList<TipoTransModel> transportes) {
        this.transportes = transportes;
    }

    public OfertasModel getMejorPuja() {
        float menorPrecio = Float.MAX_VALUE;
        OfertasModel mejorPuja = null;
        for (OfertasModel ofertaInfo : ofertas) {
            if (ofertaInfo.getPrecioPuja() < menorPrecio) {
                mejorPuja = ofertaInfo;
                menorPrecio = ofertaInfo.getPrecioPuja();
            }
        }
        return mejorPuja;
    }

    public String getTiempoRestante() {
        //TODO calcular el tiempo restante
        String textoFinal = getFecha_pub();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Calendar c = Calendar.getInstance();
            c.setTime(sdf.parse(getFecha_pub()));
            c.add(Calendar.DATE, getDiasPuja() + 1);
            textoFinal = sdf.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date fecha = new Date(textoFinal);
        return android.text.format.DateFormat.format("dd-MM-yyyy", fecha) + "/3:00 am";
    }

    public boolean haOfertado(UserModel userInfo) {
        for (OfertasModel ofertaInfo : ofertas) {
            if (ofertaInfo.getTransportista().getId() == userInfo.getId()) {
                return true;
            }
        }
        return false;
    }

    public OfertasModel getOfertaUsuario(UserModel userInfo) {
        for (OfertasModel ofertaInfo : ofertas) {
            if (ofertaInfo.getTransportista().getId() == userInfo.getId()) {
                return ofertaInfo;
            }
        }
        return null;
    }

}
