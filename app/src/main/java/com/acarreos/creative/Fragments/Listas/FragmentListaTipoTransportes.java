package com.acarreos.creative.Fragments.Listas;

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
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorTipoTransporteList;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.TipoTransModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.Models.UserTransModel;
import com.acarreos.creative.PeticionesWeb.TransportePeticiones;
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
public class FragmentListaTipoTransportes extends Fragment {

    private static final String TAG = "TransList";

    private RecyclerView recView;
    private CircularProgressView progressBar;
    private TextView txtNoTrans;
    private AdaptadorTipoTransporteList adaptadorTranspoteList;
    private RelativeLayout btnNuevoTransporte;

    UserModel userInfo;

    public static FragmentListaTipoTransportes newInstance(UserModel userInfo) {
        FragmentListaTipoTransportes fragment = new FragmentListaTipoTransportes();
        fragment.userInfo = userInfo;
        return fragment;
    }

    public void addTransporte(UserTransModel userTransInfo) {
        adaptadorTranspoteList.addTransporte(userTransInfo);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vistaInflada = inflater.inflate(R.layout.layout_transportes_trans, null);
        return vistaInflada;
    }

    SwipeRefreshLayout swipeRefreshListSolic;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recView = (RecyclerView) getView().findViewById(R.id.recView);
        btnNuevoTransporte = (RelativeLayout) getView().findViewById(R.id.btnAddTransporte);
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        UserModel userActual = sessionInfo.getUser();
        btnNuevoTransporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerTipoTransporte();
            }
        });
        if (userActual.getId() != userInfo.getId()) {
            btnNuevoTransporte.setVisibility(View.GONE);
        }
        progressBar = (CircularProgressView) getView().findViewById(R.id.progressBar);
        txtNoTrans = (TextView) getView().findViewById(R.id.txtNoTransportes);
        recView.setHasFixedSize(true);
        swipeRefreshListSolic = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshSolicitudes);
        swipeRefreshListSolic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                adaptadorTranspoteList.vaciar();
                obtenerTransportes();

            }
        });
        adaptadorTranspoteList = new AdaptadorTipoTransporteList(getActivity(), null);
        progressBar.setVisibility(View.VISIBLE);
        txtNoTrans.setVisibility(View.GONE);
        recView.setAdapter(adaptadorTranspoteList);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        obtenerTransportes();
    }

    private void obtenerTipoTransporte() {
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
        TransportePeticiones servicioTransporte = adapter.create(TransportePeticiones.class);
        servicioTransporte.obtenerTipoTransporte(new Callback<List<TipoTransModel>>() {
            @Override
            public void success(List<TipoTransModel> tipoTransModels, Response response) {
                dialogLoading.dismiss();
                abrirDialogAddTransporte(new ArrayList<>(tipoTransModels));
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                if (error.getResponse() == null) {
                    Snackbar.make(btnNuevoTransporte, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(btnNuevoTransporte, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }
            }
        });
    }

    public static final int REQUEST_IMAGE_CAPTURE = 1888;
    ImageView imgFotoTrans;

    private void abrirDialogAddTransporte(final List<TipoTransModel> listaDeTransporte) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_transporte);
        ArrayList<UserTransModel> listaTiposActuales = adaptadorTranspoteList.getListData();
        for (int i = 0; i < listaDeTransporte.size(); i++) {
            for (int j = 0; j < listaTiposActuales.size(); j++) {
                if (listaDeTransporte.get(i).getId() == listaTiposActuales.get(j).getTransporte().getId()) {
                    listaDeTransporte.remove(i);
                    i--;
                    break;
                }
            }
        }
        imgFotoTrans = (ImageView) dialog.findViewById(R.id.imgFotoCarro);
        imgFotoTrans.setTag("");
        imgFotoTrans.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.ic_add_a_photo_black_48dp));
        imgFotoTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrigDialogChooseImg();

            }
        });
        TextView txtTitle = (TextView) dialog.findViewById(R.id.title);
        txtTitle.setText("Nuevo Transporte");
        final TextInputLayout tilCondicion = (TextInputLayout) dialog.findViewById(R.id.tilCondicion);
        final EditText txtCondicion = (EditText) dialog.findViewById(R.id.textCondicion);
        final TextInputLayout tilPlaca = (TextInputLayout) dialog.findViewById(R.id.tilPlaca);
        final EditText txtPlaca = (EditText) dialog.findViewById(R.id.textPlaca);
        final TextInputLayout tilPolizaCompa = (TextInputLayout) dialog.findViewById(R.id.tilPolizaCompa);
        final EditText txtPolizaCompa = (EditText) dialog.findViewById(R.id.textPolizaCompa);
        final TextInputLayout tilPolizaNumero = (TextInputLayout) dialog.findViewById(R.id.tilPolizaNumero);
        final EditText txtPolizaNumero = (EditText) dialog.findViewById(R.id.textPolizaNumero);
        TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
        TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
        final MaterialSpinner spinnerTipoTrans = (MaterialSpinner) dialog.findViewById(R.id.spinnerCategoria);
        spinnerTipoTrans.setPaddingSafe(0, 0, 0, 0);
        String[] arrayTipoTrans = new String[listaDeTransporte.size()];
        int i = 0;
        for (TipoTransModel tipoTransInfo : listaDeTransporte) {
            arrayTipoTrans[i] = tipoTransInfo.getNombre();
            i++;
        }
        ArrayAdapter<String> adapterTipoTransList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, arrayTipoTrans);
        adapterTipoTransList.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoTrans.setAdapter(adapterTipoTransList);
        spinnerTipoTrans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
                if (txtCondicion.getText().toString().trim().length() == 0) {
                    tilCondicion.setErrorEnabled(true);
                    tilCondicion.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtPlaca.getText().toString().trim().length() == 0) {
                    tilPlaca.setErrorEnabled(true);
                    tilPlaca.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtPolizaCompa.getText().toString().trim().length() == 0) {
                    tilPolizaCompa.setErrorEnabled(true);
                    tilPolizaCompa.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (txtPolizaNumero.getText().toString().trim().length() == 0) {
                    tilPolizaNumero.setErrorEnabled(true);
                    tilPolizaNumero.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (spinnerTipoTrans.getSelectedItemPosition() == 0) {
                    spinnerTipoTrans.setError("Este campo es obligatorio");
                    valido = false;
                }
                if (valido) {
                    UserTransModel userTransInfo = new UserTransModel();
                    userTransInfo.setCondicion(txtCondicion.getText().toString());
                    userTransInfo.setPlaca(txtPlaca.getText().toString());
                    userTransInfo.setPolizaCompa(txtPolizaCompa.getText().toString());
                    userTransInfo.setPolizaNumero(txtPolizaNumero.getText().toString());
                    userTransInfo.setIdTransporte(listaDeTransporte.get(spinnerTipoTrans.getSelectedItemPosition() - 1).getId());
                    agregarTransporte(userTransInfo);
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
            imgFotoTrans.setTag(photoFile.getAbsolutePath());
            getParentFragment().startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    public void addDataFromCamara(Intent data) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        String imagePath = imgFotoTrans.getTag().toString();
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
        imgFotoTrans.setImageBitmap(imageBitmap);
        imgFotoTrans.setOnClickListener(null);
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

    private void agregarTransporte(UserTransModel userTransInfo) {
        //TODO hacer el registro de transporte
        /*final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();*/
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();

        OkHttpClient cliente = new OkHttpClient();
        cliente.setConnectTimeout(3, TimeUnit.MINUTES);
        cliente.setReadTimeout(3, TimeUnit.MINUTES);
        cliente.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setClient(new OkClient(cliente));

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("token", token);
            }
        });

        RestAdapter adapter = builder.build();
        TypedString transporteId = new TypedString(Integer.toString(userTransInfo.getIdTransporte()));
        TypedString condicion = new TypedString(userTransInfo.getCondicion());
        TypedString placa = new TypedString(userTransInfo.getCondicion());
        TypedString polizaCompa = new TypedString(userTransInfo.getPolizaCompa());
        TypedString polizaNumero = new TypedString(userTransInfo.getPolizaNumero());
        TypedFile fotoTransport = null;
        if (imgFotoTrans.getTag() != "") {
            fotoTransport = new TypedFile("multipart/form-data", new File(imgFotoTrans.getTag().toString()));
        }
        Snackbar.make(recView, "El transporte se publicara en breve", Snackbar.LENGTH_SHORT).show();
        final Context context = getActivity();
        TransportePeticiones servicioTransportes = adapter.create(TransportePeticiones.class);
        servicioTransportes.addTransportesTranspor(
                sessionInfo.getUser().getId(),
                transporteId,
                condicion,
                placa,
                polizaCompa,
                polizaNumero,
                fotoTransport,
                new Callback<UserTransModel>() {
                    @Override
                    public void success(UserTransModel userTransInfo, Response response) {
                        //dialogLoading.dismiss();
                        Toast.makeText(context, "Transporte publicado", Toast.LENGTH_SHORT).show();
                        limpiarFoto();
                        adaptadorTranspoteList.addTransporte(userTransInfo);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        limpiarFoto();
                        //dialogLoading.dismiss();
                        Log.d("TRANSPORTE", "Todo probando " + error.toString());
                        if (error.getResponse() == null) {
                            Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(recView, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                            try {
                                String errorCadena = convertStreamToString(error.getResponse().getBody().in());
                                Log.d("ERROR", errorCadena);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d("ERROR", error.toString());
                            Log.d("ERROR", error.getMessage());
                            Log.d("ERROR", error.getUrl());
                        }
                    }
                });
    }

    public void limpiarFoto() {
        new File(imgFotoTrans.getTag().toString()).delete();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void obtenerTransportes() {
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
        TransportePeticiones servicioTransporte = adapter.create(TransportePeticiones.class);
        servicioTransporte.obtenerTransportesTranspor(userInfo.getId(), new Callback<List<UserTransModel>>() {
            @Override
            public void success(List<UserTransModel> listaDeTransportes, Response response) {
                progressBar.setVisibility(View.GONE);
                if (listaDeTransportes.size() == 0) {
                    txtNoTrans.setVisibility(View.VISIBLE);
                } else {
                    for (UserTransModel userTransInfo : listaDeTransportes) {
                        adaptadorTranspoteList.addTransporte(userTransInfo);
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
                    Snackbar.make(recView, "Error de conexión", Snackbar.LENGTH_SHORT).show();
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

    public void asignarPicFromGallery(ChosenImage chosenImage) {
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

            imgFotoTrans.setTag(file.getAbsolutePath());
            imgFotoTrans.setImageBitmap(out);
            imgFotoTrans.setOnClickListener(null);
        } else {
            Toast.makeText(getActivity(), "Problemas de memoria al subir imagen, intente de nuevo", Toast.LENGTH_SHORT).show();
        }
    }

}
