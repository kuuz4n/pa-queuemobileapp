package com.example.paque;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class login extends AppCompatActivity implements View.OnClickListener {

    EditText Username, Password;
    Button loginbutton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Username = findViewById(R.id.username);
        Password = findViewById(R.id.password);
        loginbutton = findViewById(R.id.btn_login);

        loginbutton.setOnClickListener(this);
    }

    public void onClick(View v){
        if(v == loginbutton){
            login();
        }
    }

    public void login(){
        final String username = Username.getText().toString();
        final String password = Password.getText().toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.43.191:8080/login/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("status").equalsIgnoreCase("success")) {

                                SharedPreferences sharedPreferences = login.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(Config.USERNAME_SHAREDPREF,username);
                                editor.commit();

                                Toast.makeText(login.this, "Welcome "+ username, Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(login.this, dashboard.class);
                                startActivity(i);
                            } else {
                                Toast.makeText(login.this, "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(login.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        Log.wtf("Error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("password", password);
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


}


