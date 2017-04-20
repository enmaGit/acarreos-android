package com.acarreos.creative.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 04/09/2015.
 */
public class OfertasModel {

    @SerializedName("envio_id")
    public int idEnvio;

    public int id;
    @SerializedName("precio_puja")
    public Float precioPuja;
    public UserModel transportista;
    @SerializedName("fecha_puja")
    public String fechaPuja;

    @SerializedName("hora_salida")
    public String horaSalida;

    @SerializedName("fecha_salida")
    public String fechaSalida;

    @SerializedName("hora_llegada")
    public String horaLlegada;

    @SerializedName("fecha_llegada")
    public String fechaLlegada;

    public Float getPrecioPuja() {
        return precioPuja;
    }

    public void setPrecioPuja(Float precioPuja) {
        this.precioPuja = precioPuja;
    }

    public UserModel getTransportista() {
        return transportista;
    }

    public void setTransportista(UserModel transportista) {
        this.transportista = transportista;
    }

    public String getFechaPuja() {
        return fechaPuja;
    }

    public void setFechaPuja(String fechaPuja) {
        this.fechaPuja = fechaPuja;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(String horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(String fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(String horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public String getFechaLlegada() {
        return fechaLlegada;
    }

    public void setFechaLlegada(String fechaLlegada) {
        this.fechaLlegada = fechaLlegada;
    }
}
