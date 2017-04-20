package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Constants.UrlsServer;
import com.acarreos.creative.Models.CategoriaProductoModel;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.PeticionesWeb.ProductoPeticiones;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.AuthRequestInterceptor;
import com.acarreos.creative.Util.ReminderSession;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
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
public class FragmentPrincipalAdmin extends Fragment {

    private CollapsingToolbarLayout ctlLayout;
    private UserModel userInfo;

    private MaterialSpinner spinnerCategoria;
    private RelativeLayout btnSaveComision;

    private TextInputLayout tilComision;
    private EditText txtComision;

    private List<CategoriaProductoModel> listaDeCategorias;


    public static FragmentPrincipalAdmin newInstance() {
        return new FragmentPrincipalAdmin();
    }

    public FragmentPrincipalAdmin() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_comision, container, false);
        return itemView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("Fragment", "Actividad created");
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        userInfo = sessionInfo.getUser();

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle("Comisiones");

        spinnerCategoria = (MaterialSpinner) getView().findViewById(R.id.spinnerCategoria);
        btnSaveComision = (RelativeLayout) getView().findViewById(R.id.btnSaveComision);

        tilComision = (TextInputLayout) getView().findViewById(R.id.tilComision);
        txtComision = (EditText) getView().findViewById(R.id.textComision);

        btnSaveComision.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                guardarDatosComision();
            }
        });
        obtenerCategorias();
    }

    private void guardarDatosComision() {
        final Dialog dialogLoading = new Dialog(getActivity(), R.style.Theme_Dialog_Translucent);
        dialogLoading.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogLoading.setContentView(R.layout.dialog_loading);
        dialogLoading.setCancelable(false);
        dialogLoading.show();


        final int posisitionSelected = spinnerCategoria.getSelectedItemPosition() - 1;
        CategoriaProductoModel cateInfo = listaDeCategorias.get(posisitionSelected);
        cateInfo.setComision(Integer.valueOf(txtComision.getText().toString()));
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
        ProductoPeticiones servicioActProducto = adapter.create(ProductoPeticiones.class);
        servicioActProducto.modificarCategoriaProducto(cateInfo.getId(), cateInfo, new Callback<CategoriaProductoModel>() {
            @Override
            public void success(CategoriaProductoModel categoriaProductoModel, Response response) {
                Snackbar.make(btnSaveComision, "Solicitud exitosa", Snackbar.LENGTH_LONG).show();
                CategoriaProductoModel cateVieja = listaDeCategorias.get(posisitionSelected);
                cateVieja.setComision(categoriaProductoModel.getComision());
                listaDeCategorias.set(posisitionSelected, cateVieja);
                configurarSpinner(listaDeCategorias);
                spinnerCategoria.setSelection(posisitionSelected + 1);
                dialogLoading.dismiss();
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                Log.d("ERROR", error.toString());
                Log.d("ERROR", error.getMessage());
                Log.d("ERROR", error.getUrl());
                try {
                    if (error.getResponse().getBody() != null) {
                        Log.d("ERROR", convertStreamToString(error.getResponse().getBody().in()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Snackbar.make(btnSaveComision, "Problemas de conexión", Snackbar.LENGTH_LONG).show();
            }
        });
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
            public void success(List<CategoriaProductoModel> listaDeCategoriasP, Response response) {
                dialogLoading.dismiss();
                listaDeCategorias = listaDeCategoriasP;
                configurarSpinner(listaDeCategoriasP);
            }

            @Override
            public void failure(RetrofitError error) {
                dialogLoading.dismiss();
                if (error.getResponse() == null) {
                    Snackbar.make(ctlLayout, "Error de conexión", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(ctlLayout, "Todo grave " + error.toString(), Snackbar.LENGTH_SHORT).show();
                    Log.d("ERROR", error.toString());
                    Log.d("ERROR", error.getMessage());
                    Log.d("ERROR", error.getUrl());
                }

            }
        });
    }

    private void configurarSpinner(final List<CategoriaProductoModel> listaDeCategorias) {
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
                    txtComision.setText("");
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                    txtComision.setText(Integer.toString(listaDeCategorias.get(position).getComision()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

}
