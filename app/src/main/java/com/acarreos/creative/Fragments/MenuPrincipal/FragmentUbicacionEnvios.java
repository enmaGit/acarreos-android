package com.acarreos.creative.Fragments.MenuPrincipal;

import android.os.Bundle;
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
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import static com.acarreos.creative.Fragments.Listas.FragmentListaEnvios.MODO_TRANS_OFERTADOS;
import static com.acarreos.creative.Fragments.Listas.FragmentListaEnvios.MODO_TRANS_SIN_OFERTAR;
import static com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition;

/**
 * Created by EnmanuelPc on 07/09/2015.
 */
public class FragmentUbicacionEnvios extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    ArrayList<Marker> markers;
    boolean cargaContActive;
    GoogleMap googleMapFragment;
    ArrayList<EnvioModel> listaDeEnvios;


    public static FragmentUbicacionEnvios newInstance() {
        FragmentUbicacionEnvios fragment = new FragmentUbicacionEnvios();
        fragment.markers = new ArrayList<>();
        fragment.listaDeEnvios = new ArrayList<>();
        fragment.cargaContActive = false;
        fragment.googleMapFragment = null;
        return fragment;
    }

    public FragmentUbicacionEnvios() {
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
    private final LatLng PANAMA_LOCATE = new LatLng(8.4054927, -80.034829);
    private final int ZOOM_DEFUALT = 4;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapFragment = googleMap;
        googleMap.setOnMarkerClickListener(this);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(PANAMA_LOCATE)       // Sets the center of the map to Mountain View
                .zoom(ZOOM_DEFUALT)         // Sets the zoom
                .build();                   // Creates a CameraPosition from the builder
        googleMap.animateCamera(newCameraPosition(cameraPosition));
        obtenerEnviosTranspor(MODO_TRANS_OFERTADOS, 0);
        obtenerEnviosTranspor(MODO_TRANS_SIN_OFERTAR, 0);
    }

    private void obtenerEnviosTranspor(final int modoEnvio, final int pagina) {
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
                if (modoEnvio == MODO_TRANS_OFERTADOS) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                    request.addQueryParam("estatus", valueParam);
                    request.addQueryParam("ofertado", "1");
                } else if (modoEnvio == MODO_TRANS_SIN_OFERTAR) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                    request.addQueryParam("estatus", valueParam);
                }
                request.addQueryParam("page", Integer.toString(pagina + 1));
            }
        });

        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        Log.d("Trans", "pidiendo: ");
        servicioEnvio.obtenerEnviosTransportista(new Callback<List<EnvioModel>>() {
            @Override
            public void success(List<EnvioModel> listaDeEnviosServer, Response response) {
                Log.d("Trans", response.getUrl());
                if (listaDeEnviosServer.size() == 0 && markers.size() == 0) {
                    Toast.makeText(getActivity(), "No hay envíos mostrar", Toast.LENGTH_SHORT).show();
                } else {
                    for (EnvioModel envioInfo : listaDeEnviosServer) {
                        listaDeEnvios.add(envioInfo);
                        addMarkerEnvio(envioInfo);
                    }
                }
                if (listaDeEnviosServer.size() == 10) {
                    obtenerEnviosTranspor(modoEnvio, pagina + 1);
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
            SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
            if (envioInfo.haOfertado(sessionInfo.getUser())) {
                LatLng origen = new LatLng(envioInfo.lat_origen, envioInfo.lon_origen);
                LatLng destino = new LatLng(envioInfo.lat_destino, envioInfo.lon_destino);
                markers.add(googleMapFragment.addMarker(
                        new MarkerOptions()
                                .position(origen)
                                .title(envioInfo.getId() + "")
                                .icon(BitmapDescriptorFactory.defaultMarker(COLOR_VERDE))));
                markers.add(googleMapFragment.addMarker(
                        new MarkerOptions()
                                .position(destino)
                                .title(envioInfo.getId() + "")
                                .icon(BitmapDescriptorFactory.defaultMarker(COLOR_AZUL))));
            } else {
                LatLng origen = new LatLng(envioInfo.lat_origen, envioInfo.lon_origen);
                markers.add(googleMapFragment.addMarker(
                        new MarkerOptions()
                                .position(origen)
                                .title(envioInfo.getId() + "")
                                .icon(BitmapDescriptorFactory.defaultMarker(COLOR_PURPURA))));
            }
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
