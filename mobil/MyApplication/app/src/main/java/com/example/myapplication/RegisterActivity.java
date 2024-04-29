package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableEdgeToEdge();
        setContentView(R.layout.activity_register);

        requestQueue = Volley.newRequestQueue(this);

        findViewById(R.id.main).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                int leftInset = insets.getSystemWindowInsetLeft();
                int topInset = insets.getSystemWindowInsetTop();
                int rightInset = insets.getSystemWindowInsetRight();
                int bottomInset = insets.getSystemWindowInsetBottom();
                v.setPadding(leftInset, topInset, rightInset, bottomInset);
                return insets;
            }
        });

        Button buttonLoginReturn = findViewById(R.id.return_loginscreen);
        buttonLoginReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = ((EditText) findViewById(R.id.name)).getText().toString();
                String password = ((EditText) findViewById(R.id.password)).getText().toString();
                String email = ((EditText) findViewById(R.id.email)).getText().toString();
                String password2 = ((EditText) findViewById(R.id.parola_2)).getText().toString();
                if (!password.equals(password2)) {
                    Toast.makeText(RegisterActivity.this, "Parolalar uyuşmuyor, yeniden giriniz.", Toast.LENGTH_SHORT).show();
                    return; // Metodu burada sonlandır
                }

                registerMethod(email, password, password2, name);
            }
        });
    }

    private void enableEdgeToEdge() {
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    public void registerMethod(String email, String password, String password2, String name) {
        String url = "http://93.95.26.206:8080/register";

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("user_email_address", email);
            jsonBody.put("user_password", password);
            jsonBody.put("user_password2", password2);
            jsonBody.put("user_name", name);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            // Kullanıcı başarıyla oluşturulduğunda geri dönen yanıtı işleyin
                            try {
                                // Yanıtta bir "success" anahtarı varsa, kullanıcı başarıyla oluşturuldu
                                if (response.has("success")) {
                                    String successMessage = response.getString("success");
                                    Toast.makeText(RegisterActivity.this, successMessage, Toast.LENGTH_SHORT).show();

                                    // Kullanıcı başarıyla oluşturulduktan sonra giriş ekranına yönlendirin
                                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                } else {
                                    // Yanıtta bir "error" anahtarı varsa, kullanıcı oluşturulamadı
                                    String errorMessage = response.getString("error");
                                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Register Error", error.toString());
                            Toast.makeText(RegisterActivity.this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
