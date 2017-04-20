package com.acarreos.creative.Adapters.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Fragments.VistaEnvio.FragmentListaProductos;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.ProductoEnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class AdaptadorProductosList extends RecyclerView.Adapter<AdaptadorProductosList.ProductoViewHolder> {

    ArrayList<ProductoEnvioModel> listaDeProducto;

    Context context;

    EnvioModel envioAsociado;

    FragmentListaProductos fragmentPadre;

    public AdaptadorProductosList(Context context, @Nullable ArrayList<ProductoEnvioModel> listaDeProducto) {
        if (listaDeProducto == null) {
            listaDeProducto = new ArrayList<>();
        }
        this.context = context;
        this.listaDeProducto = listaDeProducto;
    }

    public void setFragmentPadre(FragmentListaProductos fragmentPadre) {
        this.fragmentPadre = fragmentPadre;
    }

    public void setEnvioAsociado(EnvioModel envioAsociado) {
        this.envioAsociado = envioAsociado;
    }

    public void addProducto(ProductoEnvioModel productoInfo) {
        listaDeProducto.add(productoInfo);
        notifyDataSetChanged();
    }

    public ArrayList<ProductoEnvioModel> getListaDeProductos() {
        return listaDeProducto;
    }

    @Override
    public ProductoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.producto_item_list, parent, false);

        ProductoViewHolder productoViewHolder = new ProductoViewHolder(itemView);
        return productoViewHolder;
    }

    @Override
    public void onBindViewHolder(ProductoViewHolder holder, int position) {
        ProductoEnvioModel productoInfo = listaDeProducto.get(position);
        holder.bindProducto(context, productoInfo, envioAsociado, fragmentPadre);
    }

    @Override
    public int getItemCount() {
        return listaDeProducto.size();
    }

    public void vaciar() {
        listaDeProducto = new ArrayList<>();
        notifyDataSetChanged();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {

        TextView txtCategoria;
        TextView btnVerDetalle;
        Context context;
        EnvioModel envioAsociado;

        public ProductoViewHolder(View itemView) {
            super(itemView);
            txtCategoria = (TextView) itemView.findViewById(R.id.txtCategoria);
            btnVerDetalle = (TextView) itemView.findViewById(R.id.btnDetalle);
        }

        public void bindProducto(Context contextP, final ProductoEnvioModel productoInfo, final EnvioModel envioAsociado, final FragmentListaProductos fragmentPadre) {
            txtCategoria.setText(productoInfo.getCategoria().getNombre());
            this.context = contextP;
            this.envioAsociado = envioAsociado;
            if (envioAsociado != null) {
                UserModel userLogged = new ReminderSession(context).obtenerInfoSession().getUser();
                if (productoInfo.getUrlFoto() == null || productoInfo.getUrlFoto().length() == 0) {
                    if (userLogged.getLogin().compareTo(envioAsociado.getCliente().getLogin()) == 0) {
                        btnVerDetalle.setText("+ Foto");
                    }
                }
            }

            btnVerDetalle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    abrirDialogDetalleProducto(context, productoInfo, fragmentPadre);
                }
            });

        }

        public static final int REQUEST_IMAGE_CAPTURE_DETALLE_PRODUCTO = 1999;
        ImageView imgFotoProducto;

        private void abrirDialogDetalleProducto(final Context context, final ProductoEnvioModel productoInfo, FragmentListaProductos fragmentPadre) {
            final Dialog dialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_producto);
            TextView txtTitle = (TextView) dialog.findViewById(R.id.title);
            TextView txtDescripcion = (TextView) dialog.findViewById(R.id.textDescripcion);
            TextView txtLargo = (TextView) dialog.findViewById(R.id.textLargo);
            TextView txtAlto = (TextView) dialog.findViewById(R.id.textAlto);
            TextView txtAncho = (TextView) dialog.findViewById(R.id.textAncho);
            TextView txtPeso = (TextView) dialog.findViewById(R.id.textPeso);
            TextView txtCantidad = (TextView) dialog.findViewById(R.id.textCantidad);
            TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
            TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
            dialog.findViewById(R.id.spinnerCategoria).setVisibility(View.GONE);
            imgFotoProducto = (ImageView) dialog.findViewById(R.id.btnImgProducto);
            UserModel userLogged = new ReminderSession(context).obtenerInfoSession().getUser();
            if (productoInfo.getUrlFoto() != null) {
                if (productoInfo.getUrlFoto().length() > 0) {
                    Picasso.with(context)
                            .load(productoInfo.getUrlFoto())
                            .placeholder(R.drawable.img_leyenda_produc_dimens)
                            .error(R.drawable.img_leyenda_produc_dimens)
                            .into(imgFotoProducto);
                }
            } else {
                if (envioAsociado != null) {
                    if (userLogged.getId() == envioAsociado.getCliente().getId()) {
                        imgFotoProducto.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_add_a_photo_black_48dp));
                    }
                }
            }
            ImageView btnInfoImgProduct = (ImageView) dialog.findViewById(R.id.btnInfoImgProduct);
            btnInfoImgProduct.setOnTouchListener(new View.OnTouchListener() {
                                                     @Override
                                                     public boolean onTouch(View view, MotionEvent motionEvent) {
                                                         if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                                                             imgFotoProducto.setImageResource(R.drawable.img_leyenda_produc_dimens);
                                                         } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                                                             Picasso.with(context)
                                                                     .load(productoInfo.getUrlFoto())
                                                                     .placeholder(R.drawable.img_leyenda_produc_dimens)
                                                                     .error(R.drawable.img_leyenda_produc_dimens)
                                                                     .into(imgFotoProducto);
                                                         }
                                                         return true;
                                                     }
                                                 }

            );
            txtTitle.setText(productoInfo.getCategoria().getNombre());
            txtLargo.setText(productoInfo.getLargo() + "");
            txtLargo.setEnabled(false);
            txtAlto.setText(productoInfo.getAlto() + "");
            txtAlto.setEnabled(false);
            txtAncho.setText(productoInfo.getAncho() + "");
            txtAncho.setEnabled(false);
            txtPeso.setText(productoInfo.getPeso() + "");
            txtPeso.setEnabled(false);
            txtCantidad.setText(productoInfo.getCantidad() + "");
            txtCantidad.setEnabled(false);
            txtDescripcion.setText("Sin descripcion");
            if (productoInfo.getDescripcion() != null) {
                if (productoInfo.getDescripcion().length() > 0) {
                    txtDescripcion.setText(productoInfo.getDescripcion());
                }
            }
            txtDescripcion.setEnabled(false);
            if (envioAsociado != null) {
                if (userLogged.getId() == envioAsociado.getCliente().getId()) {
                    btnGuardar.setText("Guardar foto");
                    btnGuardar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                            agregarProducto(productoInfo);
                        }
                    });
                } else {
                    btnGuardar.setVisibility(View.GONE);
                    btnCancelar.setText("Cerrar");
                }
            }

            btnCancelar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            if (envioAsociado != null) {
                if (userLogged.getId() == envioAsociado.getCliente().getId()) {
                    fragmentPadre.capturarFotoParaDetalle(dialog);
                }
            }
            dialog.show();
        }

        private void agregarProducto(ProductoEnvioModel productoInfo) {
            /*final Dialog dialogLoading = new Dialog(context, R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();*/
            SessionModel sessionInfo = new ReminderSession(context).obtenerInfoSession();
            final String token = sessionInfo.getToken();

            OkHttpClient client = new OkHttpClient();
            client.setConnectTimeout(3, TimeUnit.MINUTES);
            client.setReadTimeout(3, TimeUnit.MINUTES);
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

            TypedFile fotoProducto = null;
            if (imgFotoProducto.getTag() != "") {
                fotoProducto = new TypedFile("multipart/form-data", new File(imgFotoProducto.getTag().toString()));

                TypedString tipoProductoId = new TypedString(Integer.toString(productoInfo.getId()));
                TypedString cantidad = new TypedString(productoInfo.getCantidad().toString());
                TypedString largo = new TypedString(productoInfo.getLargo().toString());
                TypedString alto = new TypedString(productoInfo.getAlto().toString());
                TypedString ancho = new TypedString(productoInfo.getAncho().toString());
                TypedString peso = new TypedString(productoInfo.getPeso().toString());

                Toast.makeText(context, "Los cambios se publicaran en breve", Toast.LENGTH_SHORT).show();
                RestAdapter adapter = builder.build();
                EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
                servicioEnvio.agregarProductoEnvio(envioAsociado.getId(),
                        tipoProductoId,
                        cantidad,
                        largo,
                        alto,
                        ancho,
                        peso,
                        fotoProducto,
                        new Callback<ProductoEnvioModel>() {
                            @Override
                            public void success(ProductoEnvioModel productoInfo, Response response) {
                                if (btnVerDetalle != null) {
                                    btnVerDetalle.setText("Ver Detalles");
                                }
                                Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show();
                                //dialogLoading.dismiss();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                //dialogLoading.dismiss();
                                if (error.getResponse() == null) {
                                    Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Todo grave " + error.toString(), Toast.LENGTH_SHORT).show();
                                    Log.d("ERROR", error.toString());
                                    Log.d("ERROR", error.getMessage());
                                    Log.d("ERROR", error.getUrl());
                                }
                            }
                        });
            } else {
                Toast.makeText(context, "Debe capturar una nueva foto", Toast.LENGTH_SHORT).show();
            }
        }

    }

}
