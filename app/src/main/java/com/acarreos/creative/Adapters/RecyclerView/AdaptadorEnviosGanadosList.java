package com.acarreos.creative.Adapters.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.dd.ShadowLayout;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import static com.acarreos.creative.Models.EstatusEnvioModel.ESTATUS_FINALIZADO;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class AdaptadorEnviosGanadosList extends RecyclerView.Adapter<AdaptadorEnviosGanadosList.EnvioViewHolder> {

    ArrayList<EnvioModel> listaDeEnvios;

    BaseActivity activityPadre;

    public AdaptadorEnviosGanadosList(Activity context, @Nullable ArrayList<EnvioModel> listaDeEnvios) {
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
                .inflate(R.layout.envio_win_item_list, parent, false);

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

        TextView txtLoginUser;
        TextView txtDescripcion;
        RatingBar ratingEnvio;
        ShadowLayout btnVerDetalles;
        ShadowLayout btnFinalizar;


        public EnvioViewHolder(View itemView) {
            super(itemView);
            txtDescripcion = (TextView) itemView.findViewById(R.id.txtDescripcion);
            txtLoginUser = (TextView) itemView.findViewById(R.id.txtLoginUser);
            ratingEnvio = (RatingBar) itemView.findViewById(R.id.ratingEnvio);
            btnVerDetalles = (ShadowLayout) itemView.findViewById(R.id.btnDetalle);
            btnFinalizar = (ShadowLayout) itemView.findViewById(R.id.btnFinalizar);
        }

        public void bindEnvio(final BaseActivity actividadPadre, final EnvioModel envioInfo) {
            txtDescripcion.setText(envioInfo.getTitle());
            txtLoginUser.setText(envioInfo.getCliente().getLogin());
            ratingEnvio.setRating(0.0f);
            if (envioInfo.getValoracion() != null) {
                ratingEnvio.setRating(envioInfo.getValoracion());
            }
            btnVerDetalles.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    actividadPadre.abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_UBICACION, false);
                }
            });
            btnFinalizar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalizarEnvio(actividadPadre, envioInfo);
                }
            });
            if (envioInfo.getEstatus().getId() == ESTATUS_FINALIZADO) {
                btnFinalizar.setVisibility(View.GONE);
            }
        }

        private void finalizarEnvio(BaseActivity actividadPadre, EnvioModel envioInfo) {
            envioInfo.setEstatusId(EnvioModel.STATUS_FINALIZADO);
            final Dialog dialogLoading = new Dialog(actividadPadre, R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();
            SessionModel sessionInfo = new ReminderSession(actividadPadre).obtenerInfoSession();
            final String token = sessionInfo.getToken();
            int idUsuario = sessionInfo.getUser().getId();

            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new AuthRequestInterceptor((BaseActivity) actividadPadre));

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
            servicioEnvio.modificarEnvio(envioInfo.getCliente().getId(), envioInfo.getId(), envioInfo, new Callback<EnvioModel>() {
                @Override
                public void success(EnvioModel envioModel, Response response) {
                    //TODO traer el estatus del envio desde el servidor
                    Snackbar.make(btnFinalizar, "Se ha notificado al cliente", Snackbar.LENGTH_LONG).show();
                    dialogLoading.dismiss();
                }

                @Override
                public void failure(RetrofitError error) {
                    dialogLoading.dismiss();
                    Log.d("ERROR", error.toString());
                    //Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                    Snackbar.make(btnFinalizar, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
                }
            });
        }

    }

}
