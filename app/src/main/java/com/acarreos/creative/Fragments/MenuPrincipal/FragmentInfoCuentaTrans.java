package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;
import com.google.gson.Gson;

import java.io.IOException;

import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 06/09/2015.
 */
public class FragmentInfoCuentaTrans extends Fragment {

    public UserModel userInfo;

    private TextView textFecNac;
    private TextView textLogin;
    private TextView textEmail;
    private TextView textTelefono;
    private TextView textDni;
    private TextView textNumSeguridad;
    private TextView textTipoLicencia;

    public static FragmentInfoCuentaTrans newInstance(UserModel userInfo) {
        FragmentInfoCuentaTrans fragmentInfoCuentaTrans = new FragmentInfoCuentaTrans();
        fragmentInfoCuentaTrans.userInfo = userInfo;
        return fragmentInfoCuentaTrans;
    }

    public FragmentInfoCuentaTrans() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_cuenta_trans, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        RatingBar ratingBar = (RatingBar) getView().findViewById(R.id.ratingTranspor);
        TextView txtEnviosRealizados = (TextView) getView().findViewById(R.id.txtEnviosRealizados);
        textFecNac = (TextView) getView().findViewById(R.id.textFecNac);
        textLogin = (TextView) getView().findViewById(R.id.textLogin);
        textEmail = (TextView) getView().findViewById(R.id.textEmail);
        textTelefono = (TextView) getView().findViewById(R.id.textTelefono);
        textDni = (TextView) getView().findViewById(R.id.textDni);
        textNumSeguridad = (TextView) getView().findViewById(R.id.textNumSeguridad);
        textTipoLicencia = (TextView) getView().findViewById(R.id.textTipoLicencia);
        ratingBar.setRating(userInfo.getValoracion());
        txtEnviosRealizados.setText(userInfo.getEnviosRealizados() + " Envíos completados");
        textFecNac.setText(userInfo.getFecha_nac());
        textLogin.setText(userInfo.getLogin());
        textEmail.setText(userInfo.getEmail());
        textTelefono.setText(userInfo.getTelefono());
        switch (userInfo.getTipoDni()) {
            case UserModel.TIPO_CEDULA:
                textDni.setText("Cédula: " + userInfo.getDni());
                break;
            case UserModel.TIPO_PASAPORTE:
                textDni.setText("N° Pasaporte: " + userInfo.getDni());
                break;
            case UserModel.TIPO_REGISTRO_UNICO_CONTRIBUYENTE:
                textDni.setText("RUC: " + userInfo.getDni());
                break;
            case UserModel.TIPO_CODIGO_ENTIDAD_PUBLICA:
                textDni.setText("Código de Entidad Pública: " + userInfo.getDni());
                break;
        }
        textNumSeguridad.setText("Num. Seguridad: " + userInfo.getNumSeguridad());
        textTipoLicencia.setText("Licencia Tipo: (" + userInfo.getTipoLicencia() + ")");
        UserModel userActual = new ReminderSession(getActivity()).obtenerInfoSession().getUser();
        if (userActual.getId() != userInfo.getId()) {
            textEmail.setVisibility(View.GONE);
            textTelefono.setVisibility(View.GONE);
            textDni.setVisibility(View.GONE);
            textNumSeguridad.setVisibility(View.GONE);
        }
    }

    public void abrirDialogActDatos() {
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
        textInputTelefono.setErrorEnabled(true);
        textNombre.setText(userInfo.getNombre());
        textApellido.setText(userInfo.getApellido());
        textEmail.setText(userInfo.getEmail());
        textTelefono.setText(userInfo.getTelefono());
        ImageView btnClose = (ImageView) dialog.findViewById(R.id.btnClose);
        TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
        final MaterialSpinner spinnerTipoLicencia = (MaterialSpinner) dialog.findViewById(R.id.spinnerTipoLicencia);
        spinnerTipoLicencia.setPaddingSafe(0, 0, 0, 0);
        String[] arrayTipoLicencia = new String[12];
        arrayTipoLicencia[0] = "A";
        arrayTipoLicencia[1] = "B";
        arrayTipoLicencia[2] = "C";
        arrayTipoLicencia[3] = "D";
        arrayTipoLicencia[4] = "E";
        arrayTipoLicencia[5] = "E1 a";
        arrayTipoLicencia[6] = "E2 b";
        arrayTipoLicencia[7] = "E3 c";
        arrayTipoLicencia[8] = "F";
        arrayTipoLicencia[9] = "G";
        arrayTipoLicencia[10] = "H";
        arrayTipoLicencia[11] = "I";
        ArrayAdapter<String> adapterTipoLicenciaList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayTipoLicencia);
        adapterTipoLicenciaList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoLicencia.setAdapter(adapterTipoLicenciaList);
        spinnerTipoLicencia.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == -1) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getActivity().getResources().getColor(R.color.gray_text));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        int index = 0;
        for (String tipoLicencia : arrayTipoLicencia) {
            index++;
            if (tipoLicencia.compareTo(userInfo.getTipoLicencia()) == 0) {
                spinnerTipoLicencia.setSelection(index);
                break;
            }
        }
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
                    userInfo.setNombre(textNombre.getText().toString().trim());
                    userInfo.setApellido(textApellido.getText().toString().trim());
                    userInfo.setEmail(textEmail.getText().toString().trim());
                    userInfo.setTelefono(textTelefono.getText().toString().trim());
                    userInfo.setTipoLicencia(((String) spinnerTipoLicencia.getSelectedItem()).trim());
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
        UserPeticiones servicioActDatos = restAdapter.create(UserPeticiones.class);


        servicioActDatos.actualizarDatos(userActual.getId(), userInfo, new Callback<SessionModel>() {
            @Override
            public void success(SessionModel sessionInfo, Response response) {
                dialog.dismiss();
                Log.d("REVISAR send", new Gson().toJson(userInfo));
                UserModel userUpdate = sessionInfo.getUser();
                Log.d("REVISAR get", new Gson().toJson(userUpdate));
                ReminderSession reminderSession = new ReminderSession(getActivity());
                reminderSession.updateUser(userUpdate);
                Snackbar.make(textFecNac, "Datos Guardados con Éxito", Snackbar.LENGTH_SHORT).show();
                textFecNac.setText(userUpdate.getFecha_nac());
                textLogin.setText(userUpdate.getLogin());
                textEmail.setText(userUpdate.getEmail());
                textTelefono.setText(userUpdate.getTelefono());
                textTipoLicencia.setText("Licencia Tipo: (" + userUpdate.getTipoLicencia() + ")");
                FragmentCuentaTranspor fragmentCuentaTranspor = (FragmentCuentaTranspor) getParentFragment();
                fragmentCuentaTranspor.actualizarToolbar();
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
                Snackbar.make(textFecNac, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
