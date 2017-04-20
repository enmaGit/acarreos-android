package com.acarreos.creative.Adapters.Fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.acarreos.creative.Fragments.VistaEnvio.FragmentInforEnvio;
import com.acarreos.creative.Fragments.VistaEnvio.FragmentListaOfertas;
import com.acarreos.creative.Fragments.VistaEnvio.FragmentListaProductos;
import com.acarreos.creative.Fragments.VistaEnvio.FragmentUbicacion;
import com.acarreos.creative.Models.EnvioModel;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class PagerDetalleEnviosAdapter extends FragmentPagerAdapter {

    FragmentUbicacion fragmentUbicacion;
    FragmentInforEnvio fragmentInformacion;
    FragmentListaOfertas fragmentPujas;
    FragmentListaProductos fragmentProductos;

    EnvioModel envioInfo;

    public static int PESTAÑA_OFERTAS = 0;
    public static int PESTAÑA_PRODUCTOS = 1;
    public static int PESTAÑA_INFORMACION = 2;
    public static int PESTAÑA_UBICACION = 3;

    private String tabTitles[] =
            new String[]{"Ofertas", "Productos", "Información", "Ubicación"};
    final int PAGE_COUNT = 4;

    public PagerDetalleEnviosAdapter(FragmentManager fm, EnvioModel envioInfo) {
        super(fm);
        this.envioInfo = envioInfo;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (fragmentPujas == null) {
                    fragmentPujas = FragmentListaOfertas.newInstance(envioInfo);
                }
                return fragmentPujas;
            case 1:
                if (fragmentProductos == null) {
                    fragmentProductos = FragmentListaProductos.newInstance(envioInfo);
                }
                return fragmentProductos;
            case 2:
                if (fragmentInformacion == null) {
                    fragmentInformacion = FragmentInforEnvio.newInstance(envioInfo);
                }
                return fragmentInformacion;
            case 3:
                if (fragmentUbicacion == null) {
                    fragmentUbicacion = FragmentUbicacion.newInstance(envioInfo);
                }
                return fragmentUbicacion;
        }
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

    public void pasarDatosDeCamara(Intent data) {
        fragmentProductos.addDataFromCamara(data);
    }

    public void toggleCargaContinua() {
        fragmentUbicacion.toggleCargaContinua();
    }
}
