package com.acarreos.creative.Fragments.MenuPrincipal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.acarreos.creative.Activities.BaseActivity;
import com.acarreos.creative.Adapters.Fragment.PagerCuentaTransAdapter;
import com.acarreos.creative.Fragments.Listas.FragmentListaTipoTransportes;
import com.acarreos.creative.Models.SessionModel;
import com.acarreos.creative.Models.UserModel;
import com.acarreos.creative.R;
import com.acarreos.creative.Util.ReminderSession;

/**
 * Created by EnmanuelPc on 06/09/2015.
 */
public class FragmentCuentaTranspor extends Fragment {

    private FloatingActionButton btnActCuenta;
    private ViewPager viewPager;
    private CollapsingToolbarLayout ctlLayout;
    private UserModel userInfo;

    PagerCuentaTransAdapter adaptadorPagerCuentaTrans;

    public static FragmentCuentaTranspor newInstance(UserModel userInfo) {
        FragmentCuentaTranspor fragmentCuentaTranspor = new FragmentCuentaTranspor();
        fragmentCuentaTranspor.userInfo = userInfo;
        return fragmentCuentaTranspor;
    }

    public FragmentCuentaTranspor() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mi_cuenta_trans, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d("Fragment", "Actividad created");

        BaseActivity activity = (BaseActivity) getActivity();
        activity.setupToolbar();
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(userInfo.getNombre() + " " + userInfo.getApellido());
        toolbar.setContentInsetsAbsolute(0, 0);

        btnActCuenta = (FloatingActionButton) getView().findViewById(R.id.btnNuevoEnvio);
        UserModel userActual = new ReminderSession(getActivity()).obtenerInfoSession().getUser();
        if (userActual.getId() != userInfo.getId()) {
            btnActCuenta.setVisibility(View.GONE);
        }

        TabLayout tabLayout = (TabLayout) getView().findViewById(R.id.appbartabs);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);

        viewPager = (ViewPager) getView().findViewById(R.id.viewpager);
        adaptadorPagerCuentaTrans = new PagerCuentaTransAdapter(getChildFragmentManager(), userInfo);

        viewPager.setAdapter(adaptadorPagerCuentaTrans);
        if (userActual.getId() == userInfo.getId()) {
            viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (position == 0) {
                        btnActCuenta.show();
                    } else {
                        btnActCuenta.hide();
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        tabLayout.setupWithViewPager(viewPager);
        btnActCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adaptadorPagerCuentaTrans.actualizarDatosCuenta();
            }
        });
    }

    public void actualizarToolbar() {
        SessionModel sessionInfo = new ReminderSession(getActivity()).obtenerInfoSession();
        userInfo = sessionInfo.getUser();
        Toolbar toolbar = (Toolbar) getView().findViewById(R.id.toolbar);
        toolbar.setTitle(userInfo.getNombre() + " " + userInfo.getApellido());
        toolbar.setContentInsetsAbsolute(0, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FragmentListaTipoTransportes.REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            adaptadorPagerCuentaTrans.pasarDatosDeCamara(data);
        }
    }
}
