package com.acarreos.creative.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by EnmanuelPc on 30/08/2015.
 */
public class UserModel {

    public static int TIPO_ADMIN = 1;
    public static int TIPO_CLIENTE = 2;
    public static int TIPO_TRANSPORTISTA = 3;
    public final static int TIPO_CEDULA = 1;
    public final static int TIPO_CODIGO_ENTIDAD_PUBLICA = 2;
    public final static int TIPO_REGISTRO_UNICO_CONTRIBUYENTE = 3;
    public final static int TIPO_PASAPORTE = 4;

    public int id;

    @SerializedName("id_push")
    public String idPush;

    public String nombre;
    public String apellido;
    public String login;
    public String password;
    public String email;
    public String fecha_nac;
    public String telefono;
    public int tipo_user_id;

    @SerializedName("tipo_dni")
    public int tipoDni;

    public String dni;

    @SerializedName("tipo_licencia")
    public String tipoLicencia;

    @SerializedName("num_seguridad")
    public String numSeguridad;

    public float valoracion;

    @SerializedName("envios_realizados")
    public int enviosRealizados;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFecha_nac() {
        return fecha_nac;
    }

    public void setFecha_nac(String fecha_nac) {
        this.fecha_nac = fecha_nac;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getTipo_user_id() {
        return tipo_user_id;
    }

    public void setTipo_user_id(int tipo_user_id) {
        this.tipo_user_id = tipo_user_id;
    }

    public void actualizarDatos(UserModel userInfo) {
        if (userInfo.getNombre() != null) {
            this.nombre = userInfo.getNombre();
        }
        if (userInfo.getApellido() != null) {
            this.apellido = userInfo.getApellido();
        }
        if (userInfo.getEmail() != null) {
            this.email = userInfo.getEmail();
        }
        if (userInfo.getTelefono() != null) {
            this.telefono = userInfo.getTelefono();
        }
        if (userInfo.getPassword() != null) {
            this.password = userInfo.getPassword();
        }
    }

    public String getIdPush() {
        return idPush;
    }

    public void setIdPush(String idPush) {
        this.idPush = idPush;
    }

    public float getValoracion() {
        return valoracion;
    }

    public void setValoracion(float valoracion) {
        this.valoracion = valoracion;
    }

    public int getEnviosRealizados() {
        return enviosRealizados;
    }

    public void setEnviosRealizados(int enviosRealizados) {
        this.enviosRealizados = enviosRealizados;
    }

    public int getTipoDni() {
        return tipoDni;
    }

    public void setTipoDni(int tipoDni) {
        this.tipoDni = tipoDni;
    }

    public String getTipoLicencia() {
        return tipoLicencia;
    }

    public void setTipoLicencia(String tipoLicencia) {
        this.tipoLicencia = tipoLicencia;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNumSeguridad() {
        return numSeguridad;
    }

    public void setNumSeguridad(String numSeguridad) {
        this.numSeguridad = numSeguridad;
    }
}
