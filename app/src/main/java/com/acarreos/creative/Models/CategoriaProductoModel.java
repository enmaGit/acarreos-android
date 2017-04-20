package com.acarreos.creative.Models;

/**
 * Created by EnmanuelPc on 04/09/2015.
 */
public class CategoriaProductoModel {

    public Integer id;
    public String nombre;
    public String descripcion;
    public int comision;
    public int dias_puja;

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

    public int getComision() {
        return comision;
    }

    public void setComision(int comision) {
        this.comision = comision;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDias_puja() {
        return dias_puja;
    }

    public void setDias_puja(int dias_puja) {
        this.dias_puja = dias_puja;
    }

}

