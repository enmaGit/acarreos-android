package com.acarreos.creative.Models;

/**
 * Created by EnmanuelPc on 04/09/2015.
 */
public class EstatusEnvioModel {

    public static final int ESTATUS_SUBASTA = 1;
    public static final int ESTATUS_CANCELADO = 2;
    public static final int ESTATUS_DESARROLLO = 3;
    public static final int ESTATUS_FINALIZADO = 4;

    public Integer id;
    public String descripcion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
