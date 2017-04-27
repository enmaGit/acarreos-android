package com.acarreos.creative.Fragments.VistaEnvio;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UbicacionModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 07/09/2015.
 */
public class FragmentUbicacion extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int PADDING_MARKER = 40;
    EnvioModel envioInfo;
    ArrayList<Marker> markers;
    boolean cargaContActive;
    public int paginasSolicitar;
    GoogleMap googleMapFragment;
    Activity activityPadre;
    ArrayList<EnvioModel> listaDeEnvios;


    public static FragmentUbicacion newInstance(EnvioModel envioInfo) {
        FragmentUbicacion fragment = new FragmentUbicacion();
        fragment.envioInfo = envioInfo;
        fragment.markers = new ArrayList<>();
        fragment.listaDeEnvios = new ArrayList<>();
        fragment.cargaContActive = false;
        fragment.paginasSolicitar = 0;
        fragment.googleMapFragment = null;
        return fragment;
    }

    public FragmentUbicacion() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getMapAsync(this);
    }

    private static final float COLOR_AZUL = 211f;
    private static final float COLOR_VERDE = 82f;
    private static final float COLOR_PURPURA = 294f;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng origen = new LatLng(envioInfo.lat_origen, envioInfo.lon_origen);
        LatLng destino = new LatLng(envioInfo.lat_destino, envioInfo.lon_destino);
        googleMapFragment = googleMap;
        googleMap.setOnMarkerClickListener(this);
        markers.add(googleMap.addMarker(
                new MarkerOptions()
                        .position(origen)
                        .title("Origen")
                        .icon(BitmapDescriptorFactory.defaultMarker(COLOR_VERDE))));
        markers.add(googleMap.addMarker(new MarkerOptions()
                .position(destino)
                .title("Destino")
                .icon(BitmapDescriptorFactory.defaultMarker(COLOR_AZUL))));
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = PADDING_MARKER; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        googleMap.moveCamera(cu);
        if (envioInfo.getEstatus().getId() == EnvioModel.STATUS_DESARROLLO || envioInfo.getEstatus().getId() == EnvioModel.STATUS_FINALIZADO) {
            obtenerUbicaciones(googleMap);
        }
    }

    private void obtenerUbicaciones(final GoogleMap googleMap) {
        /*final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();*/
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        if (sessionInfo != null) {
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
            final Context context = getActivity();
            EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
            servicioEnvio.obtenerEnvioUbicaciones(envioInfo.getId(), new Callback<List<UbicacionModel>>() {
                @Override
                public void success(List<UbicacionModel> listaDeUbicacion, Response response) {
                    //dialogLoading.dismiss();
                    if (listaDeUbicacion.size() > 0) {
                        UbicacionModel latLngAnterior = listaDeUbicacion.get(0);
                        PolylineOptions trayecto = new PolylineOptions();
                        trayecto.width(5f);
                        trayecto.color(context.getResources().getColor(R.color.colorPrimary));
                        LatLng origen = new LatLng(envioInfo.lat_origen, envioInfo.lon_origen);
                        trayecto.add(origen, latLngAnterior.getPos());
                        markers.add(googleMap.addMarker(new MarkerOptions().position(latLngAnterior.getPos()).title(latLngAnterior.getFechaUpdate())));
                        for (int i = 1; i < listaDeUbicacion.size(); i++) {
                            if (i == listaDeUbicacion.size() - 1) {
                                markers.add(googleMap.addMarker(
                                        new MarkerOptions()
                                                .position(listaDeUbicacion.get(i).getPos())
                                                .title(listaDeUbicacion.get(i).getFechaUpdate())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_trans_marker))
                                                .anchor(0.5f, 0.5f)));
                            } else {
                                markers.add(googleMap.addMarker(new MarkerOptions()
                                        .position(listaDeUbicacion.get(i).getPos())
                                        .title(listaDeUbicacion.get(i).getFechaUpdate())));
                            }
                            trayecto.add(latLngAnterior.getPos(), listaDeUbicacion.get(i).getPos());
                            latLngAnterior = listaDeUbicacion.get(i);
                        }
                        googleMap.addPolyline(trayecto);
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (Marker marker : markers) {
                            builder.include(marker.getPosition());
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = PADDING_MARKER; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                        googleMap.moveCamera(cu);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    //dialogLoading.dismiss();
                    if (error.getResponse() == null) {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show();
                        /*Log.d("ERROR", error.toString());
                        Log.d("ERROR", error.getMessage());
                        Log.d("ERROR", error.getUrl());*/
                    }

                }
            });
        }
    }

    public void toggleCargaContinua() {
        if (cargaContActive) {
            desactivarCargaContinua();
        } else {
            activarCargaContinua();
        }
    }

    private void activarCargaContinua() {
        Toast.makeText(getActivity(), "Ya puedes ver los envíos en tu ruta en morado", Toast.LENGTH_SHORT).show();
        paginasSolicitar = 0;
        cargaContActive = true;
        obtenerEnviosTranspor();
    }

    private void desactivarCargaContinua() {
        cargaContActive = false;
        Toast.makeText(getActivity(), "Desactivando carga continua", Toast.LENGTH_SHORT).show();
        if (googleMapFragment != null) {
            for (Marker marker : markers) {
                String titleId = marker.getTitle();
                int id = tryParseInt(titleId);
                if (id >= 0) {
                    marker.remove();
                }
            }
        }
    }

    private void obtenerEnviosTranspor() {
        paginasSolicitar++;
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        final int idUsuario = sessionInfo.getUser().getId();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setClient(new OkClient(client));

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("token", token);
                String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                request.addQueryParam("estatus", valueParam);
                request.addQueryParam("page", Integer.toString(paginasSolicitar));
            }
        });

        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        Log.d("Trans", "pidiendo: ");
        servicioEnvio.obtenerEnviosTransportista(new Callback<List<EnvioModel>>() {
            @Override
            public void success(List<EnvioModel> listaDeEnviosServer, Response response) {
                Log.d("Trans", response.getUrl());
                if (listaDeEnviosServer.size() == 0) {
                    Toast.makeText(getActivity(), "No hay envíos para Carga Continua", Toast.LENGTH_SHORT).show();
                } else {
                    for (EnvioModel envioInfo : listaDeEnviosServer) {
                        listaDeEnvios.add(envioInfo);
                        addMarkerEnvio(envioInfo);
                    }
                }
                if (listaDeEnviosServer.size() == 10) {
                    obtenerEnviosTranspor();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (error.getResponse() == null) {
                    Log.d("Trans", "error: " + error.getUrl());
                } else {
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }
            }
        });
    }

    private EnvioModel getEnvioFromList(int id) {
        for (EnvioModel envioModel : listaDeEnvios) {
            if (envioModel.getId() == id) {
                return envioModel;
            }
        }
        return null;
    }

    private void addMarkerEnvio(EnvioModel envioInfo) {
        if (googleMapFragment != null) {
            LatLng origen = new LatLng(envioInfo.lat_origen, envioInfo.lon_origen);
            markers.add(googleMapFragment.addMarker(
                    new MarkerOptions()
                            .position(origen)
                            .title(envioInfo.getId() + "")
                            .icon(BitmapDescriptorFactory.defaultMarker(COLOR_PURPURA))));
        }
    }

    int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            // Log exception.
            return -1;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String titleId = marker.getTitle();
        int id = tryParseInt(titleId);
        Log.e("Revisar", "Se dio click a un marker: " + id + " - " + titleId);
        if (id >= 0) {
            BaseActivity activityPadre = (BaseActivity) getActivity();
            activityPadre.abrirDetalleEnvio(getEnvioFromList(id), PagerDetalleEnviosAdapter.PESTAÑA_INFORMACION, false);
            return true;
        }
        return false;
    }
}
