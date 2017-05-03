package com.acarreos.creative.Fragments.VistaEnvio;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Fragments.Listas.FragmentListaTipoTransportes;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.EstatusEnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UbicacionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.LocationService;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 08/09/2015.
 */
public class FragmentDetalleEnvio extends Fragment {

    private static final int PESTA_VISTA_UBICACION = 3;
    private EnvioModel envioInfo;
    private ViewPager viewPager;
    private CollapsingToolbarLayout ctlLayout;
    private PagerDetalleEnviosAdapter adaptadorDetalleEnvio;
    int pestañaInicial;
    LinearLayout btnActPos;
    FloatingActionButton btnActivarCargaCont;

    Activity mActivity;

    public static FragmentDetalleEnvio newInstance(EnvioModel envioInfo, int pestaña) {
        FragmentDetalleEnvio fragment = new FragmentDetalleEnvio();
        fragment.pestañaInicial = pestaña;
        fragment.envioInfo = envioInfo;
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    public FragmentDetalleEnvio() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detalle_envio, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle(envioInfo.getTitle());
        final ReminderSession reminderSession = new ReminderSession(getActivity());

        SessionModel sessionInfo = reminderSession.obtenerInfoSession();
        UserModel userActual = sessionInfo.getUser();


        btnActPos = (LinearLayout) getView().findViewById(R.id.btnActPos);

        btnActivarCargaCont = (FloatingActionButton) getView().findViewById(R.id.btnCargaContActive);

        final boolean currentCargaContinuaStatus = reminderSession.getCargaContinuaStatus();

        if (userActual.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
            btnActivarCargaCont.setVisibility(View.VISIBLE);
        }

        if (currentCargaContinuaStatus) {
            btnActivarCargaCont.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.error_color)));
        } else {
            btnActivarCargaCont.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.appbartabs);


        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);

        if (envioInfo.getGanador() != null && (envioInfo.getEstatus().getId() == EstatusEnvioModel.ESTATUS_DESARROLLO)) {
            if (envioInfo.getGanador().getTransportista().getId() == userActual.getId()) {
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        if (position == PESTA_VISTA_UBICACION) {
                            btnActPos.setVisibility(View.VISIBLE);
                            btnActivarCargaCont.setVisibility(View.VISIBLE);
                        } else {
                            btnActPos.setVisibility(View.GONE);
                            btnActivarCargaCont.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                btnActPos.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        actualizarPosicion();
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setMessage("¿Seguro desea actualizar la ubicación del envío?").setPositiveButton("Sí", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();
                    }
                });

                btnActivarCargaCont.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        reminderSession.setCargaContinuaStatus(!reminderSession.getCargaContinuaStatus());
                        if (reminderSession.getCargaContinuaStatus()) {
                            btnActivarCargaCont.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.error_color)));
                        } else {
                            btnActivarCargaCont.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                        }
                        adaptadorDetalleEnvio.toggleCargaContinua();
                    }
                });
            }
        }

        adaptadorDetalleEnvio = new PagerDetalleEnviosAdapter(getChildFragmentManager(), envioInfo);

        viewPager.setAdapter(adaptadorDetalleEnvio);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(pestañaInicial);
    }

    LocationService locationListener;
    Dialog dialogLoading;

    private void actualizarPosicion() {
        if (getGpsStatus()) {
            dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
            dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogLoading.setContentView(R.layout.dialog_loading);
            dialogLoading.setCancelable(false);
            dialogLoading.show();
            final SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
            final Context context = getActivity();
            final AuthRequestInterceptor authRequestInterceptor = new AuthRequestInterceptor((BaseActivity) getActivity());

            locationListener = LocationService.getLocationManager(getActivity(), new LocationService.OnLocationChangeListener() {
                @Override
                public void onChange(Location loc) {
                    if (loc != null) {
                        String posicion = "Lat: " + loc.getLatitude() + " - Lon: " + loc.getLongitude();
                        Log.d("POS", posicion);
                        final String token = sessionInfo.getToken();

                        UbicacionModel ubicacionInfo = new UbicacionModel();
                        ubicacionInfo.setLatitud(loc.getLatitude());
                        ubicacionInfo.setLongitud(loc.getLongitude());

                        OkHttpClient client = new OkHttpClient();
                        client.interceptors().add(authRequestInterceptor);

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
                        servicioEnvio.actualizarUbicacion(envioInfo.getId(), ubicacionInfo, new Callback<UbicacionModel>() {
                            @Override
                            public void success(UbicacionModel ubicacionModel, Response response) {
                                dialogLoading.dismiss();
                                Snackbar.make(btnActPos, "Ubicación actualizada", Snackbar.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                dialogLoading.dismiss();
                                if (error.getResponse() == null) {
                                    Snackbar.make(btnActPos, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(btnActPos, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                                    Log.d("ERROR", error.toString());
                                    Log.d("ERROR", error.getMessage());
                                    Log.d("ERROR", error.getUrl());
                                }
                            }
                        });
                    } else {
                        dialogLoading.dismiss();
                        Toast.makeText(context, "Fallo en su hardware de GPS, intente de nuevo", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Snackbar.make(viewPager, "Debes encender el gps", Snackbar.LENGTH_SHORT).show();
        }
    }

    /*----Method to Check GPS is enable or disable ----- */
    private boolean getGpsStatus() {
        ContentResolver contentResolver = getContext()
                .getContentResolver();
        boolean gpsStatus = Settings.Secure
                .isLocationProviderEnabled(contentResolver,
                        LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FragmentListaTipoTransportes.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            adaptadorDetalleEnvio.pasarDatosDeCamara(data);
        }
    }


}
