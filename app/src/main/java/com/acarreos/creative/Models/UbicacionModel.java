package com.acarreos.creative.Models;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 09/09/2015.
 */
public class UbicacionModel {

    @SerializedName("envio_id")
    public int idEnvio;

    public double latitud;

    public double longitud;

    @SerializedName("fecha_update")
    public String fechaUpdate;

    public int getIdEnvio() {
        return idEnvio;
    }

    public LatLng getPos() {
        return new LatLng(latitud, longitud);
    }

    public void setIdEnvio(int idEnvio) {
        this.idEnvio = idEnvio;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getFechaUpdate() {
        return fechaUpdate;
    }

    public void setFechaUpdate(String fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }
}
