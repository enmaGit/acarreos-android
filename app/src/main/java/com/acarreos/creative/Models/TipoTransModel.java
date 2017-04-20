package com.acarreos.creative.Models;

/**
 * Created by EnmanuelPc on 09/10/2015.
 */
public class TipoTransModel {

    public Integer id;

    public String nombre;

    public String descripcion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
