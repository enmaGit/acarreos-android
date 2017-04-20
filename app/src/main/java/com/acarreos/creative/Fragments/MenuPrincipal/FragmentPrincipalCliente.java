package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerDetalleEnviosAdapter;
import com.acarreos.creative.Adapters.Fragment.PagerEnviosClienteAdapter;
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorProductosList;
import com.acarreos.creative.Adapters.Spinner.SpinnerTipoTransAdapter;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.CustomViews.DialogMapFragment;
import com.acarreos.creative.Models.CategoriaProductoModel;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.ProductoEnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.TipoTransModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.PeticionesWeb.ProductoPeticiones;
import com.acarreos.creative.PeticionesWeb.TransportePeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AddresLocationHandler;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.OkHttpClient;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class FragmentPrincipalCliente extends Fragment implements SearchView.OnQueryTextListener {

    private FloatingActionButton btnNuevoEnvio;
    private ViewPager viewPager;
    private CollapsingToolbarLayout ctlLayout;
    private UserModel userInfo;

    PagerEnviosClienteAdapter adaptadorPagerEnvio;

    CustomOnPageChangeListener listenerChangeViewPager;

    Context contextMenu;

    public static FragmentPrincipalCliente newInstance() {
        return new FragmentPrincipalCliente();
    }

    public FragmentPrincipalCliente() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_lista_envios, container, false);
        return itemView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Fragment", "Actividad created");
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        if (sessionInfo == null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.abrirPantallaLogin();
            return;
        }
        userInfo = sessionInfo.getUser();

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle("Mis envíos");

        setHasOptionsMenu(true);
        contextMenu = getActivity();

        btnNuevoEnvio = (FloatingActionButton) getView().findViewById(R.id.btnNuevoEnvio);
        btnNuevoEnvio.hide();

        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        listenerChangeViewPager = new CustomOnPageChangeListener();

        adaptadorPagerEnvio = new PagerEnviosClienteAdapter(getChildFragmentManager());

        viewPager.setAdapter(adaptadorPagerEnvio);
        viewPager.addOnPageChangeListener(listenerChangeViewPager);

        tabLayout.setupWithViewPager(viewPager);
        btnNuevoEnvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDialogCargarProductos();
                //obtenerTipoTransporte();
            }
        });
    }

    private RelativeLayout btnAvanzar;
    private RelativeLayout btnNuevoProducto;
    private RecyclerView recView;
    private AdaptadorProductosList adaptadorProductosList;

    public void abrirDialogCargarProductos() {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cargar_productos);
        btnNuevoProducto = (RelativeLayout) dialog.findViewById(R.id.btnAddProducto);
        btnNuevoProducto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerCategorias();
            }
        });
        adaptadorProductosList = new AdaptadorProductosList(getActivity(), null);
        recView = (RecyclerView) dialog.findViewById(R.id.recView);
        recView.setHasFixedSize(true);
        recView.setAdapter(adaptadorProductosList);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        btnAvanzar = (RelativeLayout) dialog.findViewById(R.id.btnAvanzar);
        btnAvanzar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                ArrayList<ProductoEnvioModel> listaDeProductos = adaptadorProductosList.getListaDeProductos();
                if (listaDeProductos.size() == 0) {
                    Toast.makeText(getActivity(), "Debe añadir al menos un producto", Toast.LENGTH_SHORT).show();
                } else {
                    obtenerTipoTransporte(listaDeProductos);
                }
            }
        });
        dialog.show();
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
                    Toast.makeText(getActivity(), "Error de conexión", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Todo grave " + error.toString(), Toast.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }

            }
        });
    }

    private void abrirDialogAddProducto(final List<CategoriaProductoModel> listaDeCategorias) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_producto);
        ArrayList<ProductoEnvioModel> listaProdActuales = adaptadorProductosList.getListaDeProductos();
        for (int i = 0; i < listaDeCategorias.size(); i++) {
            for (int j = 0; j < listaProdActuales.size(); j++) {
                if (listaDeCategorias.get(i).getId() == listaProdActuales.get(j).getCategoria().getId()) {
                    listaDeCategorias.remove(i);
                    i--;
                    break;
                }
            }
        }
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
                    productoInfo.setCantidad(Integer.valueOf(txtCantidad.getText().toString()));
                    productoInfo.setId(spinnerCategoria.getSelectedItemPosition());
                    productoInfo.setDescripcion(txtDescripcion.getText().toString().trim());
                    CategoriaProductoModel cateInfo = new CategoriaProductoModel();
                    String nombreCateSelec = spinnerCategoria.getAdapter().getItem(spinnerCategoria.getSelectedItemPosition()).toString();
                    int idCate = 1;
                    for (CategoriaProductoModel cateInfoList : listaDeCategorias) {
                        if (cateInfoList.getNombre().compareTo(nombreCateSelec) == 0) {
                            idCate = cateInfoList.getId();
                            break;
                        }
                    }
                    cateInfo.setNombre(nombreCateSelec);
                    cateInfo.setId(idCate);
                    productoInfo.setCategoria(cateInfo);
                    adaptadorProductosList.addProducto(productoInfo);
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adaptadorPagerEnvio.filtrarEnvios(query.trim());
        return true;
    }

    private class CustomOnPageChangeListener implements ViewPager.OnPageChangeListener {

        ArrayList<Integer> pagesWithoutFloatButton;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (estaSinBotonNewEnvio(position)) {
                btnNuevoEnvio.hide();
            } else {
                btnNuevoEnvio.show();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        private boolean estaSinBotonNewEnvio(Integer num) {
            if (pagesWithoutFloatButton != null) {
                for (Integer numList : pagesWithoutFloatButton) {
                    if (num == numList) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void addPageHiddeFloatingButton(Integer page) {
            if (pagesWithoutFloatButton == null) {
                pagesWithoutFloatButton = new ArrayList<>();
            }
            if (!estaSinBotonNewEnvio(page)) {
                pagesWithoutFloatButton.add(page);
            }
        }

        public void removePageHiddeFloatingButton(Integer page) {
            if (pagesWithoutFloatButton == null) {
                pagesWithoutFloatButton = new ArrayList<>();
            }
            if (estaSinBotonNewEnvio(page)) {
                for (Integer numList : pagesWithoutFloatButton) {
                    if (page == numList) {
                        pagesWithoutFloatButton.remove(numList);
                        break;
                    }
                }
            }
        }
    }

    public void ocultarFloatingButton(int page) {
        listenerChangeViewPager.addPageHiddeFloatingButton(page);
    }

    public void mostrarFloatingButton(int page) {
        listenerChangeViewPager.removePageHiddeFloatingButton(page);
    }

    public void obtenerTipoTransporte(final ArrayList<ProductoEnvioModel> listaDeProductos) {
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
                abrirDialogNuevoEnvio(new ArrayList<>(tipoTransModels), listaDeProductos);
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                if (error.getResponse() == null) {
                    Snackbar.make(viewPager, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(viewPager, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }
            }
        });
    }

    EditText textDesde;
    EditText textDesdeRef;
    LatLng latLngDesde;
    EditText textHasta;
    EditText textTitle;
    EditText textHastaRef;
    EditText textFecSugEntrega;
    EditText textHoraSugEntrega;
    LatLng latLngHasta;
    MaterialSpinner spinnerTipoTrans;
    TextView txtCantTransSelected;

    private void abrirDialogNuevoEnvio(ArrayList<TipoTransModel> listTrans, final ArrayList<ProductoEnvioModel> listaDeProductos) {
        final Dialog dialog = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_nuevo_envio);
        spinnerTipoTrans = (MaterialSpinner) dialog.findViewById(R.id.spinnerTipoTransporte);
        txtCantTransSelected = (TextView) dialog.findViewById(R.id.txtCantTransSelected);
        txtCantTransSelected.setText("0 Transportes seleccionados");
        spinnerTipoTrans.setPaddingSafe(0, 0, 0, 0);
        final SpinnerTipoTransAdapter adapterSpinner = new SpinnerTipoTransAdapter(getActivity(), listTrans, new SpinnerTipoTransAdapter.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int numItemsSelected) {
                txtCantTransSelected.setText(numItemsSelected + " Transportes seleccionados");
            }
        });
        spinnerTipoTrans.setAdapter(adapterSpinner);
        textTitle = (EditText) dialog.findViewById(R.id.textTitle);
        final TextInputLayout textInputTitle = (TextInputLayout) dialog.findViewById(R.id.tilTitle);
        textInputTitle.setErrorEnabled(true);
        textDesde = (EditText) dialog.findViewById(R.id.textDesde);
        final TextInputLayout textInputDesde = (TextInputLayout) dialog.findViewById(R.id.tilDesde);
        textInputDesde.setErrorEnabled(true);
        textHasta = (EditText) dialog.findViewById(R.id.textHasta);
        final TextInputLayout textInputHasta = (TextInputLayout) dialog.findViewById(R.id.tilHasta);
        textInputHasta.setErrorEnabled(true);
        textDesdeRef = (EditText) dialog.findViewById(R.id.textDesdeRef);
        final TextInputLayout textInputDesdeRef = (TextInputLayout) dialog.findViewById(R.id.tilDesdeRef);
        textInputDesdeRef.setErrorEnabled(true);
        textHastaRef = (EditText) dialog.findViewById(R.id.textHastaRef);
        final TextInputLayout textInputHastaRef = (TextInputLayout) dialog.findViewById(R.id.tilHastaRef);
        textInputHastaRef.setErrorEnabled(true);
        final EditText textMaxDias = (EditText) dialog.findViewById(R.id.textMaxDias);
        final TextInputLayout textInputMaxDias = (TextInputLayout) dialog.findViewById(R.id.tilMaxDias);
        textInputMaxDias.setErrorEnabled(true);
        textFecSugEntrega = (EditText) dialog.findViewById(R.id.textFecSugEntrega);
        final TextInputLayout textInputFecSug = (TextInputLayout) dialog.findViewById(R.id.tilFecSugEntrega);
        textInputFecSug.setErrorEnabled(true);
        final Activity actividadPadre = getActivity();
        textFecSugEntrega.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar now = Calendar.getInstance();
                    DatePickerDialog dpd = DatePickerDialog.newInstance(
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePickerDialog datePickerDialog, int year, int month, int day) {
                                    textFecSugEntrega.setText(year + "/" + (month + 1) + "/" + day);
                                }
                            },
                            now.get(Calendar.YEAR),
                            now.get(Calendar.MONTH),
                            now.get(Calendar.DAY_OF_MONTH)
                    );

                    dpd.show(actividadPadre.getFragmentManager(), "DatepickerdialogHola");
                    return true;
                }
                return false;
            }
        });
        textHoraSugEntrega = (EditText) dialog.findViewById(R.id.textHoraSugEntrega);
        final TextInputLayout textInputHoraSugEntrega = (TextInputLayout) dialog.findViewById(R.id.tilHoraSugEntrega);
        textInputHoraSugEntrega.setErrorEnabled(true);
        textHoraSugEntrega.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Calendar now = Calendar.getInstance();
                    TimePickerDialog dpd = TimePickerDialog.newInstance(
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(RadialPickerLayout radialPickerLayout, int hora, int minuto) {
                                    String horaCadena = hora + "";
                                    String minCadena = minuto + "";
                                    if (horaCadena.length() < 2) {
                                        horaCadena = "0" + horaCadena;
                                    }
                                    if (minCadena.length() < 2) {
                                        minCadena = "0" + minCadena;
                                    }
                                    textHoraSugEntrega.setText(horaCadena + ":" + minCadena + ":00");
                                }
                            },
                            now.get(Calendar.HOUR),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    dpd.show(actividadPadre.getFragmentManager(), "TimepickerdialogHola");
                    return true;
                }
                return false;
            }
        });
        TextView btnGuardar = (TextView) dialog.findViewById(R.id.btnGuardar);
        TextView btnCancelar = (TextView) dialog.findViewById(R.id.btnCancelar);
        textDesde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogMapFragment mapDialog = new DialogMapFragment();
                mapDialog.setTitulo("Escoja su punto de origen");
                mapDialog.setOnLocationSelectedListener(new DialogMapFragment.OnLocationSelectedListener() {
                    @Override
                    public void onLocationSelected(final LatLng latLng) {
                        if (latLng != null) {
                            /*String direccion = AddresLocationHandler.getAddresFromLatLon(getActivity(), latLng);
                            Log.d("Dirección", direccion);
                            textDesde.setText(direccion);*/
                            AddresLocationHandler locationHandlerDesde = new AddresLocationHandler(textDesde);
                            locationHandlerDesde.getAddresFromLatLon(latLng, false);
                            latLngDesde = latLng;
                            mapDialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Debe aumentar el nivel de zoom antes de escoger", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mapDialog.show(getActivity().getSupportFragmentManager(), "Historico");
            }
        });
        textHasta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DialogMapFragment mapDialog = new DialogMapFragment();
                mapDialog.setTitulo("Escoja su punto de destino");
                mapDialog.setOnLocationSelectedListener(new DialogMapFragment.OnLocationSelectedListener() {
                    @Override
                    public void onLocationSelected(final LatLng latLng) {
                        if (latLng != null) {
                            /*String direccion = AddresLocationHandler.getAddresFromLatLon(getActivity(), latLng);
                            Log.d("Dirección", direccion);
                            textHasta.setText(direccion);*/
                            AddresLocationHandler locationHandlerHasta = new AddresLocationHandler(textHasta);
                            locationHandlerHasta.getAddresFromLatLon(latLng, false);
                            latLngHasta = latLng;
                            mapDialog.dismiss();
                        } else {
                            Toast.makeText(getActivity(), "Debe aumentar al máximo nivel de zoom antes de escoger", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mapDialog.show(getActivity().getSupportFragmentManager(), "Historico");
            }
        });
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valido = true;
                ArrayList<TipoTransModel> listTipoTrans = adapterSpinner.getItemsSelected();
                if (textTitle.getText().toString().length() == 0) {
                    valido = false;
                    textTitle.setError("Este campo es obligatorio");
                }
                if (textDesde.getText().toString().length() == 0) {
                    valido = false;
                    textDesde.setError("Este campo es obligatorio");
                }
                if (textHasta.getText().toString().length() == 0) {
                    valido = false;
                    textHasta.setError("Este campo es obligatorio");
                }
                if (textDesdeRef.getText().toString().length() == 0) {
                    valido = false;
                    textDesdeRef.setError("Este campo es obligatorio");
                }
                if (textHastaRef.getText().toString().length() == 0) {
                    valido = false;
                    textHastaRef.setError("Este campo es obligatorio");
                }
                if (textMaxDias.getText().toString().length() == 0) {
                    valido = false;
                    textMaxDias.setError("Este campo es obligatorio");
                } else {
                    int maxDias = Integer.valueOf(textMaxDias.getText().toString());
                    if (maxDias < 0) {
                        valido = false;
                        textMaxDias.setError("Debe ser mínimo cero (0)");
                    }
                }
                if (textFecSugEntrega.getText().toString().length() == 0) {
                    valido = false;
                    textInputFecSug.setError("Este campo es obligatorio");
                }
                if (textHoraSugEntrega.getText().toString().length() == 0) {
                    valido = false;
                    textInputHoraSugEntrega.setError("Este campo es obligatorio");
                }
                if (listTipoTrans.size() == 0) {
                    valido = false;
                    Toast.makeText(getActivity(), "Debe escoger al menos un tipo de transporte", Toast.LENGTH_SHORT).show();
                }
                if (valido) {
                    EnvioModel envioInfo = new EnvioModel();
                    envioInfo.setOrigen(latLngDesde);
                    envioInfo.setDestino(latLngHasta);
                    envioInfo.setOrigenRef(textDesdeRef.getText().toString().trim());
                    envioInfo.setDestinoRef(textHastaRef.getText().toString().trim());
                    envioInfo.setMax_dias(textMaxDias.getText().toString());
                    envioInfo.setFechaSugerencia(textFecSugEntrega.getText().toString());
                    envioInfo.setHoraSugerencia(textHoraSugEntrega.getText().toString());
                    envioInfo.setTitle(textTitle.getText().toString());
                    envioInfo.setTransportes(listTipoTrans);
                    envioInfo.setProductos(listaDeProductos);
                    guardarInfoEnvio(envioInfo);
                    dialog.dismiss();
                }
            }
        });
        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void guardarInfoEnvio(EnvioModel envioInfo) {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        int idUsuario = sessionInfo.getUser().getId();

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
        servicioEnvio.solicitarEnvio(idUsuario, envioInfo, new Callback<EnvioModel>() {
            @Override
            public void success(EnvioModel envioModel, Response response) {
                //TODO traer el estatus del envio desde el servidor
                Snackbar.make(btnNuevoEnvio, "Solicitud exitosa", Snackbar.LENGTH_LONG).show();
                Toast.makeText(getContext(), "Recuerda agregar fotos a tus productos", Toast.LENGTH_LONG).show();
                adaptadorPagerEnvio.addEnvio(envioModel);
                dialogLoading.dismiss();
                BaseActivity activity = (BaseActivity) getActivity();
                activity.abrirDetalleEnvio(envioModel, PagerDetalleEnviosAdapter.PESTAÑA_PRODUCTOS, false);
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                /*Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());
                try {
                    if (error.getResponse().getBody() != null) {
                        Log.d("ERROR", convertStreamToString(error.getResponse().getBody().in()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                Toast.makeText(getContext(), "Problemas de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(contextMenu);
        sv.setOnQueryTextListener(this);
        item.setActionView(sv);
    }
}
