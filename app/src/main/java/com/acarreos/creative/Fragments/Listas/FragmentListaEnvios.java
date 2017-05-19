package com.acarreos.creative.Fragments.Listas;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorEnviosList;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentPrincipalCliente;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.OnLoadMoreListener;
import com.acarreos.creative.Util.ReminderSession;
import com.dd.ShadowLayout;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
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
 * Created by EnmanuelPc on 31/08/2015.
 */
public class FragmentListaEnvios extends Fragment {

    private static final int PAGE_OFERTAS = 1;
    private static final int PAGE_DESARROLLOS = 0;
    public int modoEnvio;

    public static final int MODO_CLIENTE_OFERTA = 1;
    public static final int MODO_CLIENTE_DESARROLLO = 2;
    public static final int MODO_TRANS_SIN_OFERTAR = 3;
    public static final int MODO_TRANS_OFERTADOS = 4;

    public int paginasSolicitar = 1;

    private RecyclerView recView;
    private CircularProgressView progressBar;
    private RelativeLayout conteNoEnvios;
    private AdaptadorEnviosList adapatadorListaEnvios;
    private ShadowLayout btnNuevoEnvio;
    private OnLoadMoreListener loadMoreListener;

    public ArrayList<EnvioModel> listaDeEnvios;

    public static FragmentListaEnvios newInstance(int modo) {
        FragmentListaEnvios fragment = new FragmentListaEnvios();
        fragment.modoEnvio = modo;
        return fragment;
    }

    public void addEnvio(EnvioModel envioInfo) {
        adapatadorListaEnvios.addEnvio(envioInfo);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vistaInflada = inflater.inflate(R.layout.layout_envios_list, null);
        return vistaInflada;
    }

    SwipeRefreshLayout swipeRefreshListSolic;
    LinearLayoutManager mLayoutManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recView = (RecyclerView) getView().findViewById(R.id.recView);

