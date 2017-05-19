package com.acarreos.creative.Adapters.RecyclerView;

import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
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
import com.dd.ShadowLayout;
import com.squareup.okhttp.OkHttpClient;
import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class AdaptadorOfertasList extends RecyclerView.Adapter<AdaptadorOfertasList.OfertaViewHolder> {

    ArrayList<OfertasModel> listaDeOfertas;

    BaseActivity context;

    EnvioModel envioInfo;

    public AdaptadorOfertasList(BaseActivity context, @Nullable ArrayList<OfertasModel> listaDeOfertas, EnvioModel envioInfo) {
        if (listaDeOfertas == null) {
            listaDeOfertas = new ArrayList<>();
        }
        this.envioInfo = envioInfo;
        this.context = context;
        this.listaDeOfertas = listaDeOfertas;
    }

    public void addOferta(OfertasModel ofertaInfo) {
        listaDeOfertas.add(ofertaInfo);
        notifyDataSetChanged();
    }

    @Override
    public OfertaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.oferta_item_list, parent, false);

        OfertaViewHolder ofertaViewHolder = new OfertaViewHolder(itemView);
        return ofertaViewHolder;
    }

    @Override
    public void onBindViewHolder(OfertaViewHolder holder, int position) {
        OfertasModel ofertaInfo = listaDeOfertas.get(position);
        holder.bindOferta(context, ofertaInfo, envioInfo);
    }

    @Override
    public int getItemCount() {
        return listaDeOfertas.size();
    }

    public void vaciar() {
        listaDeOfertas = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class OfertaViewHolder extends RecyclerView.ViewHolder {

        private static final String STRIPE_API_KEY = "pk_test_QGB4qqY7Qz3nIxae1pO9Vr7T";
        TextView txtTransLogin;
        ShadowLayout btnAceptarOferta;
        TextView fechaPuja;
        TextView precioPuja;
        TextView fechaSalida;
        TextView fechaLlegada;
        TextView horaSalida;
        TextView horaLlegada;

        public OfertaViewHolder(View itemView) {
            super(itemView);
            txtTransLogin = (TextView) itemView.findViewById(R.id.txtLoginTrans);
            btnAceptarOferta = (ShadowLayout) itemView.findViewById(R.id.btnAceptarOferta);
            fechaPuja = (TextView) itemView.findViewById(R.id.textFechaPub);
            precioPuja = (TextView) itemView.findViewById(R.id.textPrecioPuja);
            fechaSalida = (TextView) itemView.findViewById(R.id.txtFechaSalida);
            fechaLlegada = (TextView) itemView.findViewById(R.id.txtFechaLlegada);
            horaSalida = (TextView) itemView.findViewById(R.id.horaSalida);
            horaLlegada = (TextView) itemView.findViewById(R.id.horaLlegada);
        }

        public void bindOferta(final BaseActivity context, final OfertasModel ofertaInfo, final EnvioModel envioInfo) {
            txtTransLogin.setText(ofertaInfo.getTransportista().getLogin());
            txtTransLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.abrirCuentaTranspor(ofertaInfo.getTransportista());
                }
            });
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaPujaDate = null;
            Date fechaSalidaDate = null;
            Date fechaLlegadaDate = null;
            try {
                fechaPujaDate = dateFormat.parse(ofertaInfo.getFechaPuja());
                fechaSalidaDate = dateFormat.parse(ofertaInfo.getFechaSalida());
                fechaLlegadaDate = dateFormat.parse(ofertaInfo.getFechaLlegada());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            fechaPuja.setText(android.text.format.DateFormat.format("dd-MM-yyyy", fechaPujaDate));
            fechaSalida.setText(android.text.format.DateFormat.format("dd-MM-yyyy", fechaSalidaDate));
            fechaLlegada.setText(android.text.format.DateFormat.format("dd-MM-yyyy", fechaLlegadaDate));
            precioPuja.setText(String.format("%,.2f", ofertaInfo.getPrecioPuja()) + "$");
            horaLlegada.setText(ofertaInfo.getHoraLlegada());
            SessionModel sessionInfo = new ReminderSession(context).obtenerInfoSession();
            UserModel userActual = sessionInfo.getUser();
            if (envioInfo.getCliente().getId() == userActual.getId() && envioInfo.getEstatus().getId() == EstatusEnvioModel.ESTATUS_SUBASTA) {
                btnAceptarOferta.setVisibility(View.VISIBLE);
                btnAceptarOferta.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        guardarInformacionPago(context, ofertaInfo, envioInfo);
                    }
                });
            }
        }

        private void guardarInformacionPago(final BaseActivity context, final OfertasModel ofertaInfo, EnvioModel envioInfo) {
            final Dialog dialog = new Dialog(context, android.R.style.Theme_Light);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_payment_infor);
            TextView txtPrecioEnvio = (TextView) dialog.findViewById(R.id.precioEnvio);
            TextView txtComisionEnvio = (TextView) dialog.findViewById(R.id.comisionEnvio);
            TextView txtTotalPrice = (TextView) dialog.findViewById(R.id.totalPrice);
            float comision = envioInfo.getComisionFinal() / 100f;
            float added = ofertaInfo.getPrecioPuja() * comision;
            float totalPrice = ofertaInfo.getPrecioPuja() + added;
            txtPrecioEnvio.setText(ofertaInfo.getPrecioPuja() + "$");
            txtComisionEnvio.setText(added + "$");
            txtTotalPrice.setText(totalPrice + "$");
            final TextInputLayout textInputNombre = (TextInputLayout) dialog.findViewById(R.id.tilNombre);
            textInputNombre.setErrorEnabled(true);
            final EditText editNombre = (EditText) dialog.findViewById(R.id.textNombre);
            final TextInputLayout textInputAddress = (TextInputLayout) dialog.findViewById(R.id.tilAddress);
            textInputAddress.setErrorEnabled(true);
            final EditText editAddress = (EditText) dialog.findViewById(R.id.textAddress);
            final TextInputLayout textInputAddress2 = (TextInputLayout) dialog.findViewById(R.id.tilAddress2);
            textInputAddress2.setErrorEnabled(true);
            final EditText editAddress2 = (EditText) dialog.findViewById(R.id.textAddress2);
            final TextInputLayout textInputZipCode = (TextInputLayout) dialog.findViewById(R.id.tilZipCode);
            textInputZipCode.setErrorEnabled(true);
            final EditText editZipCode = (EditText) dialog.findViewById(R.id.textZipCode);
            TextView btnProcesar = (TextView) dialog.findViewById(R.id.btnProcesor);
            btnProcesar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CardInputWidget mCardInputWidget = (CardInputWidget) dialog.findViewById(R.id.card_input_widget);
                    Card cardToSave = mCardInputWidget.getCard();
                    if (cardToSave == null) {
                        Toast.makeText(context, "Invalid Card Data", Toast.LENGTH_SHORT).show();
                    } else {
                        cardToSave.setName(editNombre.getText().toString().trim());
                        cardToSave.setAddressLine1(editAddress.getText().toString().trim());
                        cardToSave.setAddressLine2(editAddress2.getText().toString().trim());
                        cardToSave.setAddressZip(editZipCode.getText().toString().trim());
                        if (cardToSave.validateCard()) {
                            Toast.makeText(context, "Todo paso bien", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            obtenerTokenStripe(context, ofertaInfo, cardToSave);
                        } else {
                            Toast.makeText(context, "Invalid Card Data 2", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            dialog.show();
        }

        private void obtenerTokenStripe(final BaseActivity context, final OfertasModel ofertaInfo, Card cardToSave) {
            final Dialog dialogLoading = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();
            Stripe stripe = new Stripe(context, STRIPE_API_KEY);
            stripe.createToken(cardToSave, new TokenCallback() {
                @Override
                public void onError(Exception error) {
                    Toast.makeText(context, "Problemas con Stripe", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onSuccess(Token token) {
                    Toast.makeText(context, "Este es el token: " + token, Toast.LENGTH_SHORT).show();
                    aceptarOfertaGanadora(context, ofertaInfo, token, dialogLoading);
                }
            });
        }

        private void aceptarOfertaGanadora(BaseActivity context, final OfertasModel ofertaInfo, Token stripeToken, final Dialog dialogLoading) {
            SessionModel sessionInfo = new ReminderSession(context).obtenerInfoSession();
            final String token = sessionInfo.getToken();

            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(60, TimeUnit.SECONDS);
            client.setReadTimeout(60, TimeUnit.SECONDS);
            client.interceptors().add(new AuthRequestInterceptor((BaseActivity) context));

            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(UrlsServer.RUTA_SERVER)
                    .setClient(new OkClient(client));

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("token", token);
                    request.addQueryParam("oferta_id", ofertaInfo.id + "");
                }
            });

            builder.setLogLevel(RestAdapter.LogLevel.FULL).setLog(new RestAdapter.Log() {
                public void log(String msg) {
                    Log.i("retrofit", msg);
                }
            });

            RestAdapter adapter = builder.build();
            EnvioPeticiones servicioEnvios = adapter.create(EnvioPeticiones.class);
            servicioEnvios.definirEnvioGanador(ofertaInfo.idEnvio, stripeToken, new Callback<Object>() {

                @Override
                public void success(Object o, Response response) {
                    dialogLoading.dismiss();
                    Snackbar.make(txtTransLogin, "Oferta Aceptada", Snackbar.LENGTH_SHORT).show();
                    btnAceptarOferta.setVisibility(View.GONE);
                }

                @Override
                public void failure(RetrofitError error) {
                    dialogLoading.dismiss();
                    if (error.getResponse() == null) {
                        Snackbar.make(txtTransLogin, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(txtTransLogin, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                        Log.d("ERROR", error.toString());
                        Log.d("ERROR", error.getMessage());
                        Log.d("ERROR", error.getUrl());
                    }

                }
            });
        }

    }

}
