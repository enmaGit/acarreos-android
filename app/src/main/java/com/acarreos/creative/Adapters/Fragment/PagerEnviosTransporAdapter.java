package com.acarreos.creative.Adapters.Fragment;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.acarreos.creative.Fragments.Listas.FragmentListaEnvios;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentUbicacionEnvios;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class PagerEnviosTransporAdapter extends FragmentStatePagerAdapter {

    private FragmentListaEnvios fragmentEnviosSinOfertar;
    private FragmentListaEnvios fragmentEnviosOfertados;
    private FragmentUbicacionEnvios fragmentUbicacionEnvios;

    private String tabTitles[] =
            new String[]{"Envíos", "Mapa", "Ofertas"};

    private final int PAGE_COUNT = tabTitles.length;

    public PagerEnviosTransporAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (fragmentEnviosSinOfertar == null) {
                    fragmentEnviosSinOfertar = FragmentListaEnvios.newInstance(FragmentListaEnvios.MODO_TRANS_SIN_OFERTAR);
                }
                return fragmentEnviosSinOfertar;
            case 1:
                if (fragmentUbicacionEnvios == null) {
                    fragmentUbicacionEnvios = FragmentUbicacionEnvios.newInstance();
                }
                return fragmentUbicacionEnvios;
            case 2:
                if (fragmentEnviosOfertados == null) {
                    fragmentEnviosOfertados = FragmentListaEnvios.newInstance(FragmentListaEnvios.MODO_TRANS_OFERTADOS);
                }
                return fragmentEnviosOfertados;
        }
        return null;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    public void filtrarEnvios(String query) {
        if (fragmentEnviosSinOfertar != null) {
            fragmentEnviosSinOfertar.filtrarEnvios(query, true);
        }
        if (fragmentEnviosOfertados != null) {
            fragmentEnviosOfertados.filtrarEnvios(query, true);
        }
    }
}
