package com.acarreos.creative.Util;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.widget.TextView;

import com.acarreos.creative.Models.AddressModel;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.http.GET;

/**
 * Created by EnmanuelPc on 07/09/2015.
 */
public class AddresLocationHandler {

    private static final OkHttpClient client = new OkHttpClient();

    private static final String RUTA_API_MAPS = "http://maps.google.com/maps/api";

    private TextView textView;

    public static int MAX_LENGTH_DIRECCION = 20;

    public AddresLocationHandler(TextView textView) {
        this.textView = textView;
    }

    public static void getLatLongFromAddress(String youraddress) throws IOException {
        String uri = "http://maps.google.com/maps/api/geocode/json?address=" +
                youraddress + "&sensor=false";
        Request request = new Request.Builder()
                .url(uri)
                .build();
        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Error in the response: " + response);
        }
        System.out.println(response.body().string());


        /*HttpGet httpGet = new HttpGet(uri);
        HttpClient client = new DefaultHttpClient();
        HttpResponse response;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());

            double lng = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lng");

            double lat = ((JSONArray) jsonObject.get("results")).getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location")
                    .getDouble("lat");

            Log.d("latitude", "" + lat);
            Log.d("longitude", "" + lng);
        } catch (JSONException e) {
            e.printStackTrace();
        }*/

    }

    public static String getAddresFromLatLon1(Context context, LatLng latLng) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String direccion = "Dirección especificada por latitud y longitud";
        if (addresses != null) {
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                direccion = "";
                if (address.getLocality() != null) {
                    direccion = direccion + address.getLocality() + ", ";
                }
                if (address.getAdminArea() != null) {
                    direccion = direccion + address.getAdminArea() + ", ";
                }
                if (address.getCountryName() != null) {
                    direccion = direccion + address.getCountryName() + ", ";
                }
                if (address.getPostalCode() != null) {
                    direccion = direccion + address.getPostalCode() + ", ";
                }
                if (address.getFeatureName() != null) {
                    direccion = direccion + address.getFeatureName();
                }
            }
        }
        Log.d("Empzao", "Ubicado: " + direccion);
        return direccion;
    }

    public void getAddresFromLatLon(final LatLng latLng, final boolean recortarAddress) {
        RestAdapter.Builder builder = new RestAdapter.Builder()
                .setEndpoint("http://maps.google.com/maps/api");

        builder.setRequestInterceptor(new RequestInterceptor() {
            @Override
            public void intercept(RequestFacade request) {
                request.addQueryParam("latlng", latLng.latitude + "," + latLng.longitude);
                request.addQueryParam("sensor", "false");
            }
        });
        textView.setText("Dirección especificada por latitud y longitud");
        RestAdapter adapter = builder.build();
        DireccionPeticion servicioDirecciones = adapter.create(DireccionPeticion.class);
        servicioDirecciones.obtenerPeticion(new Callback<RespuestaServicioDireccion>() {
            @Override
            public void success(RespuestaServicioDireccion resultado, retrofit.client.Response response) {
                if (!resultado.results.isEmpty()) {
                    Log.d("Ruta", resultado.results.get(0).getFormattedAddress());
                    String direccion = resultado.results.get(0).getFormattedAddress();
                    if (textView != null) {
                        textView.setText(resultado.results.get(0).getFormattedAddress());
                        if (direccion.length() > MAX_LENGTH_DIRECCION && recortarAddress) {
                            textView.setText(direccion);
                            //textView.setText(direccion.substring(0, MAX_LENGTH_DIRECCION - 3) + "...");
                        } else {
                            textView.setText(direccion);
                        }
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Ruta", "Error " + error.getUrl());
            }
        });

    }

    public interface DireccionPeticion {

        @GET("/geocode/json")
        void obtenerPeticion(Callback<RespuestaServicioDireccion> respuestaServidor);

    }

    public class RespuestaServicioDireccion {
        public List<AddressModel> results;
    }

}
