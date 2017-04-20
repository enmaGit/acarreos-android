package com.acarreos.creative.Adapters.Fragment;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.acarreos.creative.Fragments.Listas.FragmentListaTipoTransportes;
import com.acarreos.creative.Fragments.MenuPrincipal.FragmentInfoCuentaTrans;
import com.acarreos.creative.Models.UserModel;

/**
 * Created by EnmanuelPc on 31/08/2015.
 */
public class PagerCuentaTransAdapter extends FragmentPagerAdapter {

    FragmentInfoCuentaTrans fragmentInfoCuentaTrans;
    FragmentListaTipoTransportes fragmentListaTrans;
    UserModel userInfo;

    final int PAGE_COUNT = 2;
    private String tabTitles[] =
            new String[]{"Cuenta", "Transportes"};

    public PagerCuentaTransAdapter(FragmentManager fm, UserModel userInfo) {
        super(fm);
        this.userInfo = userInfo;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                if (fragmentInfoCuentaTrans == null) {
                    fragmentInfoCuentaTrans = FragmentInfoCuentaTrans.newInstance(userInfo);
                }
                return fragmentInfoCuentaTrans;
            case 1:
                if (fragmentListaTrans == null) {
                    fragmentListaTrans = FragmentListaTipoTransportes.newInstance(userInfo);
                }
                return fragmentListaTrans;
        }
        return null;
    }

    public void pasarDatosDeCamara(Intent data) {
        fragmentListaTrans.addDataFromCamara(data);
    }

    public void actualizarDatosCuenta() {
        fragmentInfoCuentaTrans.abrirDialogActDatos();
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
}
