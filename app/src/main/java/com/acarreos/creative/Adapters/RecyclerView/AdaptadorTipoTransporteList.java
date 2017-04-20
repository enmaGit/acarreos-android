package com.acarreos.creative.Adapters.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.acarreos.creative.Models.UserTransModel;
import com.acarreos.creative.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class AdaptadorTipoTransporteList extends RecyclerView.Adapter<AdaptadorTipoTransporteList.TransporteViewHolder> {

    ArrayList<UserTransModel> listaDeTransporte;

    Context context;

    public AdaptadorTipoTransporteList(Context context, @Nullable ArrayList<UserTransModel> listaDeTransporte) {
        if (listaDeTransporte == null) {
            listaDeTransporte = new ArrayList<>();
        }
        this.context = context;
        this.listaDeTransporte = listaDeTransporte;
    }

    public void addTransporte(UserTransModel userTransInfo) {
        listaDeTransporte.add(userTransInfo);
        notifyDataSetChanged();
    }

    public ArrayList<UserTransModel> getListData() {
        return listaDeTransporte;
    }

    @Override
    public TransporteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tipo_trans_item_list, parent, false);

        TransporteViewHolder transporteViewHolder = new TransporteViewHolder(itemView);
        return transporteViewHolder;
    }

    @Override
    public void onBindViewHolder(TransporteViewHolder holder, int position) {
        UserTransModel userTransInfo = listaDeTransporte.get(position);
        holder.bindEnvio(context, userTransInfo);
    }

    @Override
    public int getItemCount() {
        return listaDeTransporte.size();
    }

    public void vaciar() {
        listaDeTransporte = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class TransporteViewHolder extends RecyclerView.ViewHolder {

        TextView txtDescripcion;
        TextView btnVerDetalle;

        public TransporteViewHolder(View itemView) {
            super(itemView);
            txtDescripcion = (TextView) itemView.findViewById(R.id.txtDescripcion);
            btnVerDetalle = (TextView) itemView.findViewById(R.id.btnDetalle);
        }

        public void bindEnvio(final Context context, final UserTransModel userTransInfo) {
            txtDescripcion.setText(userTransInfo.getTransporte().getNombre());
            btnVerDetalle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirDialogDetalleTransporte(context, userTransInfo);
                }
            });
        }

        private void abrirDialogDetalleTransporte(Context context, UserTransModel userTransInfo) {
            final Dialog dialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_transporte);
            TextView txtTitle = (TextView) dialog.findViewById(R.id.title);
            TextView txtCondicion = (TextView) dialog.findViewById(R.id.textCondicion);
            TextView txtPlaca = (TextView) dialog.findViewById(R.id.textPlaca);
            TextView txtPolizaCompa = (TextView) dialog.findViewById(R.id.textPolizaCompa);
            TextView txtPolizaNumero = (TextView) dialog.findViewById(R.id.textPolizaNumero);
            TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
            TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
            ImageView imgTranspor = (ImageView) dialog.findViewById(R.id.imgFotoCarro);
            if (userTransInfo.getUrlFoto() != null) {
                if (userTransInfo.getUrlFoto().length() > 0) {
                    Picasso.with(context)
                            .load(userTransInfo.getUrlFoto())
                            .placeholder(R.drawable.img_categoria_car)
                            .error(R.drawable.img_categoria_car)
                            .skipMemoryCache()
                            .into(imgTranspor);
                }
            }
            dialog.findViewById(R.id.spinnerCategoria).setVisibility(View.GONE);
            btnGuardar.setVisibility(View.GONE);
            txtTitle.setText(userTransInfo.getTransporte().getNombre());
            txtCondicion.setText(userTransInfo.getCondicion() + "");
            txtCondicion.setEnabled(false);
            txtPlaca.setText(userTransInfo.getPlaca() + "");
            txtPlaca.setEnabled(false);
            txtPolizaCompa.setText(userTransInfo.getPolizaCompa() + "");
            txtPolizaCompa.setEnabled(false);
            txtPolizaNumero.setText(userTransInfo.getPolizaNumero() + "");
            txtPolizaNumero.setEnabled(false);
            btnCancelar.setText("Cerrar");
            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

}
