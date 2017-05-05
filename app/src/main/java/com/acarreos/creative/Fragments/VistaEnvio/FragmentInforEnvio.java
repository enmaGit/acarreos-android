package com.acarreos.creative.Fragments.VistaEnvio;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.EstatusEnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AddresLocationHandler;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;

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
 * Created by EnmanuelPc on 08/09/2015.
 */
public class FragmentInforEnvio extends Fragment {

    private EnvioModel envioInfo;

    public static int MAX_LENGTH_DIRECCION = 20;

    public static FragmentInforEnvio newInstance(EnvioModel envioInfo) {
        FragmentInforEnvio fragment = new FragmentInforEnvio();
        fragment.envioInfo = envioInfo;
        return fragment;
    }

    public FragmentInforEnvio() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_detalle_envio, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TextView textoFechaPub = (TextView) getView().findViewById(R.id.textFecha);
        TextView textStatus = (TextView) getView().findViewById(R.id.textEstatus);
        TextView transEnvio = (TextView) getView().findViewById(R.id.textTransEnvio);
        TextView txtOrigen = (TextView) getView().findViewById(R.id.txtDesde);
        TextView txtOrigenRef = (TextView) getView().findViewById(R.id.txtDesdeRef);
        TextView txtDestino = (TextView) getView().findViewById(R.id.txtHasta);
        TextView txtDestinoRef = (TextView) getView().findViewById(R.id.txtHastaRef);
        TextView txtRestanteParaPujar = (TextView) getView().findViewById(R.id.txtRestantePuja);
        TextView txtMaxDias = (TextView) getView().findViewById(R.id.txtDuracion);
        TextView clienteEnvio = (TextView) getView().findViewById(R.id.textClienteEnvio);
        TextView txtFechaSug = (TextView) getView().findViewById(R.id.txtFechaSug);

        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        UserModel userActual = sessionInfo.getUser();

        TextView btnCancelarEnvio = (TextView) getView().findViewById(R.id.btnCancelarEnvio);
        if (envioInfo.getEstatus().getId() == EstatusEnvioModel.ESTATUS_SUBASTA && (userActual.getId() == envioInfo.getCliente().getId())) {
            btnCancelarEnvio.setVisibility(View.VISIBLE);
            btnCancelarEnvio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actionClickCancelarEnvio();
                }
            });
        } else {
            btnCancelarEnvio.setVisibility(View.GONE);
        }
        clienteEnvio.setText(envioInfo.getCliente().getLogin());
        textoFechaPub.setText(envioInfo.getFecha_pub());
        textStatus.setText(envioInfo.getEstatus().getDescripcion());
        txtFechaSug.setText(envioInfo.getFechaSugerencia() + " (" + envioInfo.getHoraSugerencia().substring(0, 5) + ")");
        transEnvio.setText("Sin asignar");
        switch (envioInfo.getEstatus().getId()) {
            case ESTATUS_SUBASTA:
                textStatus.setBackgroundResource(R.drawable.square_corner_yellow);
                break;
            case ESTATUS_CANCELADO:
                textStatus.setBackgroundResource(R.drawable.square_corner_red);
                break;
            case ESTATUS_DESARROLLO:
                if (envioInfo.getGanador() != null) {
                    transEnvio.setText(envioInfo.getGanador().getTransportista().getLogin());
                    transEnvio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BaseActivity activity = (BaseActivity) getActivity();
                            activity.abrirCuentaTranspor(envioInfo.getGanador().getTransportista());
                        }
                    });
                }
                textStatus.setBackgroundResource(R.drawable.square_corner_green);
                break;
            case ESTATUS_FINALIZADO:
                if (envioInfo.getGanador() != null) {
                    transEnvio.setText(envioInfo.getGanador().getTransportista().getLogin());
                    transEnvio.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            BaseActivity activity = (BaseActivity) getActivity();
                            activity.abrirCuentaTranspor(envioInfo.getGanador().getTransportista());
                        }
                    });
                }
                textStatus.setBackgroundResource(R.drawable.square_corner_blue);
                break;
        }
        AddresLocationHandler locationHandlerOrigen = new AddresLocationHandler(txtOrigen);
        locationHandlerOrigen.getAddresFromLatLon(envioInfo.getOrigen(), true);
        txtOrigen.setSelected(true);
        AddresLocationHandler locationHandlerDestino = new AddresLocationHandler(txtDestino);
        locationHandlerDestino.getAddresFromLatLon(envioInfo.getDestino(), true);
        txtDestino.setSelected(true);
        txtOrigenRef.setText(envioInfo.getOrigenRef());
        txtOrigenRef.setSelected(true);
        txtDestinoRef.setText(envioInfo.getDestinoRef());
        txtDestinoRef.setSelected(true);
        txtMaxDias.setText(envioInfo.getMax_dias() + " dias");
        txtRestanteParaPujar.setText(envioInfo.getTiempoRestante());
    }

    private void actionClickCancelarEnvio() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        cancelarEnvio();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("¿Seguro desea cancelar el envío?").setPositiveButton("Sí", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    private void cancelarEnvio() {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        final SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        int idUsuario = sessionInfo.getUser().getId();

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
        envioInfo.setEstatusId(EstatusEnvioModel.ESTATUS_CANCELADO);

        final Context context = getActivity();
        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        servicioEnvio.modificarEnvio(idUsuario, envioInfo.getId(), envioInfo, new Callback<EnvioModel>() {
            @Override
            public void success(EnvioModel envioModel, Response response) {
                //TODO traer el estatus del envio desde el servidor
                dialogLoading.dismiss();
                if (getActivity() != null) {
                    BaseActivity activity = (BaseActivity) getActivity();
                    activity.abrirEnvios(sessionInfo.getUser());
                }
                Toast.makeText(context, "Envío cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
               /* Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());*/
                Toast.makeText(context, "Problemas de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
