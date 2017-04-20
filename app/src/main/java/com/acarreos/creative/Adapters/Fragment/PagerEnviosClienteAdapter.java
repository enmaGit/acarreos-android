package com.acarreos.creative.Adapters.Fragment;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.acarreos.creative.Fragments.Listas.FragmentListaEnvios;
import com.acarreos.creative.Models.EnvioModel;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class PagerEnviosClienteAdapter extends FragmentStatePagerAdapter {

    FragmentListaEnvios fragmentEnviosDesarrollo;
    FragmentListaEnvios fragmentEnviosOferta;

    final int PAGE_COUNT = 2;
    private String tabTitles[] =
            new String[]{"Activos", "Solicitudes"};

    public PagerEnviosClienteAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (fragmentEnviosDesarrollo == null) {
                    fragmentEnviosDesarrollo = FragmentListaEnvios.newInstance(FragmentListaEnvios.MODO_CLIENTE_DESARROLLO);
                }
                return fragmentEnviosDesarrollo;
            case 1:
                if (fragmentEnviosOferta == null) {
                    fragmentEnviosOferta = FragmentListaEnvios.newInstance(FragmentListaEnvios.MODO_CLIENTE_OFERTA);
                }
                return fragmentEnviosOferta;
        }
        return null;
    }

    @Override
    public Parcelable saveState() {
        return null;
    }

    public void addEnvio(EnvioModel envioInfo) {
        if ((envioInfo.getEstatus().getId() == EnvioModel.STATUS_FINALIZADO) || envioInfo.getEstatus().getId() == EnvioModel.STATUS_DESARROLLO) {
            if (fragmentEnviosDesarrollo != null) {
                fragmentEnviosDesarrollo.addEnvio(envioInfo);
            }
        } else if ((envioInfo.getEstatus().getId() == EnvioModel.STATUS_SUBASTA) || envioInfo.getEstatus().getId() == EnvioModel.STATUS_CANCELADO) {
            if (fragmentEnviosOferta != null) {
                fragmentEnviosOferta.addEnvio(envioInfo);
            }
        }
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
        if (fragmentEnviosOferta != null) {
            fragmentEnviosOferta.filtrarEnvios(query, true);
        }
    }
}
