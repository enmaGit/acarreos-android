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
import com.acarreos.creative.Adapters.Fragment.PagerEnviosTransporAdapter;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;

/**
 * Created by EnmanuelPc on 12/10/2015.
 */
public class FragmentPrincipalTranspor extends Fragment implements SearchView.OnQueryTextListener {

    private FloatingActionButton btnNuevoEnvio;
    private ViewPager viewPager;
    private CollapsingToolbarLayout ctlLayout;
    private UserModel userInfo;

    PagerEnviosTransporAdapter adaptadorPagerEnvio;

    public static FragmentPrincipalTranspor newInstance() {
        return new FragmentPrincipalTranspor();
    }

    public FragmentPrincipalTranspor() {
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
        userInfo = sessionInfo.getUser();
        if (sessionInfo == null) {
            BaseActivity activity = (BaseActivity) getActivity();
            activity.abrirPantallaLogin();
            return;
        }

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();

        ctlLayout = (CollapsingToolbarLayout) getView().findViewById(R.id.ctlLayout);
        ctlLayout.setTitle("Envíos");
        setHasOptionsMenu(true);

        //TODO ver que vamos a hacer con ese snack

        btnNuevoEnvio = (FloatingActionButton) getView().findViewById(R.id.btnNuevoEnvio);

        btnNuevoEnvio.hide();

        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);

        adaptadorPagerEnvio = new PagerEnviosTransporAdapter(getChildFragmentManager());

        viewPager.setAdapter(adaptadorPagerEnvio);

        tabLayout.setupWithViewPager(viewPager);
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

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adaptadorPagerEnvio.filtrarEnvios(query.trim());
        return true;
    }
}
