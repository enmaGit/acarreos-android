package com.acarreos.creative.Fragments.VistaEnvio;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorProductosList;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.CategoriaProductoModel;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.ProductoEnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.PeticionesWeb.ProductoPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import fr.ganfra.materialspinner.MaterialSpinner;
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
public class FragmentListaProductos extends Fragment {

    private RecyclerView recView;
    private CircularProgressView progressBar;
    private TextView txtNoProductos;
    private AdaptadorProductosList adaptadorProductosList;
    private RelativeLayout btnNuevoProducto;

    EnvioModel envioInfo;

    SwipeRefreshLayout swipeRefreshListSolic;

    public static FragmentListaProductos newInstance(EnvioModel envioInfo) {
        FragmentListaProductos fragment = new FragmentListaProductos();
        fragment.envioInfo = envioInfo;
        return fragment;
    }

    public void addProducto(ProductoEnvioModel productoInfo) {
        adaptadorProductosList.addProducto(productoInfo);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vistaInflada = inflater.inflate(R.layout.layout_productos_envio, null);
        return vistaInflada;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recView = (RecyclerView) getView().findViewById(R.id.recView);
        btnNuevoProducto = (RelativeLayout) getView().findViewById(R.id.btnAddProducto);
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        UserModel userActual = sessionInfo.getUser();
        if (userActual.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
            btnNuevoProducto.setVisibility(View.GONE);
        }
        if (envioInfo.getEstatus().getId() == EnvioModel.STATUS_SUBASTA) {
            btnNuevoProducto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    obtenerCategorias();
                }
            });
        } else {
            btnNuevoProducto.setVisibility(View.GONE);
        }
        progressBar = (CircularProgressView) getView().findViewById(R.id.progressBar);
        txtNoProductos = (TextView) getView().findViewById(R.id.txtNoProductos);
        recView.setHasFixedSize(true);
        swipeRefreshListSolic = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshSolicitudes);
        swipeRefreshListSolic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adaptadorProductosList.vaciar();
                obtenerProductos();
            }
        });
        adaptadorProductosList = new AdaptadorProductosList(getActivity(), null);
        adaptadorProductosList.setEnvioAsociado(envioInfo);
        adaptadorProductosList.setFragmentPadre(this);
        progressBar.setVisibility(View.VISIBLE);
        txtNoProductos.setVisibility(View.GONE);
        recView.setAdapter(adaptadorProductosList);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        obtenerProductos();
    }

    private void obtenerCategorias() {
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

        RestAdapter adapter = builder.build();
        ProductoPeticiones servicioProducto = adapter.create(ProductoPeticiones.class);
        servicioProducto.obtenerCategoriasProducto(new Callback<List<CategoriaProductoModel>>() {
            @Override
            public void success(List<CategoriaProductoModel> listaDeCategorias, Response response) {
                dialogLoading.dismiss();
                abrirDialogAddProducto(listaDeCategorias);
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                if (error.getResponse() == null) {
                    Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(recView, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }

            }
        });
    }

    public static final int REQUEST_IMAGE_CAPTURE_NUEVO_PRODUCTO = 1888;
    ImageView imgFotoProducto;

    public void capturarFotoParaDetalle(Dialog dialog) {
        imgFotoProducto = (ImageView) dialog.findViewById(R.id.btnImgProducto);
        imgFotoProducto.setTag("");
        imgFotoProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                }
                if (photoFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    imgFotoProducto.setTag(photoFile.getAbsolutePath());
                    getParentFragment().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_NUEVO_PRODUCTO);
                }*/
                abrigDialogChooseImg();
            }
        });
    }

    private void abrirDialogAddProducto(final List<CategoriaProductoModel> listaDeCategorias) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_producto);
        ArrayList<ProductoEnvioModel> listaCateActuales = adaptadorProductosList.getListaDeProductos();
        for (int i = 0; i < listaDeCategorias.size(); i++) {
            for (int j = 0; j < listaCateActuales.size(); j++) {
                if (listaDeCategorias.get(i).getId() == listaCateActuales.get(j).getCategoria().getId()) {
                    listaDeCategorias.remove(i);
                    i--;
                    break;
                }
            }
        }
        imgFotoProducto = (ImageView) dialog.findViewById(R.id.btnImgProducto);
        imgFotoProducto.setTag("");
        imgFotoProducto.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_a_photo_black_48dp));
        imgFotoProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrigDialogChooseImg();
            }
        });
        TextView txtTitle = (TextView) dialog.findViewById(R.id.title);
        txtTitle.setText("Nuevo Producto");
        final TextInputLayout tilLargo = (TextInputLayout) dialog.findViewById(R.id.tilLargo);
        final EditText txtLargo = (EditText) dialog.findViewById(R.id.textLargo);
        final TextInputLayout tilAlto = (TextInputLayout) dialog.findViewById(R.id.tilAlto);
        final EditText txtAlto = (EditText) dialog.findViewById(R.id.textAlto);
        final TextInputLayout tilAncho = (TextInputLayout) dialog.findViewById(R.id.tilAncho);
        final EditText txtAncho = (EditText) dialog.findViewById(R.id.textAncho);
        final TextInputLayout tilPeso = (TextInputLayout) dialog.findViewById(R.id.tilPeso);
        final EditText txtPeso = (EditText) dialog.findViewById(R.id.textPeso);
        final TextInputLayout tilCantidad = (TextInputLayout) dialog.findViewById(R.id.tilCantidad);
        final EditText txtCantidad = (EditText) dialog.findViewById(R.id.textCantidad);
        final TextInputLayout tilDescripcion = (TextInputLayout) dialog.findViewById(R.id.tilDescripcion);
        final EditText txtDescripcion = (EditText) dialog.findViewById(R.id.textDescripcion);
        TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
        TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
        final MaterialSpinner spinnerCategoria = (MaterialSpinner) dialog.findViewById(R.id.spinnerCategoria);
        spinnerCategoria.setPaddingSafe(0, 0, 0, 0);
        String[] arrayCategorias = new String[listaDeCategorias.size()];
        int i = 0;
        for (CategoriaProductoModel categoriaInfo : listaDeCategorias) {
            arrayCategorias[i] = categoriaInfo.getNombre();
            i++;
        }
        ArrayAdapter<String> adapterCategoriaList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayCategorias);
        adapterCategoriaList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapterCategoriaList);
        spinnerCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = true;
                if (txtAlto.getText().toString().trim().length() == 0) {
                    tilAlto.setErrorEnabled(true);
                    tilAlto.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtLargo.getText().toString().trim().length() == 0) {
                    tilLargo.setErrorEnabled(true);
                    tilLargo.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtAncho.getText().toString().trim().length() == 0) {
                    tilAncho.setErrorEnabled(true);
                    tilAncho.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtPeso.getText().toString().trim().length() == 0) {
                    tilPeso.setErrorEnabled(true);
                    tilPeso.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtCantidad.getText().toString().trim().length() == 0) {
                    tilCantidad.setErrorEnabled(true);
                    tilCantidad.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtDescripcion.getText().toString().trim().length() == 0) {
                    tilDescripcion.setErrorEnabled(true);
                    tilDescripcion.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (spinnerCategoria.getSelectedItemPosition() == 0) {
                    spinnerCategoria.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (valido) {
                    ProductoEnvioModel productoInfo = new ProductoEnvioModel();
                    productoInfo.setAlto(Float.valueOf(txtAlto.getText().toString()));
                    productoInfo.setLargo(Float.valueOf(txtLargo.getText().toString()));
                    productoInfo.setAncho(Float.valueOf(txtAncho.getText().toString()));
                    productoInfo.setPeso(Float.valueOf(txtPeso.getText().toString()));
                    productoInfo.setDescripcion(txtDescripcion.getText().toString().trim());
                    productoInfo.setCantidad(Integer.valueOf(txtCantidad.getText().toString()));
                    productoInfo.setId(listaDeCategorias.get(spinnerCategoria.getSelectedItemPosition() - 1).getId());
                    agregarProducto(productoInfo);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    private void abrigDialogChooseImg() {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_shoosing_img);
        RelativeLayout btnFromGallery = (RelativeLayout) dialog.findViewById(R.id.btnFromGallery);
        RelativeLayout btnTakePic = (RelativeLayout) dialog.findViewById(R.id.btnTakePic);
        btnFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                chooseImgGallery();
            }
        });
        btnTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                takeAPic();
            }
        });
        dialog.show();
    }

    private ImageChooserManager imageChooserManager;

    private void chooseImgGallery() {
        imageChooserManager = new ImageChooserManager(getParentFragment().getActivity(),
                ChooserType.REQUEST_PICK_PICTURE, true);
        imageChooserManager.setImageChooserListener(new ImageChooserListener() {
            @Override
            public void onImageChosen(final ChosenImage chosenImage) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        asignarPicFromGallery(chosenImage);
                    }
                });
            }

            @Override
            public void onError(String s) {

            }
        });
        imageChooserManager.clearOldFiles();
        try {
            BaseActivity activity = (BaseActivity) getParentFragment().getActivity();
            activity.setImageChooserManager(imageChooserManager);
            imageChooserManager.choose();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void asignarPicFromGallery(ChosenImage chosenImage) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        String imagePath = chosenImage.getFilePathOriginal();
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, options);
        Bitmap out;
        if (imageBitmap != null) {
            if (imageBitmap.getWidth() < imageBitmap.getHeight()) {
                out = Bitmap.createScaledBitmap(imageBitmap, 320, 480, false);
            } else if (imageBitmap.getWidth() > imageBitmap.getHeight()) {
                out = Bitmap.createScaledBitmap(imageBitmap, 480, 320, false);
            } else {
                out = Bitmap.createScaledBitmap(imageBitmap, 320, 320, false);
            }
            File file = new File(imagePath);
            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(file);
                out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (Exception e) {
            }
            imgFotoProducto.setTag(file.getAbsolutePath());
            imgFotoProducto.setImageBitmap(out);
            imgFotoProducto.setOnClickListener(null);
        } else {
            Toast.makeText(getActivity(), "Problemas de memoria al subir imagen, intente de nuevo", Toast.LENGTH_SHORT).show();
        }
    }

    private void takeAPic() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        if (photoFile != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(photoFile));
            imgFotoProducto.setTag(photoFile.getAbsolutePath());
            getParentFragment().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_NUEVO_PRODUCTO);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }

    private void agregarProducto(ProductoEnvioModel productoInfo) {
        /*final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();*/
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(3, TimeUnit.MINUTES);
        client.setReadTimeout(3, TimeUnit.MINUTES);
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

        TypedString tipoProductoId = new TypedString(Integer.toString(productoInfo.getId()));
        TypedString cantidad = new TypedString(productoInfo.getCantidad().toString());
        TypedString largo = new TypedString(productoInfo.getLargo().toString());
        TypedString alto = new TypedString(productoInfo.getAlto().toString());
        TypedString ancho = new TypedString(productoInfo.getAncho().toString());
        TypedString peso = new TypedString(productoInfo.getPeso().toString());

        TypedFile fotoProducto = null;
        if (imgFotoProducto.getTag() != "") {
            fotoProducto = new TypedFile("multipart/form-data", new File(imgFotoProducto.getTag().toString()));
        }

        final Context context = getActivity();
        Snackbar.make(recView, "El producto se publicara en breve", Snackbar.LENGTH_SHORT).show();
        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        servicioEnvio.agregarProductoEnvio(envioInfo.getId(),
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
                        //dialogLoading.dismiss();
                        Toast.makeText(context, "Producto publicado", Toast.LENGTH_SHORT).show();
                        adaptadorProductosList.addProducto(productoInfo);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        //dialogLoading.dismiss();
                        if (error.getResponse() == null) {
                            Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(recView, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                            Log.d("ERROR", error.toString());
                            Log.d("ERROR", error.getMessage());
                            Log.d("ERROR", error.getUrl());
                        }
                    }
                });
    }

    private void obtenerProductos() {
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
        servicioEnvio.obtenerProductosEnvio(envioInfo.getId(), new Callback<List<ProductoEnvioModel>>() {
            @Override
            public void success(List<ProductoEnvioModel> listaDeProductos, Response response) {
                progressBar.setVisibility(View.GONE);
                if (listaDeProductos.size() == 0) {
                    txtNoProductos.setVisibility(View.VISIBLE);
                } else {
                    for (ProductoEnvioModel productoInfo : listaDeProductos) {
                        adaptadorProductosList.addProducto(productoInfo);
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
                    if (recView.isAttachedToWindow()) {
                        Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                    }
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

    public void addDataFromCamara(Intent data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        String imagePath = imgFotoProducto.getTag().toString();
        Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, options);
        Bitmap out;
        if (imageBitmap.getWidth() < imageBitmap.getHeight()) {
            out = Bitmap.createScaledBitmap(imageBitmap, 320, 480, false);
        } else {
            out = Bitmap.createScaledBitmap(imageBitmap, 480, 320, false);
        }
        File file = new File(imagePath);
        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
        imgFotoProducto.setImageBitmap(out);
        imgFotoProducto.setOnClickListener(null);
    }
}
