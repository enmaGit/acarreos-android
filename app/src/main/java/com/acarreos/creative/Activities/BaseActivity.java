package com.acarreos.creative.Activities;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Constants.AppConstants;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentCuentaTranspor;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentEnviosTransportista;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentLogin;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentMiCuentaCliente;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentPrincipalAdmin;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentPrincipalCliente;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentPrincipalTranspor;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentSpam;
import com.acarreos.creative.Fragments.VistaEnvio.FragmentDetalleEnvio;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.PeticionesWeb.UserPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Services.AlarmService;
import com.acarreos.creative.Services.RegistrationIntentService;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.Navigator;
import com.acarreos.creative.Util.ReminderSession;
import com.dd.ShadowLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.Gson;
import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.squareup.okhttp.OkHttpClient;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

import static com.acarreos.creative.Constants.AppConstants.KEY_ENVIO;

public class BaseActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String SHARED_PRIMERA_VEZ = "Shared_Primera_vez";
    private static final String NOMBRE_ITEM_PRIMERA_VEZ = "primeraVezItem";

    protected Toolbar mToolbar;

    protected DrawerLayout mDrawerLayout;
    protected NavigationView navigationDrawer;
    protected ActionBarDrawerToggle mDrawerToggle;
    protected TextView txtNombreUsuario;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }


    private static final String MINT_API_KEY = "b4d1b9cb";

    protected
    @IdRes
    int mCurrentMenuItem;

    protected static Navigator mNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Mint.initAndStartSession(BaseActivity.this, MINT_API_KEY);
        setContentView(R.layout.activity_cliente);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (isPrimeraVez()) {
            addShortcut();
        }
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigator = new Navigator(getSupportFragmentManager(), R.id.content_frame);
        navigationDrawer = (NavigationView) findViewById(R.id.navigation_drawer);
        View headerVew = navigationDrawer.getHeaderView(0);
        txtNombreUsuario = (TextView) headerVew.findViewById(R.id.nombreUser);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        ReminderSession reminderSession = new ReminderSession(BaseActivity.this);
        if (reminderSession.obtenerInfoSession() != null) {
            mCurrentMenuItem = R.id.item_ver_envios;
            ingresar(reminderSession.obtenerInfoSession().getUser());
        } else {
            mCurrentMenuItem = R.id.cerrar_sesion;
            abrirPantallaLogin();
        }
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
        revisarNotificacion(getIntent());
        //abrirPantallaLogin();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }

    private boolean isPrimeraVez() {
        SharedPreferences prefPrimeraVez = BaseActivity.this.getSharedPreferences(SHARED_PRIMERA_VEZ, Context.MODE_PRIVATE);
        if (prefPrimeraVez.getBoolean(NOMBRE_ITEM_PRIMERA_VEZ, true)) {
            SharedPreferences.Editor editor = prefPrimeraVez.edit();
            editor.putBoolean(NOMBRE_ITEM_PRIMERA_VEZ, false);
            editor.apply();
            return true;
        }
        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        revisarNotificacion(intent);
    }

    private void revisarNotificacion(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String tipoNotif = extras.getString(AppConstants.KEY_TIPO);
            for (String value : extras.keySet()) {
                Log.d("BaseActivity", "Value " + value + ": " + extras.get(value));
            }
            if (tipoNotif != null) {
                switch (tipoNotif) {
                    case AppConstants.NOTIF_ENVIO_FINALIZADO:
                        ReminderSession reminderSession = new ReminderSession(BaseActivity.this);
                        UserModel userInfo = reminderSession.obtenerInfoSession().getUser();
                        if (userInfo.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
                            String envioJson = extras.getString(KEY_ENVIO);
                            Log.d("Prueba", envioJson);
                            Gson conversorJson = new Gson();
                            EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
                            abrirDialogoValorarTranspor(envioInfo);
                        } else if (userInfo.getTipo_user_id() == UserModel.TIPO_CLIENTE) {
                            String envioJson = extras.getString(KEY_ENVIO);
                            Log.d("Prueba", envioJson);
                            Gson conversorJson = new Gson();
                            EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
                            abrirDialogoValorarCliente(envioInfo);
                        }
                        break;
                    case AppConstants.NOTIF_NUEVA_OFERTA:
                        notificarNuevaOferta(extras);
                        break;
                    case AppConstants.NOTIF_ENVIO_CANCELADO:
                        notificarEnvioCancelado(extras);
                        break;
                    case AppConstants.NOTIF_OFERTA_ACEPTADA:
                        notificarOfertaAceptada(extras);
                        break;
                    case AppConstants.NOTIF_ACT_UBICACION:
                        notificarActUbicacion(extras);
                        break;
                    default:
                        break;
                }
            }
        } else {
            //Toast.makeText(BaseActivity.this, "Vacio", Toast.LENGTH_LONG).show();
        }
    }

    private void notificarActUbicacion(Bundle extras) {
        String envioJson = extras.getString(KEY_ENVIO);
        Gson conversorJson = new Gson();
        final EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
        abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_UBICACION, true);
    }

    private void notificarOfertaAceptada(Bundle extras) {
        SessionModel sessionInfo = new ReminderSession(BaseActivity.this).obtenerInfoSession();
        UserModel userInfo = sessionInfo.getUser();
        setNewRootFragment(FragmentEnviosTransportista.newInstance());
    }

    private void notificarNuevaOferta(Bundle extras) {
        String envioJson = extras.getString(KEY_ENVIO);
        Gson conversorJson = new Gson();
        final EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
        abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_OFERTAS, true);
    }

    private void notificarEnvioCancelado(Bundle extras) {
        String envioJson = extras.getString(KEY_ENVIO);
        Gson conversorJson = new Gson();
        final EnvioModel envioInfo = conversorJson.fromJson(envioJson, EnvioModel.class);
        abrirDetalleEnvio(envioInfo, PagerDetalleEnviosAdapter.PESTAÑA_INFORMACION, true);
    }

    private Dialog dialogValorar;

    public void abrirDialogoValorarTranspor(EnvioModel envioInfo) {
        dialogValorar = new Dialog(BaseActivity.this, android.R.style.Theme_Light);
        dialogValorar.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogValorar.setContentView(R.layout.dialog_valoracion_trans);
        TextView txtTitleEnvio = (TextView) dialogValorar.findViewById(R.id.txtEnvioTitle);
        txtTitleEnvio.setText(envioInfo.getTitle());
        TextView txtUserEnvio = (TextView) dialogValorar.findViewById(R.id.textUserEnvio);
        txtUserEnvio.setText(envioInfo.getCliente().getLogin());
        final RatingBar rating = (RatingBar) dialogValorar.findViewById(R.id.ratingEnvio);
        rating.setRating(envioInfo.getValoracion());
        final TextView txtComentario = (TextView) dialogValorar.findViewById(R.id.textComentario);
        txtComentario.setText(envioInfo.getComentario());
        final ShadowLayout btnAvanzar = (ShadowLayout) dialogValorar.findViewById(R.id.btnAvanzar);
        btnAvanzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogValorar.dismiss();
            }
        });
        dialogValorar.show();
    }


    public void abrirDialogoValorarCliente(final EnvioModel envioInfo) {
        dialogValorar = new Dialog(BaseActivity.this, android.R.style.Theme_Light);
        dialogValorar.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogValorar.setContentView(R.layout.dialog_valoracion_cli);
        TextView txtTitleEnvio = (TextView) dialogValorar.findViewById(R.id.txtEnvioTitle);
        txtTitleEnvio.setText(envioInfo.getTitle());
        TextView txtTransEnvio = (TextView) dialogValorar.findViewById(R.id.textTransEnvio);
        txtTransEnvio.setText(envioInfo.getGanador().getTransportista().getLogin());
        final RatingBar rating = (RatingBar) dialogValorar.findViewById(R.id.ratingEnvio);
        final EditText txtComentario = (EditText) dialogValorar.findViewById(R.id.textComentario);
        final ShadowLayout btnValorar = (ShadowLayout) dialogValorar.findViewById(R.id.btnValorar);
        btnValorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (txtComentario.getText().toString().length() == 0) {
                    Snackbar.make(btnValorar, "Debe añadir un comentario", Snackbar.LENGTH_SHORT).show();
                } else {
                    envioInfo.setValoracion((int) rating.getRating());
                    envioInfo.setComentario(txtComentario.getText().toString().trim());
                    enviarInfoValoracion(envioInfo);
                }
            }
        });
        dialogValorar.show();
    }

    private void enviarInfoValoracion(EnvioModel envioInfo) {
        final Dialog dialogLoading = new Dialog(BaseActivity.this, R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        SessionModel sessionInfo = new ReminderSession(BaseActivity.this).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        int idUsuario = sessionInfo.getUser().getId();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor(BaseActivity.this));

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
        servicioEnvio.modificarEnvio(idUsuario, envioInfo.getId(), envioInfo, new Callback<EnvioModel>() {
            @Override
            public void success(EnvioModel envioModel, Response response) {
                //TODO traer el estatus del envio desde el servidor
                dialogLoading.dismiss();
                dialogValorar.dismiss();
                Snackbar.make(mDrawerLayout, "Valoración guardada", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                /*Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());*/
                Snackbar.make(mDrawerLayout, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void addShortcut() {
        //Adding shortcut for MainActivity
        //on Home screen
        /*Intent shortcutIntent = new Intent(getApplicationContext(),
                BaseActivity.class);

        shortcutIntent.setAction(Intent.ACTION_MAIN);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(getApplicationContext(),
                        R.drawable.ic_launcher));
        addIntent.putExtra("duplicate", false);

        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);

        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        getApplicationContext().sendBroadcast(addIntent);*/
        Intent shortcutIntent = new Intent(getApplicationContext(),
                BaseActivity.class);
        //shortcutIntent.setClassName(BaseActivity.this.getPackageName(), "BaseActivity");
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(BaseActivity.this, R.drawable.ic_launcher));
        addIntent.putExtra("duplicate", false);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        BaseActivity.this.sendBroadcast(addIntent);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("ERROR", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    protected void setNewRootFragment(Fragment fragment) {
        fragment.setRetainInstance(true);
        mNavigator.setRootFragment(fragment);
        mDrawerLayout.closeDrawers();
    }

    public void abrirPantallaLogin() {
        if (mNavigator != null) {
            //mNavigator.clearHistory();
            mCurrentMenuItem = R.id.cerrar_sesion;
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            Log.e("Login", "Esto paso 1");
            mNavigator.setRootFragment(FragmentLogin.newInstance());
            Log.e("Login", "Esto paso 2");
        }
    }

    public void abrirDetalleEnvio(final EnvioModel envioInfo, final int pestaña, boolean cargar) {
        mCurrentMenuItem = -1;
        if (cargar) {
            Toast.makeText(BaseActivity.this, "Espere, por favor", Toast.LENGTH_SHORT).show();
            SessionModel sessionInfo = new ReminderSession(BaseActivity.this).obtenerInfoSession();
            final String token = sessionInfo.getToken();

            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new AuthRequestInterceptor(BaseActivity.this));

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
            servicioEnvio.obtenerDetalleEnvio(envioInfo.getId(), new Callback<EnvioModel>() {
                @Override
                public void success(EnvioModel envioModel, Response response) {
                    //TODO traer el estatus del envio desde el servidor
                    mNavigator.goTo(FragmentDetalleEnvio.newInstance(envioModel, pestaña));
                }

                @Override
                public void failure(RetrofitError error) {
                /*Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());*/
                    if (envioInfo != null) {
                        Toast.makeText(BaseActivity.this, "Problemas de conexión", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            mNavigator.goTo(FragmentDetalleEnvio.newInstance(envioInfo, pestaña));
        }
    }

    public void cerrarSesion() {
        //ingresar(new ReminderSession(this).obtenerInfoSession().getUser());
        desactivarAlarma();
        abrirPantallaLogin();
        ViewGroup container = (ViewGroup) findViewById(R.id.content_frame);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View screenLoading = inflater.inflate(R.layout.layout_loading_screen, container, false);
        container.addView(screenLoading);
        TextView txtMensajeLoading = (TextView) screenLoading.findViewById(R.id.mensajeLoading);
        txtMensajeLoading.setText("Desconectando");
        SessionModel sessionInfo = new ReminderSession(this).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        int idUsuario = sessionInfo.getUser().getId();

        OkHttpClient client = new OkHttpClient();

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setClient(new OkClient(client));

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("token", token);
            }
        });

        new ReminderSession(BaseActivity.this).borrarSession();
        RestAdapter adapter = builder.build();
        UserPeticiones userPeticiones = adapter.create(UserPeticiones.class);
        userPeticiones.cerrarSession(new Callback<Object>() {

            @Override
            public void success(Object o, Response response) {
                //abrirPantallaLogin();
                screenLoading.setVisibility(View.GONE);
            }

            @Override
            public void failure(RetrofitError error) {
                screenLoading.setVisibility(View.GONE);
                Toast.makeText(BaseActivity.this, "Error de conexion al cerrar sesion", Toast.LENGTH_SHORT).show();
                /*Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());*/
            }
        });
    }

    private void desactivarAlarma() {
        Intent intent = new Intent(this, AlarmService.class);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), AlarmService.ALARM_ID, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    ImageChooserManager imageChooserManager;

    public void setImageChooserManager(ImageChooserManager imageChooserManager) {
        this.imageChooserManager = imageChooserManager;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == ChooserType.REQUEST_PICK_PICTURE) {
            if (imageChooserManager != null) {
                imageChooserManager.submit(requestCode, data);
            } else {
                Toast.makeText(this, "Intente de nuevo, fallo de memoria", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void ingresar(UserModel userInfo) {
        configurarNavigationDrawer(userInfo);
        if (userInfo.getTipo_user_id() == UserModel.TIPO_CLIENTE) {
            abrirEnvios(userInfo);
        }
        if (userInfo.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
            abrirVistaBuscarEnvios(userInfo);
        }
        if (userInfo.getTipo_user_id() == UserModel.TIPO_ADMIN) {
            abrirVistaComisiones();
        }
        Snackbar.make(mDrawerLayout, "Bienvenido, " + userInfo.getNombre(), Snackbar.LENGTH_SHORT).show();
    }

    private void abrirVistaComisiones() {
        mNavigator.clearHistory();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mCurrentMenuItem = R.id.item_ver_comisiones;
        setNewRootFragment(FragmentPrincipalAdmin.newInstance());
    }

    public void abrirEnvios(UserModel userInfo) {
        if (mNavigator != null) {
            mNavigator.clearHistory();
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            mCurrentMenuItem = R.id.item_ver_envios;
            txtNombreUsuario.setText(userInfo.getNombre() + " " + userInfo.getApellido());
            setNewRootFragment(FragmentPrincipalCliente.newInstance());
        }
    }

    public void abrirVistaBuscarEnvios(UserModel userInfo) {
        mNavigator.clearHistory();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mCurrentMenuItem = R.id.item_buscar_envios;
        txtNombreUsuario.setText(userInfo.getNombre() + " " + userInfo.getApellido());
        setNewRootFragment(FragmentPrincipalTranspor.newInstance());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //TODO revisar el stack de navigator
    }

    public void setupToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar == null) {
            //LOGD(this, "Didn't find a toolbar");
            return;
        }
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                mToolbar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerToggle.setDrawerIndicatorEnabled(false);
        mToolbar.setNavigationIcon(R.drawable.ic_launcher);
        mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_launcher);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
    }

    public void abrirCuentaTranspor(UserModel userInfo) {
        mCurrentMenuItem = R.id.mi_cuenta * -1;
        mNavigator.goTo(FragmentCuentaTranspor.newInstance(userInfo));
        mDrawerLayout.closeDrawers();
    }

    public void configurarNavigationDrawer(UserModel userInfo) {
        navigationDrawer.getMenu().clear();
        if (userInfo.getTipo_user_id() == UserModel.TIPO_CLIENTE) {
            navigationDrawer.inflateMenu(R.menu.menu_drawer_cliente);
            navigationDrawer.setNavigationItemSelectedListener(navListenerCliente);
        } else if (userInfo.getTipo_user_id() == UserModel.TIPO_TRANSPORTISTA) {
            navigationDrawer.inflateMenu(R.menu.menu_drawer_transpor);
            navigationDrawer.setNavigationItemSelectedListener(navListenerTransportista);
        } else if (userInfo.getTipo_user_id() == UserModel.TIPO_ADMIN) {
            navigationDrawer.inflateMenu(R.menu.menu_drawer_admin);
            navigationDrawer.setNavigationItemSelectedListener(navListenerAdmin);
        }
    }

    NavigationView.OnNavigationItemSelectedListener navListenerTransportista = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            @IdRes int id = menuItem.getItemId();
            if (id == mCurrentMenuItem) {
                mDrawerLayout.closeDrawers();
                return false;
            }
            mCurrentMenuItem = id;
            mDrawerLayout.closeDrawers();
            menuItem.setChecked(true);
            if (id == R.id.item_buscar_envios) {
                setNewRootFragment(FragmentPrincipalTranspor.newInstance());
                return true;
            } else if (id == R.id.item_mis_envios) {
                setNewRootFragment(FragmentEnviosTransportista.newInstance());
                return true;
            } else if (id == R.id.mi_cuenta) {
                SessionModel sessionInfo = new ReminderSession(BaseActivity.this).obtenerInfoSession();
                UserModel userInfo = sessionInfo.getUser();
                setNewRootFragment(FragmentCuentaTranspor.newInstance(userInfo));
                return true;
            } else if (id == R.id.cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return true;
        }
    };

    NavigationView.OnNavigationItemSelectedListener navListenerCliente = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            @IdRes int id = menuItem.getItemId();
            if (id == mCurrentMenuItem) {
                mDrawerLayout.closeDrawers();
                return false;
            }

            mCurrentMenuItem = id;
            mDrawerLayout.closeDrawers();
            menuItem.setChecked(true);
            if (id == R.id.item_ver_envios) {
                setNewRootFragment(FragmentPrincipalCliente.newInstance());
                return true;
            } else if (id == R.id.mi_cuenta) {
                setNewRootFragment(FragmentMiCuentaCliente.newInstance());
                return true;
            } else if (id == R.id.cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return true;
        }
    };

    NavigationView.OnNavigationItemSelectedListener navListenerAdmin = new NavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(MenuItem menuItem) {
            @IdRes int id = menuItem.getItemId();
            if (id == mCurrentMenuItem) {
                mDrawerLayout.closeDrawers();
                return false;
            }

            mCurrentMenuItem = id;
            mDrawerLayout.closeDrawers();
            menuItem.setChecked(true);
            if (id == R.id.item_ver_usuarios) {
                setNewRootFragment(FragmentPrincipalAdmin.newInstance());
                return true;
            } else if (id == R.id.item_ver_comisiones) {
                setNewRootFragment(FragmentPrincipalAdmin.newInstance());
                return true;
            } else if (id == R.id.item_send_spam) {
                setNewRootFragment(FragmentSpam.newInstance());
                return true;
            } else if (id == R.id.cerrar_sesion) {
                cerrarSesion();
                return true;
            }
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_cliente, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.item_ver_envios) {
            Toast.makeText(this, "HOla mundo", Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        mNavigator = null;
        super.finish();
    }
}
