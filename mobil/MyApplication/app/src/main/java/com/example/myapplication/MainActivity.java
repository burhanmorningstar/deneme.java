package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import android.widget.EditText;

import org.json.JSONException;
import android.content.Intent;


public class MainActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        Button buttonLogin = findViewById(R.id.button_login);

        Button buttonRegister = findViewById(R.id.signup_loginscreen);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText)findViewById(R.id.email)).getText().toString();
                String password = ((EditText)findViewById(R.id.parola)).getText().toString();
                loginUser(email, password);
            }
        });
    }

    public void loginUser(String email, String password) {
        String url = "http://93.95.26.206:8080/login";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_email_address", email);
            jsonBody.put("user_password", password);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                String userId = response.getString("userId");
                                Log.d("user_id", userId);
                                // Token'i kullanarak bir sonraki ekrana geçiş yap
                                User user  = new User();
                                user.setUserId(Integer.parseInt(userId));
                                Intent intent = new Intent(MainActivity.this, MainPage.class);
                                intent.putExtra("user_id", userId);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Giriş Hatası", error.toString());
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}



