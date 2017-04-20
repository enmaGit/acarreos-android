package com.acarreos.creative.Fragments.VistaEnvio;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorOfertasList;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.EstatusEnvioModel;
import com.acarreos.creative.Models.OfertasModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.squareup.okhttp.OkHttpClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class FragmentListaOfertas extends Fragment {

    private RecyclerView recView;
    private CircularProgressView progressBar;
    private TextView txtNoOfertas;
    private AdaptadorOfertasList adaptadorOfertasList;
    private RelativeLayout btnOfertar;

    EnvioModel envioInfo;

    public static FragmentListaOfertas newInstance(EnvioModel envioInfo) {
        FragmentListaOfertas fragment = new FragmentListaOfertas();
        fragment.envioInfo = envioInfo;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vistaInflada = inflater.inflate(R.layout.layout_ofertas_envio, null);
        return vistaInflada;
    }

    SwipeRefreshLayout swipeRefreshListSolic;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recView = (RecyclerView) getView().findViewById(R.id.recView);
        progressBar = (CircularProgressView) getView().findViewById(R.id.progressBar);
        txtNoOfertas = (TextView) getView().findViewById(R.id.txtNoOfertas);
        recView.setHasFixedSize(true);
        swipeRefreshListSolic = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshSolicitudes);
        swipeRefreshListSolic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adaptadorOfertasList.vaciar();
                obtenerOfertas();
            }
        });
        adaptadorOfertasList = new AdaptadorOfertasList((BaseActivity) getActivity(), null, envioInfo);
        progressBar.setVisibility(View.VISIBLE);
        txtNoOfertas.setVisibility(View.GONE);
        recView.setAdapter(adaptadorOfertasList);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        btnOfertar = (RelativeLayout) getView().findViewById(R.id.btnOfertar);
        UserModel userActual = new ReminderSession(getActivity()).obtenerInfoSession().getUser();
        if ((envioInfo.getEstatus().getId() == EstatusEnvioModel.ESTATUS_SUBASTA) && (userActual.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA)) {
            btnOfertar.setVisibility(View.VISIBLE);
        } else {
            btnOfertar.setVisibility(View.GONE);
        }
        btnOfertar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirDialogoOferta();
            }
        });
        obtenerOfertas();
    }

    EditText txtFechaSalida;
    EditText txtFechaLlegada;
    EditText txtHoraSalida;
    EditText txtHoraLlegada;
    EditText txtPrecio;
    EditText txtComision;

    private void abrirDialogoOferta() {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
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
                    dpd.show(getActivity().getFragmentManager(), "DatepickerdialogHola");
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
                    dpd.show(getActivity().getFragmentManager(), "DatepickerdialogHola");
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
                    dpd.show(getActivity().getFragmentManager(), "TimepickerdialogHola");
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
                    dpd.show(getActivity().getFragmentManager(), "TimepickerdialogHola");
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
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
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
                        modifOferta(ofertaInfo);
                    } else {
                        enviarOferta(ofertaInfo);
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

    private void enviarOferta(OfertasModel ofertaInfo) {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

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
                adaptadorOfertasList.addOferta(ofertasModel);
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

    private void modifOferta(OfertasModel ofertaInfo) {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

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

    private void obtenerOfertas() {
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setClient(new OkClient(client));

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("token", token);
            }
        });

        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        servicioEnvio.obtenerEnvioOfertas(envioInfo.getId(), new Callback<List<OfertasModel>>() {
            @Override
            public void success(List<OfertasModel> listaDeOfertas, Response response) {
                progressBar.setVisibility(View.GONE);
                if (listaDeOfertas.size() == 0) {
                    txtNoOfertas.setVisibility(View.VISIBLE);
                } else {
                    for (OfertasModel ofertasInfo : listaDeOfertas) {
                        adaptadorOfertasList.addOferta(ofertasInfo);
                    }
                }
                if (swipeRefreshListSolic.isRefreshing()) {
                    swipeRefreshListSolic.setRefreshing(false);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressBar.setVisibility(View.GONE);
                if (error.getResponse() == null) {
                    if (recView.isAttachedToWindow()) {
                        Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(recView, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }
                if (swipeRefreshListSolic.isRefreshing()) {
                    swipeRefreshListSolic.setRefreshing(false);
                }
            }
        });

    }

}
