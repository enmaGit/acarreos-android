package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.CategoriaProductoModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class FragmentSpam extends Fragment {

    private CollapsingToolbarLayout ctlLayout;
    private UserModel userInfo;

    private RelativeLayout btnSendSpam;

    private TextInputLayout tilMensajeSpam;
    private EditText textMensajeSpam;

    private List<CategoriaProductoModel> listaDeCategorias;


    public static FragmentSpam newInstance() {
        return new FragmentSpam();
    }

    public FragmentSpam() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_spam, container, false);
        return itemView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Fragment", "Actividad created");
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        userInfo = sessionInfo.getUser();

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle("Comisiones");

        btnSendSpam = (RelativeLayout) getView().findViewById(R.id.btnSendSpam);

        tilMensajeSpam = (TextInputLayout) getView().findViewById(R.id.tilMensajeSpam);
        textMensajeSpam = (EditText) getView().findViewById(R.id.textMensajeSpam);

        btnSendSpam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarMensajeSpam();
            }
        });
    }

    private void enviarMensajeSpam() {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        String mensaje = textMensajeSpam.getText().toString();

        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
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

        RestAdapter adapter = builder.build();
        UserPeticiones servicioUserSpam = adapter.create(UserPeticiones.class);

        servicioUserSpam.mandarSpam(mensaje, new Callback<UserModel>() {
            @Override
            public void success(UserModel userModel, Response response) {
                Snackbar.make(btnSendSpam, "Solicitud exitosa", Snackbar.LENGTH_LONG).show();
                dialogLoading.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());
                try {
                    if (error.getResponse().getBody() != null) {
                        Log.d("ERROR", convertStreamToString(error.getResponse().getBody().in()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Snackbar.make(btnSendSpam, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
