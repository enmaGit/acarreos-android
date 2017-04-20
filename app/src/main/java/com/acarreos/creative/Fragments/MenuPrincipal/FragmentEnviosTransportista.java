package com.acarreos.creative.Fragments.MenuPrincipal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerEnviosGanadosAdapter;
import com.acarreos.creative.R;

/**
 * Created by EnmanuelPc on 03/09/2015.
 */
public class FragmentEnviosTransportista extends Fragment implements SearchView.OnQueryTextListener {

    private FloatingActionButton btnNuevoEnvio;
    private ViewPager viewPager;
    private CollapsingToolbarLayout ctlLayout;

    PagerEnviosGanadosAdapter adaptadorPagerEnvio;

    public static FragmentEnviosTransportista newInstance() {
        return new FragmentEnviosTransportista();
    }

    public FragmentEnviosTransportista() {
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

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle("Mis envíos");
        setHasOptionsMenu(true);

        btnNuevoEnvio = (FloatingActionButton) getView().findViewById(R.id.btnNuevoEnvio);
        btnNuevoEnvio.hide();

        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);

        adaptadorPagerEnvio = new PagerEnviosGanadosAdapter(getChildFragmentManager());

        viewPager.setAdapter(adaptadorPagerEnvio);

        tabLayout.setupWithViewPager(viewPager);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.add("Search");
        item.setIcon(android.R.drawable.ic_menu_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        SearchView sv = new SearchView(getActivity());
        sv.setOnQueryTextListener(this);
        item.setActionView(sv);
        adaptadorPagerEnvio.filtrarEnvios("");
    }
}
