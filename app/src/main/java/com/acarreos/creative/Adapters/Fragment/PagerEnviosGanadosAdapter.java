package com.acarreos.creative.Adapters.Fragment;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.acarreos.creative.Fragments.Listas.FragmentListaEnviosGanados;
import com.acarreos.creative.Models.EnvioModel;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class PagerEnviosGanadosAdapter extends FragmentStatePagerAdapter {

    FragmentListaEnviosGanados fragmentEnviosDesarrollo;
    FragmentListaEnviosGanados fragmentEnviosFinalizados;

    final int PAGE_COUNT = 2;
    private String tabTitles[] =
            new String[]{"Desarrollo", "Finalizados"};

    public PagerEnviosGanadosAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (fragmentEnviosDesarrollo == null) {
                    fragmentEnviosDesarrollo = FragmentListaEnviosGanados.newInstance(FragmentListaEnviosGanados.MODO_DESARROLLO);
                }
                return fragmentEnviosDesarrollo;
            case 1:
                if (fragmentEnviosFinalizados == null) {
                    fragmentEnviosFinalizados = FragmentListaEnviosGanados.newInstance(FragmentListaEnviosGanados.MODO_FINALIZADOS);
                }
                return fragmentEnviosFinalizados;
        }
        return null;
    }

    public void addEnvio(EnvioModel envioInfo) {
        if ((envioInfo.getEstatus().getId() == EnvioModel.STATUS_FINALIZADO) || envioInfo.getEstatus().getId() == EnvioModel.STATUS_DESARROLLO) {
            fragmentEnviosDesarrollo.addEnvio(envioInfo);
        } else if ((envioInfo.getEstatus().getId() == EnvioModel.STATUS_SUBASTA) || envioInfo.getEstatus().getId() == EnvioModel.STATUS_CANCELADO) {
            fragmentEnviosFinalizados.addEnvio(envioInfo);
        }
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
        if (fragmentEnviosDesarrollo != null) {
            fragmentEnviosDesarrollo.filtrarEnvios(query, true);
        }
        if (fragmentEnviosFinalizados != null) {
            fragmentEnviosFinalizados.filtrarEnvios(query, true);
        }
    }
}
