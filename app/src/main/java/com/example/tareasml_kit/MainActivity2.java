package com.example.tareasml_kit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity2 extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener {
    GoogleMap mapa;

    private ArrayList listInformacion ;
    private ArrayAdapter adaptador1;
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        listView = findViewById(R.id.listInformcion);
        String codigPais = getIntent().getExtras().getString("key");

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listInformacion=new ArrayList();


        obtenerDatosPorPais(codigPais);
        imagenPais(codigPais);


    }
    public void obtenerDatosPorPais(String code) {
        String url = "http://www.geognos.com/api/en/countries/info/"+code+".json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1= jsonObject.getJSONObject("Results");
                        String nombrePais=jsonObject1.getString("Name");
                        JSONObject jsonObjectCapitales=jsonObject1.getJSONObject("Capital");
                        JSONObject jsonObjectGeorrectangle=jsonObject1.getJSONObject("GeoRectangle");
                        String TelPref=jsonObject1.getString("TelPref").toString();
                        JSONObject jsonObjectTelCountryCodes=jsonObject1.getJSONObject("CountryCodes");

                        listInformacion.add("País:"+nombrePais);
                        listInformacion.add("Capital:"+jsonObjectCapitales.getString("Name"));
                        listInformacion.add("Code ISO 2:"+jsonObjectTelCountryCodes.getString("iso2"));
                        listInformacion.add("Code ISO Num:"+jsonObjectTelCountryCodes.getString("isoN"));
                        listInformacion.add("Code ISO 3:"+jsonObjectTelCountryCodes.getString("iso3")   );
                        listInformacion.add("Code FIPS:"+jsonObjectTelCountryCodes.getString("fips"));
                        listInformacion.add("Tel Prefix:"+TelPref);

                        String rect="West"+jsonObjectGeorrectangle.getString("West")+"\n"+
                                "East"+jsonObjectGeorrectangle.getString("East")+"\n"+
                                "North"+jsonObjectGeorrectangle.getString("North")+"\n"+
                                "South"+jsonObjectGeorrectangle.getString("South")+"\n";

                        String rect_2=""+jsonObjectGeorrectangle.getString("West")+";"+
                                ""+jsonObjectGeorrectangle.getString("East")+";"+
                                ""+jsonObjectGeorrectangle.getString("North")+";"+
                                ""+jsonObjectGeorrectangle.getString("South")+";";
                        listInformacion.add("Rectngle:"+rect);
                        poner(listInformacion);
                        marcaPais(rect_2);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) ;

        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public void imagenPais(String code) {
        String UrlRegions = "http://www.geognos.com/api/en/countries/flag/"+code+".png";
        ImageView imageView=findViewById(R.id.imageView);
        Picasso.get().load(UrlRegions).resize(700,500).centerCrop().into(imageView);
    }
    public void marcaPais(String posi)
    {

        LatLng nor_oeste = new LatLng(Double.parseDouble(posi.split(";")[2]) , Double.parseDouble(posi.split(";")[0]));
        LatLng nor_este = new LatLng(Double.parseDouble(posi.split(";")[2]) , Double.parseDouble(posi.split(";")[1]));
        LatLng sur_este = new LatLng(Double.parseDouble(posi.split(";")[3]) , Double.parseDouble(posi.split(";")[1]));
        LatLng sur_oeste = new LatLng(Double.parseDouble(posi.split(";")[3]) , Double.parseDouble(posi.split(";")[0]));
        LatLng noreste = new LatLng(Double.parseDouble(posi.split(";")[2]) , Double.parseDouble(posi.split(";")[0]));

        PolylineOptions lineas = new PolylineOptions()
                .add(nor_oeste)
                .add(nor_este)
                .add(sur_este)
                .add(sur_oeste)
                .add(noreste);

        lineas.width(8);
        lineas.color(Color.RED);
        mapa.addPolyline(lineas);

    }

    public void poner(ArrayList listInformacion2 )
    {
        adaptador1=new ArrayAdapter(this,android.R.layout.simple_list_item_1,listInformacion2);
        listView.setAdapter(adaptador1);
    }

    @Override
    public void onMapClick( LatLng latLng) {
        Projection proj = mapa.getProjection();
        Point coord = proj.toScreenLocation(latLng);
    }

    @Override
    public void onMapReady( GoogleMap googleMap) {
        mapa = googleMap;

        mapa.getUiSettings().setZoomControlsEnabled(true);

        mapa.setOnMapClickListener(this);
    }
}