        progressBar = (CircularProgressView) getView().findViewById(R.id.progressBar);
        conteNoEnvios = (RelativeLayout) getView().findViewById(R.id.contenedorNoEnvios);
        btnNuevoEnvio = (ShadowLayout) getView().findViewById(R.id.btnNuevoEnvioOpcional);
        recView.setHasFixedSize(true);
        swipeRefreshListSolic = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshSolicitudes);
        paginasSolicitar = 0;
        swipeRefreshListSolic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                paginasSolicitar = 0;
                adapatadorListaEnvios.vaciar();
                listaDeEnvios.clear();
                if (modoEnvio == MODO_TRANS_SIN_OFERTAR || modoEnvio == MODO_TRANS_OFERTADOS) {
                    obtenerEnviosTranspor();
                } else if (modoEnvio == MODO_CLIENTE_OFERTA || modoEnvio == MODO_CLIENTE_DESARROLLO) {
                    obtenerEnviosClientes();
                }
            }
        });
        adapatadorListaEnvios = new AdaptadorEnviosList(getActivity(), null);
        listaDeEnvios = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        conteNoEnvios.setVisibility(View.GONE);
        btnNuevoEnvio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentPrincipalCliente fragment = (FragmentPrincipalCliente) getParentFragment();
                fragment.abrirDialogCargarProductos();
            }
        });
        recView.setAdapter(adapatadorListaEnvios);
        mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        loadMoreListener = new OnLoadMoreListener(new OnLoadMoreListener.OnEndListListener() {
            @Override
            public void onLoadMore() {
                Toast.makeText(getActivity(), "Buscando más envíos...", Toast.LENGTH_SHORT).show();
                cargarEnvios();
            }
        });
        recView.setLayoutManager(mLayoutManager);
        recView.addOnScrollListener(loadMoreListener);
        cargarEnvios();
    }

    private void cargarEnvios() {
        if (modoEnvio == MODO_TRANS_SIN_OFERTAR || modoEnvio == MODO_TRANS_OFERTADOS) {
            obtenerEnviosTranspor();
        } else if (modoEnvio == MODO_CLIENTE_OFERTA || modoEnvio == MODO_CLIENTE_DESARROLLO) {
            obtenerEnviosClientes();
        }
    }

    private void obtenerEnviosClientes() {
        //TODO hacer el paginado de los envios
        paginasSolicitar++;
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        final String token = sessionInfo.getToken();
        int idUsuario = sessionInfo.getUser().getId();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint(UrlsServer.RUTA_SERVER)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new OkClient(client));

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("token", token);
                if (modoEnvio == MODO_CLIENTE_DESARROLLO) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_DESARROLLO) + "," + Integer.toString(EnvioModel.STATUS_FINALIZADO);
                    request.addQueryParam("estatus", valueParam);
                } else if (modoEnvio == MODO_CLIENTE_OFERTA) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                    request.addQueryParam("estatus", valueParam);
                }
                request.addQueryParam("page", Integer.toString(paginasSolicitar));
            }
        });

        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        servicioEnvio.obtenerEnviosUser(idUsuario, new Callback<List<EnvioModel>>() {
            @Override
            public void success(List<EnvioModel> listaDeEnviosServer, Response response) {
                progressBar.setVisibility(View.GONE);
                if (listaDeEnviosServer.size() == 0) {
                    if (adapatadorListaEnvios.getItemCount() == 0) {
                        conteNoEnvios.setVisibility(View.VISIBLE);
                        btnNuevoEnvio.setVisibility(View.VISIBLE);
                        FragmentPrincipalCliente fragment = (FragmentPrincipalCliente) getParentFragment();
                        if (modoEnvio == MODO_CLIENTE_OFERTA) {
                            fragment.ocultarFloatingButton(PAGE_OFERTAS);
                        } else {
                            fragment.ocultarFloatingButton(PAGE_DESARROLLOS);
                        }
                    }
                } else {
                    FragmentPrincipalCliente fragment = (FragmentPrincipalCliente) getParentFragment();
                    if (modoEnvio == MODO_CLIENTE_OFERTA) {
                        fragment.mostrarFloatingButton(PAGE_OFERTAS);
                    } else {
                        fragment.mostrarFloatingButton(PAGE_DESARROLLOS);
                    }
                    conteNoEnvios.setVisibility(View.GONE);
                    btnNuevoEnvio.setVisibility(View.GONE);
                    for (EnvioModel envioInfo : listaDeEnviosServer) {
                        adapatadorListaEnvios.addEnvio(envioInfo);
                        listaDeEnvios.add(envioInfo);
                    }
                    if (queryBusqueda.length() > 0) {
                        filtrarEnvios(queryBusqueda, false);
                    }
                    //paginasSolicitar++;
                    loadMoreListener.enableLoading();
                }
                if (swipeRefreshListSolic.isRefreshing()) {
                    swipeRefreshListSolic.setRefreshing(false);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressBar.setVisibility(View.GONE);
                if (error.getResponse() == null) {
                } else {
                    if (recView.isAttachedToWindow()) {
                        Snackbar.make(recView, "Error de conexión, intente de nuevo", Snackbar.LENGTH_SHORT).show();
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
                if (modoEnvio == MODO_TRANS_OFERTADOS) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                    request.addQueryParam("estatus", valueParam);
                    request.addQueryParam("ofertado", "1");
                } else if (modoEnvio == MODO_TRANS_SIN_OFERTAR) {
                    String valueParam = Integer.toString(EnvioModel.STATUS_SUBASTA);
                    request.addQueryParam("estatus", valueParam);
                }
                request.addQueryParam("page", Integer.toString(paginasSolicitar));
            }
        });

        RestAdapter adapter = builder.build();
        EnvioPeticiones servicioEnvio = adapter.create(EnvioPeticiones.class);
        Log.d("Trans", "pidiendo: ");
        servicioEnvio.obtenerEnviosTransportista(new Callback<List<EnvioModel>>() {
            @Override
            public void success(List<EnvioModel> listaDeEnviosServer, Response response) {
                progressBar.setVisibility(View.GONE);
                Log.d("Trans", response.getUrl());
                if (listaDeEnviosServer.size() == 0) {
                    if (adapatadorListaEnvios.getItemCount() == 0) {
                        conteNoEnvios.setVisibility(View.VISIBLE);
                    }
                } else {
                    for (EnvioModel envioInfo : listaDeEnviosServer) {
                        adapatadorListaEnvios.addEnvio(envioInfo);
                        listaDeEnvios.add(envioInfo);
                    }
                    if (queryBusqueda.length() > 0) {
                        filtrarEnvios(queryBusqueda, false);
                    }
                    //paginasSolicitar++;
                    loadMoreListener.enableLoading();
                }
                if (swipeRefreshListSolic.isRefreshing()) {
                    swipeRefreshListSolic.setRefreshing(false);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                progressBar.setVisibility(View.GONE);
                if (error.getResponse() == null) {
                    Log.d("Trans", "error: " + error.getUrl());
                } else {
                    if (recView.isAttachedToWindow()) {
                        Snackbar.make(recView, "Erro de conexión, intente de nuevo", Snackbar.LENGTH_SHORT).show();
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

    private String queryBusqueda = "";


    public void filtrarEnvios(String query, boolean schrollUp) {
        this.queryBusqueda = query;
        if (query.length() > 0) {
            final List<EnvioModel> filteredModelList = filter(listaDeEnvios, query);
            adapatadorListaEnvios.animateTo(filteredModelList);
            if (schrollUp) {
                recView.scrollToPosition(0);
            }
        }
    }

    private List<EnvioModel> filter(List<EnvioModel> models, String query) {
        query = query.toLowerCase();

        final List<EnvioModel> filteredModelList = new ArrayList<>();
        for (EnvioModel envioInfo : models) {
            String titulo = envioInfo.getTitle().toLowerCase();
            String userLogin = envioInfo.getCliente().getLogin().toLowerCase();
            String origenRef = envioInfo.getOrigenRef().toLowerCase();
            String destinoRef = envioInfo.getDestinoRef().toLowerCase();
            boolean pasoFiltro = false;
            if (titulo.contains(query) || userLogin.contains(query) || origenRef.contains(query) || destinoRef.contains(query)) {
                pasoFiltro = true;
            }
            if (pasoFiltro) {
                filteredModelList.add(envioInfo);
            }
        }
        return filteredModelList;
    }
}
