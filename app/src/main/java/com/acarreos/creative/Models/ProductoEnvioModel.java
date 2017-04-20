package com.acarreos.creative.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 04/09/2015.
 */
public class ProductoEnvioModel {

    public Integer envio_id;

    @SerializedName("tipo_producto")
    public CategoriaProductoModel categoria;

    public Integer cantidad;
    public Float largo;
    public Float ancho;
    public Float alto;
    public Float peso;
    public String descripcion;

    @SerializedName("producto_id")
    public int id;

    @SerializedName("foto")
    public String urlFoto;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getEnvio_id() {
        return envio_id;
    }

    public void setEnvio_id(Integer envio_id) {
        this.envio_id = envio_id;
    }

    public CategoriaProductoModel getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProductoModel categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Float getLargo() {
        return largo;
    }

    public void setLargo(Float largo) {
        this.largo = largo;
    }

    public Float getAlto() {
        return alto;
    }

    public void setAlto(Float alto) {
        this.alto = alto;
    }

    public Float getAncho() {
        return ancho;
    }

    public void setAncho(Float ancho) {
        this.ancho = ancho;
    }

    public Float getPeso() {
        return peso;
    }

    public void setPeso(Float peso) {
        this.peso = peso;
    }

    public String getUrlFoto() {
        return urlFoto;
    }

    public void setUrlFoto(String urlFoto) {
        this.urlFoto = urlFoto;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
