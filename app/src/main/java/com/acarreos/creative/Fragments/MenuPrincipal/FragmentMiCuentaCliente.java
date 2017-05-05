package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;

import java.io.IOException;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 06/09/2015.
 */
public class FragmentMiCuentaCliente extends Fragment {

    private ViewGroup container;
    private UserModel userInfo;
    private Toolbar toolbar;
    private FloatingActionButton btnActCuenta;

    private TextView textFecNac;
    private TextView textLogin;
    private TextView textEmail;
    private TextView textTelefono;

    public static FragmentMiCuentaCliente newInstance() {
        return new FragmentMiCuentaCliente();
    }

    public FragmentMiCuentaCliente() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        return inflater.inflate(R.layout.fragment_mi_cuenta_cli, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        btnActCuenta = (FloatingActionButton) getView().findViewById(R.id.btnActDatos);
        btnActCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogActDatos();
            }
        });

        userInfo = new ReminderSession(getActivity()).obtenerInfoSession().getUser();
        toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(userInfo.getNombre() + " " + userInfo.getApellido());
        toolbar.setContentInsetsAbsolute(0, 0);

        textFecNac = (TextView) getView().findViewById(R.id.textFecNac);
        textLogin = (TextView) getView().findViewById(R.id.textLogin);
        textEmail = (TextView) getView().findViewById(R.id.textEmail);
        textTelefono = (TextView) getView().findViewById(R.id.textTelefono);
        textFecNac.setText(userInfo.getFecha_nac());
        textLogin.setText(userInfo.getLogin());
        textEmail.setText(userInfo.getEmail());
        textTelefono.setText(userInfo.getTelefono());
    }

    private void abrirDialogActDatos() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Light);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_act_usuario);
        final EditText textNombre = (EditText) dialog.findViewById(R.id.textNombre);
        final TextInputLayout textInputNombre = (TextInputLayout) dialog.findViewById(R.id.tilNombre);
        textInputNombre.setErrorEnabled(true);
        final EditText textApellido = (EditText) dialog.findViewById(R.id.textApellido);
        final TextInputLayout textInputApellido = (TextInputLayout) dialog.findViewById(R.id.tilApellido);
        textInputApellido.setErrorEnabled(true);
        final EditText textEmail = (EditText) dialog.findViewById(R.id.textEmail);
        final TextInputLayout textInputEmail = (TextInputLayout) dialog.findViewById(R.id.tilEmail);
        textInputEmail.setErrorEnabled(true);
        final EditText textTelefono = (EditText) dialog.findViewById(R.id.textTelefono);
        final TextInputLayout textInputTelefono = (TextInputLayout) dialog.findViewById(R.id.tilTelefono);
        dialog.findViewById(R.id.conteTipoLicencia).setVisibility(View.GONE);
        textInputTelefono.setErrorEnabled(true);
        textNombre.setText(userInfo.getNombre());
        textApellido.setText(userInfo.getApellido());
        textEmail.setText(userInfo.getEmail());
        textTelefono.setText(userInfo.getTelefono());
        ImageView btnClose = (ImageView) dialog.findViewById(R.id.btnClose);
        TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textInputNombre.setError(null);
                textInputApellido.setError(null);
                textInputEmail.setError(null);
                textTelefono.setError(null);
                if (textNombre.getText().toString().length() == 0) {
                    textInputNombre.setError("Este campo es obligatorio");
                } else if (textApellido.getText().toString().length() == 0) {
                    textInputApellido.setError("Este campo es obligatorio");
                } else if (textEmail.getText().toString().length() == 0) {
                    textInputEmail.setError("Este campo es obligatorio");
                } else if (textTelefono.getText().toString().length() == 0) {
                    textTelefono.setError("Este campo es obligatorio");
                } else {
                    UserModel userInfo = new UserModel();
                    userInfo.setNombre(textNombre.getText().toString());
                    userInfo.setApellido(textApellido.getText().toString());
                    userInfo.setEmail(textEmail.getText().toString());
                    userInfo.setTelefono(textTelefono.getText().toString());
                    actualizarDatosCuenta(userInfo);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void actualizarDatosCuenta(final UserModel userInfo) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_loading);
        dialog.setCancelable(false);
        dialog.show();
        UserModel userActual = new ReminderSession(getActivity()).obtenerInfoSession().getUser();
        userInfo.setId(userActual.getId());
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestFacade request) {
                        request.addQueryParam("token", new ReminderSession(getActivity()).obtenerInfoSession().getToken());
                    }
                })
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .build();
        final UserPeticiones servicioActDatos = restAdapter.create(UserPeticiones.class);

        servicioActDatos.actualizarDatos(userActual.getId(), userInfo, new Callback<SessionModel>() {
            @Override
            public void success(SessionModel sessionInfo, Response response) {
                dialog.dismiss();
                UserModel userInfo = sessionInfo.getUser();
                ReminderSession reminderSession = new ReminderSession(getActivity());
                reminderSession.updateUser(userInfo);
                Snackbar.make(btnActCuenta, "Datos Guardados con Éxito", Snackbar.LENGTH_SHORT).show();
                textFecNac.setText(userInfo.getFecha_nac());
                textLogin.setText(userInfo.getLogin());
                textEmail.setText(userInfo.getEmail());
                textTelefono.setText(userInfo.getTelefono());
                toolbar.setTitle(userInfo.getNombre() + " " + userInfo.getApellido());
            }

            @Override
            public void failure(RetrofitError error) {
                dialog.dismiss();
                Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());
                try {
                    Log.d("ERROR", convertStreamToString(error.getResponse().getBody().in()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Snackbar.make(btnActCuenta, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
