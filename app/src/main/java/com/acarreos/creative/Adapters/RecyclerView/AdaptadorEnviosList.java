package com.acarreos.creative.Adapters.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.OfertasModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AddresLocationHandler;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.dd.ShadowLayout;
import com.squareup.okhttp.OkHttpClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import static com.acarreos.creative.Models.EstatusEnvioModel.ESTATUS_CANCELADO;
import static com.acarreos.creative.Models.EstatusEnvioModel.ESTATUS_DESARROLLO;
import static com.acarreos.creative.Models.EstatusEnvioModel.ESTATUS_FINALIZADO;
import static com.acarreos.creative.Models.EstatusEnvioModel.ESTATUS_SUBASTA;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class AdaptadorEnviosList extends RecyclerView.Adapter<AdaptadorEnviosList.EnvioViewHolder> {

    ArrayList<EnvioModel> listaDeEnvios;

    public static int MAX_LENGTH_DIRECCION = 20;

    BaseActivity activityPadre;

    public AdaptadorEnviosList(Activity context, @Nullable ArrayList<EnvioModel> listaDeEnvios) {
        this.activityPadre = (BaseActivity) context;
        if (listaDeEnvios == null) {
            listaDeEnvios = new ArrayList<>();
        }
        this.listaDeEnvios = listaDeEnvios;
    }

    public void addEnvio(EnvioModel envioInfo) {
        listaDeEnvios.add(envioInfo);
        notifyDataSetChanged();
    }

    @Override
    public EnvioViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.envio_item_list, parent, false);

        EnvioViewHolder envioViewHolder = new EnvioViewHolder(itemView);
        return envioViewHolder;
    }

    @Override
    public void onBindViewHolder(EnvioViewHolder holder, int position) {
        EnvioModel envioInfo = listaDeEnvios.get(position);
        holder.bindEnvio(activityPadre, envioInfo);
    }

    @Override
    public int getItemCount() {
        return listaDeEnvios.size();
    }

    public void vaciar() {
        listaDeEnvios = new ArrayList<>();
        notifyDataSetChanged();
    }

    public EnvioModel removeItem(int position) {
        final EnvioModel model = listaDeEnvios.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, EnvioModel model) {
        listaDeEnvios.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final EnvioModel model = listaDeEnvios.remove(fromPosition);
        listaDeEnvios.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<EnvioModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<EnvioModel> newModels) {
        for (int i = listaDeEnvios.size() - 1; i >= 0; i--) {
            final EnvioModel model = listaDeEnvios.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<EnvioModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final EnvioModel model = newModels.get(i);
            if (!listaDeEnvios.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<EnvioModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final EnvioModel model = newModels.get(toPosition);
            final int fromPosition = listaDeEnvios.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public static class EnvioViewHolder extends RecyclerView.ViewHolder {

        TextView textoFechaPub;
        TextView textStatus;
        TextView textTitle;
        TextView userEnvio;
        TextView txtOrigen;
        TextView txtOrigenRef;
        TextView txtDestino;
        TextView txtDestinoRef;
        TextView txtMaxDias;
        TextView txtMejorPujaPrecio;
        TextView txtMejorPujaUser;
        TextView txtRestanteParaPujar;
        TextView btnVerPujas;
        TextView btnVerProductos;
        TextView labelTxtOfertar;
        ImageView imageUser;
        RelativeLayout btnOfertar;
        ShadowLayout btnVerDetalle;
        ShadowLayout btnValorar;
        RelativeLayout contenedorInfoUbicacion;
        RelativeLayout contenedorInfoOfertas;
        LinearLayout contenedorBtnDetails;

        public EnvioViewHolder(View itemView) {
            super(itemView);
            contenedorInfoUbicacion = (RelativeLayout) itemView.findViewById(R.id.contenedorInfoUbicacion);
            contenedorInfoOfertas = (RelativeLayout) itemView.findViewById(R.id.contenedorInfoOfertas);
            contenedorBtnDetails = (LinearLayout) itemView.findViewById(R.id.contenedorBtnDetails);
            btnValorar = (ShadowLayout) itemView.findViewById(R.id.btnValorar);
            textoFechaPub = (TextView) itemView.findViewById(R.id.textFecha);
            textStatus = (TextView) itemView.findViewById(R.id.textEstatus);
            textTitle = (TextView) itemView.findViewById(R.id.txtDescripcion);
            userEnvio = (TextView) itemView.findViewById(R.id.textUserEnvio);
            txtOrigen = (TextView) itemView.findViewById(R.id.txtDesde);
            txtOrigenRef = (TextView) itemView.findViewById(R.id.txtDesdeRef);
            txtDestino = (TextView) itemView.findViewById(R.id.txtHasta);
            txtDestinoRef = (TextView) itemView.findViewById(R.id.txtHastaRef);
            txtMaxDias = (TextView) itemView.findViewById(R.id.txtDuracion);
            txtMejorPujaPrecio = (TextView) itemView.findViewById(R.id.txtPrecioMejor);
            txtMejorPujaUser = (TextView) itemView.findViewById(R.id.txtUserPuja);
            txtRestanteParaPujar = (TextView) itemView.findViewById(R.id.txtRestantePuja);
            imageUser = (ImageView) itemView.findViewById(R.id.imgUser);
            btnVerPujas = (TextView) itemView.findViewById(R.id.btnVerPujas);
            btnVerProductos = (TextView) itemView.findViewById(R.id.btnVerProductos);
            btnOfertar = (RelativeLayout) itemView.findViewById(R.id.btnOfertar);
            labelTxtOfertar = (TextView) itemView.findViewById(R.id.labelTxtOfertar);
            btnVerDetalle = (ShadowLayout) itemView.findViewById(R.id.btnDetalle);
        }

        public void bindEnvio(final BaseActivity actividadPadre, final EnvioModel envioInfo) {
            Date fecha = new Date(envioInfo.getFecha_pub());
            textoFechaPub.setText(android.text.format.DateFormat.format("dd-MM-yyyy", fecha));
            textStatus.setText(envioInfo.getEstatus().getDescripcion());
            btnValorar.setVisibility(View.GONE);
            switch (envioInfo.getEstatus().getId()) {
                case ESTATUS_SUBASTA:
                    textStatus.setBackgroundResource(R.drawable.square_corner_yellow);
                    break;
                case ESTATUS_CANCELADO:
                    textStatus.setBackgroundResource(R.drawable.square_corner_red);
                    break;
                case ESTATUS_DESARROLLO:
                    textStatus.setBackgroundResource(R.drawable.square_corner_green);
                    contenedorBtnDetails.setVisibility(View.GONE);
                    contenedorInfoOfertas.setVisibility(View.GONE);
                    contenedorInfoUbicacion.setVisibility(View.GONE);
                    break;
                case ESTATUS_FINALIZADO:
                    textStatus.setBackgroundResource(R.drawable.square_corner_blue);
                    contenedorBtnDetails.setVisibility(View.GONE);
                    contenedorInfoOfertas.setVisibility(View.GONE);
                    contenedorInfoUbicacion.setVisibility(View.GONE);
                    UserModel userActual = new ReminderSession(actividadPadre).obtenerInfoSession().getUser();
                    if (envioInfo.getValoracion() == null && userActual.getTipo_user_id() == UserModel.TIPO_CLIENTE) {
                        btnValorar.setVisibility(View.VISIBLE);
                    }
                    break;
            }
            textTitle.setText(envioInfo.getTitle());
            userEnvio.setText(envioInfo.getCliente().getLogin());
            txtOrigen.setSelected(true);
            AddresLocationHandler locationHandlerOrigen = new AddresLocationHandler(txtOrigen);
            locationHandlerOrigen.getAddresFromLatLon(envioInfo.getOrigen(), true);
            txtDestino.setSelected(true);
            AddresLocationHandler locationHandlerDestino = new AddresLocationHandler(txtDestino);
            locationHandlerDestino.getAddresFromLatLon(envioInfo.getDestino(), true);
            txtOrigenRef.setText(envioInfo.getOrigenRef());
            txtOrigenRef.setSelected(true);
            txtDestinoRef.setText(envioInfo.getDestinoRef());
            txtDestinoRef.setSelected(true);
            txtMaxDias.setText(envioInfo.getMax_dias() + " dias");
            if (envioInfo.ofertas.size() > 0) {
                txtMejorPujaPrecio.setText("$" + String.format("%,.2f", envioInfo.getMejorPuja().getPrecioPuja()));
                txtMejorPujaUser.setText(envioInfo.getMejorPuja().getTransportista().getLogin());
                txtMejorPujaUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        actividadPadre.abrirCuentaTranspor(envioInfo.getMejorPuja().getTransportista());
                    }
                });
                imageUser.setVisibility(View.VISIBLE);
            } else {
                txtMejorPujaPrecio.setText("SIN OFERTAS");
                txtMejorPujaUser.setText("");
                imageUser.setVisibility(View.GONE);
            }
            txtRestanteParaPujar.setText(envioInfo.getTiempoRestante());
            btnVerPujas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actividadPadre.abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_OFERTAS, false);
                }
            });
            btnVerProductos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    actividadPadre.abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_PRODUCTOS, false);
                }
            });
            btnVerDetalle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actividadPadre.abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_INFORMACION, false);
                }
            });
            SessionModel sessionInfo = new ReminderSession(actividadPadre).obtenerInfoSession();
            UserModel userActual = sessionInfo.getUser();
            if (userActual.getTipo_user_id() == UserModel.TIPO_CLIENTE) {
                btnOfertar.setVisibility(View.GONE);
            }
            if (userActual.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
                btnOfertar.setVisibility(View.VISIBLE);
                if (envioInfo.haOfertado(userActual)) {
                    labelTxtOfertar.setText("Modificar Oferta");
                } else {
                    labelTxtOfertar.setText("Ofertar");
                }
            }
            btnValorar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actividadPadre.abrirDialogoValorarCliente(envioInfo);
                }
            });
            btnOfertar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    abrirDialogoOferta(actividadPadre, envioInfo);
                }
            });
        }

        EditText txtFechaSalida;
        EditText txtFechaLlegada;
        EditText txtHoraSalida;
        EditText txtHoraLlegada;
        EditText txtPrecio;
        EditText txtComision;

        private void abrirDialogoOferta(final BaseActivity actividadPadre, final EnvioModel envioInfo) {
            final Dialog dialog = new Dialog(actividadPadre, R.style.Theme_Dialog_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_nueva_oferta);
            txtFechaSalida = (EditText) dialog.findViewById(R.id.textFecSalida);
            final TextInputLayout textInputFecSal = (TextInputLayout) dialog.findViewById(R.id.tilFecSalida);
            textInputFecSal.setErrorEnabled(true);
            txtFechaSalida.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                        txtFechaSalida.setText(year + "/" + (month + 1) + "/" + day);
                                    }
                                },
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.show(actividadPadre.getFragmentManager(), "DatepickerdialogHola");
                        return true;
                    }
                    return false;
                }
            });
            txtFechaLlegada = (EditText) dialog.findViewById(R.id.textFecLlegada);
            final TextInputLayout textInputFecLlegada = (TextInputLayout) dialog.findViewById(R.id.tilFecLlegada);
            textInputFecLlegada.setErrorEnabled(true);
            txtFechaLlegada.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Calendar now = Calendar.getInstance();
                        DatePickerDialog dpd = DatePickerDialog.newInstance(
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                        txtFechaLlegada.setText(year + "/" + (month + 1) + "/" + day);
                                    }
                                },
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                        );
                        dpd.show(actividadPadre.getFragmentManager(), "DatepickerdialogHola");
                        return true;
                    }
                    return false;
                }
            });
            txtHoraSalida = (EditText) dialog.findViewById(R.id.textHoraSalida);
            final TextInputLayout textInputHoraSal = (TextInputLayout) dialog.findViewById(R.id.tilHoraSalida);
            textInputHoraSal.setErrorEnabled(true);
            txtHoraSalida.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Calendar now = Calendar.getInstance();
                        TimePickerDialog dpd = TimePickerDialog.newInstance(
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hora, int minuto) {
                                        String horaCadena = hora + "";
                                        String minCadena = minuto + "";
                                        if (horaCadena.length() < 2) {
                                            horaCadena = "0" + horaCadena;
                                        }
                                        if (minCadena.length() < 2) {
                                            minCadena = "0" + minCadena;
                                        }
                                        txtHoraSalida.setText(horaCadena + ":" + minCadena + ":00");
                                    }
                                },
                                now.get(Calendar.HOUR),
                                now.get(Calendar.MINUTE),
                                false
                        );
                        dpd.show(actividadPadre.getFragmentManager(), "TimepickerdialogHola");
                        return true;
                    }
                    return false;
                }
            });
            txtHoraLlegada = (EditText) dialog.findViewById(R.id.textHoraLlegada);
            final TextInputLayout textInputHoraLlegada = (TextInputLayout) dialog.findViewById(R.id.tilHoraLlegada);
            textInputHoraLlegada.setErrorEnabled(true);
            txtHoraLlegada.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Calendar now = Calendar.getInstance();
                        TimePickerDialog dpd = TimePickerDialog.newInstance(
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(RadialPickerLayout radialPickerLayout, int hora, int minuto) {
                                        String horaCadena = hora + "";
                                        String minCadena = minuto + "";
                                        if (horaCadena.length() < 2) {
                                            horaCadena = "0" + horaCadena;
                                        }
                                        if (minCadena.length() < 2) {
                                            minCadena = "0" + minCadena;
                                        }
                                        txtHoraLlegada.setText(horaCadena + ":" + minCadena + ":00");
                                    }
                                },
                                now.get(Calendar.HOUR),
                                now.get(Calendar.MINUTE),
                                false
                        );
                        dpd.show(actividadPadre.getFragmentManager(), "TimepickerdialogHola");
                        return true;
                    }
                    return false;
                }
            });
            txtComision = (EditText) dialog.findViewById(R.id.textComision);
            txtPrecio = (EditText) dialog.findViewById(R.id.textPrecio);
            txtPrecio.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().length() > 0) {
                        float precio = Float.valueOf(charSequence.toString());
                        float comision = envioInfo.getComisionFinal() / 100f;
                        float added = precio * comision;
                        txtComision.setText(String.format("%,.2f", added) + "");
                    } else {
                        txtComision.setText("0.00");
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            final TextInputLayout textInputPrecio = (TextInputLayout) dialog.findViewById(R.id.tilPrecio);
            textInputPrecio.setErrorEnabled(true);
            SessionModel sessionInfo = new ReminderSession(actividadPadre).obtenerInfoSession();
            final UserModel userActual = sessionInfo.getUser();
            if (envioInfo.haOfertado(userActual)) {
                OfertasModel ofertaInfo = envioInfo.getOfertaUsuario(userActual);
                txtPrecio.setText(ofertaInfo.getPrecioPuja() + "");
                txtFechaSalida.setText(ofertaInfo.getFechaSalida());
                txtFechaLlegada.setText(ofertaInfo.getFechaLlegada());
                txtHoraSalida.setText(ofertaInfo.getHoraSalida());
                txtHoraLlegada.setText(ofertaInfo.getHoraLlegada());
            }
            TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
            final TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
            btnGuardar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean valido = true;
                    if (txtFechaSalida.getText().toString().length() == 0) {
                        valido = false;
                        txtFechaSalida.setError("Este campo es obligatorio");
                    }
                    if (txtFechaLlegada.getText().toString().length() == 0) {
                        valido = false;
                        txtFechaLlegada.setError("Este campo es obligatorio");
                    }
                    if (txtHoraSalida.getText().toString().length() == 0) {
                        valido = false;
                        txtHoraSalida.setError("Este campo es obligatorio");
                    }
                    if (txtHoraLlegada.getText().toString().length() == 0) {
                        valido = false;
                        txtHoraLlegada.setError("Este campo es obligatorio");
                    }
                    if (txtPrecio.getText().toString().length() == 0) {
                        valido = false;
                        txtPrecio.setError("Este campo es obligatorio");
                    }
                    if (valido) {
                        OfertasModel ofertaInfo = new OfertasModel();
                        ofertaInfo.setFechaLlegada(txtFechaLlegada.getText().toString());
                        ofertaInfo.setFechaSalida(txtFechaSalida.getText().toString());
                        ofertaInfo.setHoraLlegada(txtHoraLlegada.getText().toString());
                        ofertaInfo.setHoraSalida(txtHoraSalida.getText().toString());
                        ofertaInfo.setPrecioPuja(Float.valueOf(txtPrecio.getText().toString()));
                        if (envioInfo.haOfertado(userActual)) {
                            modifOferta(actividadPadre, ofertaInfo, envioInfo);
                        } else {
                            enviarOferta(actividadPadre, ofertaInfo, envioInfo);
                        }
                        dialog.dismiss();
                    }
                }
            });
            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        private void enviarOferta(Context context, OfertasModel ofertaInfo, EnvioModel envioInfo) {
            final Dialog dialogLoading = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();
            SessionModel sessionInfo = new ReminderSession(context).obtenerInfoSession();
            final String token = sessionInfo.getToken();

            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new AuthRequestInterceptor((BaseActivity) context));

            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(UrlsServer.RUTA_SERVER)
                    .setClient(new OkClient(client));

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("token", token);
                }
            });

            builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                public void log(String msg) {
                    Log.i("retrofit", msg);
                }
            });

            RestAdapter adapter = builder.build();
            EnvioPeticiones servicioOfertas = adapter.create(EnvioPeticiones.class);
            servicioOfertas.agregarOferta(envioInfo.getId(), ofertaInfo, new Callback<OfertasModel>() {
                @Override
                public void success(OfertasModel ofertasModel, Response response) {
                    Snackbar.make(btnOfertar, "Oferta publicada", Snackbar.LENGTH_LONG).show();
                    dialogLoading.dismiss();
                }

                @Override
                public void failure(RetrofitError error) {
                    dialogLoading.dismiss();
                    Log.d("ERROR1", error.toString());
                    //Log.d("ERROR2", error.getMessage());
                    //Log.d("ERROR3", error.getUrl());
                    Snackbar.make(btnOfertar, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                }
            });
        }

        private void modifOferta(Context context, OfertasModel ofertaInfo, EnvioModel envioInfo) {
            final Dialog dialogLoading = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();
            SessionModel sessionInfo = new ReminderSession(context).obtenerInfoSession();
            final String token = sessionInfo.getToken();

            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new AuthRequestInterceptor((BaseActivity) context));

            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(UrlsServer.RUTA_SERVER)
                    .setClient(new OkClient(client));

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("token", token);
                }
            });

            builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                public void log(String msg) {
                    Log.i("retrofit", msg);
                }
            });

            RestAdapter adapter = builder.build();
            EnvioPeticiones servicioOfertas = adapter.create(EnvioPeticiones.class);
            int idOferta = envioInfo.getOfertaUsuario(sessionInfo.getUser()).id;
            servicioOfertas.modificarOferta(envioInfo.getId(), idOferta, ofertaInfo, new Callback<OfertasModel>() {
                @Override
                public void success(OfertasModel ofertasModel, Response response) {
                    Snackbar.make(btnOfertar, "Oferta publicada", Snackbar.LENGTH_LONG).show();
                    dialogLoading.dismiss();
                }

                @Override
                public void failure(RetrofitError error) {
                    dialogLoading.dismiss();
                    Log.d("ERROR1", error.toString());
                    Log.d("ERROR2", error.getMessage());
                    Log.d("ERROR3", error.getUrl());
                    Snackbar.make(btnOfertar, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

}
