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
import android.widget.TextView;
import android.widget.Toast;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.RecyclerView.AdaptadorEnviosGanadosList;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.EnvioModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.PeticionesWeb.EnvioPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.OnLoadMoreListener;
import com.acarreos.creative.Util.ReminderSession;
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
public class FragmentListaEnviosGanados extends Fragment {

    public int modoEnvio;

    public static final int MODO_DESARROLLO = 1;
    public static final int MODO_FINALIZADOS = 2;

    public int paginasSolicitar = 1;

    private RecyclerView recView;
    private CircularProgressView progressBar;
    private TextView txtNoEnvios;
    //TODO cambiar el adaptador con la nueva interfaz de envíos
    private AdaptadorEnviosGanadosList adapatadorListaEnvios;

    private OnLoadMoreListener loadMoreListener;

    public ArrayList<EnvioModel> listaDeEnvios;


    public static FragmentListaEnviosGanados newInstance(int modo) {
        FragmentListaEnviosGanados fragment = new FragmentListaEnviosGanados();
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recView = (RecyclerView) getView().findViewById(R.id.recView);
        progressBar = (CircularProgressView) getView().findViewById(R.id.progressBar);
        txtNoEnvios = (TextView) getView().findViewById(R.id.txtNoEnvios);
        if (modoEnvio == MODO_FINALIZADOS) {
            txtNoEnvios.setText("Termina uno de tus envíos");
        } else {
            txtNoEnvios.setText("Busca un nuevo envío");
        }
        recView.setHasFixedSize(true);
        listaDeEnvios = new ArrayList<>();
        paginasSolicitar = 0;
        swipeRefreshListSolic = (SwipeRefreshLayout) getView().findViewById(R.id.swipeRefreshSolicitudes);
        swipeRefreshListSolic.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                paginasSolicitar = 0;
                adapatadorListaEnvios.vaciar();
                txtNoEnvios.setVisibility(View.GONE);
                listaDeEnvios.clear();
                obtenerEnviosGanados();
            }
        });
        adapatadorListaEnvios = new AdaptadorEnviosGanadosList(getActivity(), null);
        progressBar.setVisibility(View.VISIBLE);
        txtNoEnvios.setVisibility(View.GONE);
        recView.setAdapter(adapatadorListaEnvios);
        recView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        loadMoreListener = new OnLoadMoreListener(new OnLoadMoreListener.OnEndListListener() {
            @Override
            public void onLoadMore() {
                Toast.makeText(getActivity(), "Buscando más envíos...", Toast.LENGTH_SHORT).show();
                obtenerEnviosGanados();
            }
        });
        recView.addOnScrollListener(loadMoreListener);
        obtenerEnviosGanados();
    }

    private void obtenerEnviosGanados() {
        //TODO hacer el paginado de los envios
        paginasSolicitar++;
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        if (sessionInfo != null) {
            final String token = sessionInfo.getToken();
            int idUsuario = sessionInfo.getUser().getId();

            OkHttpClient client = new OkHttpClient();
            client.interceptors().add(new AuthRequestInterceptor((BaseActivity) getActivity()));

            RestAdapter.Builder builder = new RestAdapter.Builder()
                    .setEndpoint(UrlsServer.RUTA_SERVER)
                    .setClient(new OkClient(client))
                    .setLogLevel(RestAdapter.LogLevel.FULL);

            builder.setRequestInterceptor(new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
                    request.addQueryParam("token", token);
                    if (modoEnvio == MODO_DESARROLLO) {
                        String valueParam = Integer.toString(EnvioModel.STATUS_DESARROLLO);
                        request.addQueryParam("estatus", valueParam);
                    } else if (modoEnvio == MODO_FINALIZADOS) {
                        String valueParam = Integer.toString(EnvioModel.STATUS_FINALIZADO);
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
                            txtNoEnvios.setVisibility(View.VISIBLE);
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
                    } else {
                        if (recView != null) {
                            if (recView.isAttachedToWindow()) {
                                Snackbar.make(recView, "Error de conexión, intente de nuevo", Snackbar.LENGTH_SHORT).show();
                                Log.d("ERROR ENORME", error.toString());
                            }
                            Log.d("ERROR", error.toString());
                            Log.d("ERROR", error.getMessage());
                            Log.d("ERROR", error.getUrl());
                        }
                    }
                    if (swipeRefreshListSolic.isRefreshing()) {
                        swipeRefreshListSolic.setRefreshing(false);
                    }
                }
            });
        }

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
