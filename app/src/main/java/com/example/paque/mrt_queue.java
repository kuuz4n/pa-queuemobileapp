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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class mrt_queue extends AppCompatActivity {

    TextView txt1;
    Button btn1;
    ImageView qr;
    String assigned_number;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jeep_queue);

        txt1 = findViewById(R.id.queue_number);
        btn1 = findViewById(R.id.queue_button);
        qr = findViewById(R.id.qr_code);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queue();
            }
        });
        String current_number = getIntent().getStringExtra("CURRENT_NUMBER_KEY");
        txt1.setText(current_number);

    }

    public void queue() {
        SharedPreferences sharedPreferences = this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        final String username = sharedPreferences.getString(Config.USERNAME_SHAREDPREF,"null");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://192.168.43.191:8080/assign_number/",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (jsonObject.getString("assigned_number").length() != 0) {
                                txt1.setText("Your Queue is : " + jsonObject.getString("assigned_number"));
                                assigned_number = jsonObject.getString("assigned_number");
                            } else {
                                Toast.makeText(mrt_queue.this, "Something Bad Happened!", Toast.LENGTH_SHORT).show();
                            }

                            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                            try {
                                BitMatrix bitMatrix = multiFormatWriter.encode("Assigned number is: " + assigned_number + "\n" +"Username: " + username, BarcodeFormat.QR_CODE, 500, 500);
                                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                                qr.setImageBitmap(bitmap);

                            } catch (WriterException e){
                                e.printStackTrace();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(mrt_queue.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        Log.wtf("Error", error);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("queue_id", "1");
                params.put("user_id", username);
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
