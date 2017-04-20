package com.acarreos.creative.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 14/10/2015.
 */
public class UserTransModel {

    @SerializedName("tipo_transporte")
    public TipoTransModel transporte;

    @SerializedName("transporte_id")
    public int idTransporte;

    public String placa;

    @SerializedName("poliza_compa")
    public String polizaCompa;

    @SerializedName("poliza_numero")
    public String polizaNumero;

    @SerializedName("foto")
    public String urlFoto;

    public String condicion;

    public int getIdTransporte() {
        return idTransporte;
    }

    public void setIdTransporte(int idTransporte) {
        this.idTransporte = idTransporte;
    }

    public TipoTransModel getTransporte() {
        return transporte;
    }

    public void setTransporte(TipoTransModel transporte) {
        this.transporte = transporte;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getPolizaCompa() {
        return polizaCompa;
    }

    public void setPolizaCompa(String polizaCompa) {
        this.polizaCompa = polizaCompa;
    }

    public String getPolizaNumero() {
        return polizaNumero;
    }

    public void setPolizaNumero(String polizaNumero) {
        this.polizaNumero = polizaNumero;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }
}
