package com.acarreos.creative.CustomViews;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.acarreos.creative.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by EnmanuelPc on 07/09/2015.
 */
public class DialogMapFragment extends DialogFragment implements OnMapReadyCallback {

    private SupportMapFragment fragment;

    private OnLocationSelectedListener locationSelectedListener;

    public String tituloHead;

    private TextView tituloDialog;

    public DialogMapFragment() {
        fragment = new SupportMapFragment();
    }

    public void setTitulo(String titulo) {
        tituloHead = titulo;
    }

    public void setOnLocationSelectedListener(OnLocationSelectedListener locationSelectedListener) {
        this.locationSelectedListener = locationSelectedListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_map_view, container, false);
        getDialog().setTitle("");
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.mapView, fragment).commit();
        fragment.getMapAsync(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tituloDialog = (TextView) getView().findViewById(R.id.tituloDialog);
        tituloDialog.setText(tituloHead);
    }

    public SupportMapFragment getFragment() {
        return fragment;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        LatLng panama = new LatLng(8.9753354, -79.5267459);
        googleMap.addMarker(new MarkerOptions().position(panama).title("Marker in Panama"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(panama));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(panama, 12.0f));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (googleMap.getCameraPosition().zoom >= googleMap.getMaxZoomLevel() - 7) {
                    Log.d("Posicion", "Lat:" + latLng.latitude + " - Lon:" + latLng.longitude);
                    locationSelectedListener.onLocationSelected(latLng);
                } else {
                    locationSelectedListener.onLocationSelected(null);
                }
            }
        });
    }

    public static interface OnLocationSelectedListener {

        public void onLocationSelected(LatLng latLng);

    }
}
