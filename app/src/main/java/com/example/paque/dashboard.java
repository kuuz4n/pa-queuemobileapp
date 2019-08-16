package com.example.paque;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class dashboard extends AppCompatActivity {

    ImageButton imgbutton1,imgbutton2, imgbutton3;

    TextView txtbalance,text1,text2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_activity);

        imgbutton1 = findViewById(R.id.sample);
        imgbutton2 = findViewById(R.id.sample2);
        imgbutton3 = findViewById(R.id.camera);
        txtbalance = findViewById(R.id.yourbalance);
        text1 =  findViewById(R.id.jeeptext);
        text2 = findViewById(R.id.mrttext);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.aleo_bold);
        txtbalance.setTypeface(typeface);
        text1.setTypeface(typeface);
        text2.setTypeface(typeface);



        walletBalance();

        imgbutton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLatestNumber();
            }
        });

        imgbutton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLatestNumber();
            }
        });

        imgbutton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Toast.makeText(dashboard.this, "Saved!", Toast.LENGTH_SHORT);
                startActivityForResult(intent, 0);
            }
        });




    }
    public void getLatestNumber(){

            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.43.191:8080/current_number/1/",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            if (response.length() != 0) {
                                Intent intent = new Intent(dashboard.this, jeep_queue.class);
                                intent.putExtra("CURRENT_NUMBER_KEY", response);
                                startActivity(intent);

                            } else {
                                Toast.makeText(dashboard.this, "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(dashboard.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                            Log.wtf("Error", error);
                        }
                    }) {
            };


            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            stringRequest.setShouldCache(false);
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            requestQueue.add(stringRequest);
    }

    public void walletBalance(){
        SharedPreferences sharedPreferences = this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String username = sharedPreferences.getString(Config.USERNAME_SHAREDPREF,"null");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://192.168.43.191:8080/get_wallet?user_id="+username,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("wallet_value").length() != 0) {
                                    txtbalance.setText("Your Balance is : " + jsonObject.getString("wallet_value"));

                            } else {
                                Toast.makeText(dashboard.this, "Something Bad Happened!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(dashboard.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        Log.wtf("Error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", username );
                return params;
            }
        };


        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(policy);
        stringRequest.setShouldCache(false);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    public void onRestart()
    {
        super.onRestart();
        finish();
        startActivity(getIntent());
        walletBalance();
    }
}
