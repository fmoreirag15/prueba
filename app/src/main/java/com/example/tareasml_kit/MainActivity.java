package com.example.tareasml_kit;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.JsonObject;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
     //---------------------VRIABLES-----------------------------//
    //---------------------Firebase----------------------//
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //-------------------------------------------------//

    //-----Identificación-||-texto-||--idioma--||--Traducción---------//
    TextView resultadoIdentificacion;
    Button btnExplorar;
    EditText txtEscaneado,resultadoTextoImagen;

    private static final String TAG = "MiTag";
    private static  final int STORAGE_PERMISSION_CODE=113;
    ActivityResultLauncher<Intent> activityResultLauncher;

    InputImage inputImage;
    TextRecognizer textReconocido;

    //-------------------------------------------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Firebase();
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();

        resultadoTextoImagen=findViewById(R.id.txtResultadoEscaneo);
        btnExplorar=findViewById(R.id.btnAbrirExplorador);
        textReconocido= TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Intent data= result.getData();
                        Uri image=data.getData();
                        convertirImagen(image);

                    }
                });
        btnExplorar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                activityResultLauncher.launch(intent);

            }
        });
    }
    private void Firebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase= FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference();
        txtEscaneado=findViewById(R.id.txtResultadoEscaneo);
        resultadoIdentificacion=findViewById(R.id.lblIdentificacion);
    }
    //--------------------------------------Converti imagen a texto-------------------------------------//
    private void convertirImagen(Uri image) {
        try{
            inputImage=InputImage.fromFilePath(getApplicationContext(),image);
            Task<Text> result=textReconocido.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<Text>() {
                        @Override
                        public void onSuccess(@NonNull Text text) {
                            resultadoTextoImagen.setText(text.getText().replace("\n",""));
                            consuimirvolley(resultadoTextoImagen.getText().toString());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            resultadoTextoImagen.setText("Error: "+e.getMessage());
                            Log.d(TAG, "Error: "+e.getMessage());
                        }
                    });
        }catch (Exception e)
        {
         Log.d(TAG,"error:"+e.getMessage());
        }
    }
    //----------------------------------------------------volley----------------------------------------------------------//
    public void consuimirvolley(String name) {
        String url = "http://www.geognos.com/api/en/countries/info/all.json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        JSONObject jsonObject1= jsonObject.getJSONObject("Results");
                        Iterator<String> iterator = jsonObject1.keys();
                        while(iterator.hasNext()){
                            String key = iterator.next();
                            JSONObject jsonObject2= jsonObject1.getJSONObject(key);
                            if(jsonObject2.getString("Name").toUpperCase().equals(name.toUpperCase()))
                            {
                                Intent i = new Intent(MainActivity.this,MainActivity2.class);
                                i.putExtra("key",key);
                                startActivity(i);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.e("error is ", "" + error);
            }
        }) ;
        RequestQueue requestQueue;
        requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    //--------------------------------------------Parte de los permisos---------------------------------------------------//
    @Override
    protected void onResume()
    {
        super.onResume();
        checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, STORAGE_PERMISSION_CODE);
    }
    public void checkPermission(String permission, int requestCode)
    {
        if(ContextCompat.checkSelfPermission(MainActivity.this, permission)== PackageManager.PERMISSION_DENIED)
        {
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{permission},requestCode);
        }
    }
    @Override
    public  void onRequestPermissionsResult(int requestCode, @NonNull  String[] permission, @NonNull int[] grantResults )
    {
        super.onRequestPermissionsResult(requestCode, permission, grantResults);
        if(requestCode==STORAGE_PERMISSION_CODE)
        {
          if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
          {Toast.makeText(MainActivity.this,"Acepto los permisos", Toast.LENGTH_SHORT).show();}
        }else
          {Toast.makeText(MainActivity.this,"Permisos denegados", Toast.LENGTH_SHORT).show();}
    }
    //--------------------------------------------Parte de los permisos---------------------------------------------------//


}