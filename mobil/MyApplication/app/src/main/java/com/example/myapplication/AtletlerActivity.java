package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.toolbox.JsonArrayRequest;


import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AtletlerActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AtletAdapter adapter;
    private List<Atlet> atletList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atletler);

        recyclerView = findViewById(R.id.recyclerViewatletler);
        adapter = new AtletAdapter(this, atletList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AtletlerActivity.this, MainPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        Button drinkButton = findViewById(R.id.icecek_buton);
        drinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AtletlerActivity.this, DrinkActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        // Atlet verilerini almak için metod çağırın
        getAtletDataFromAPI();
    }

    private void getAtletDataFromAPI() {
        String url = "http://93.95.26.206:8080/athletes"; // API URL'si

        RequestQueue queue = Volley.newRequestQueue(this);

        // JSON dizisi olarak isteği yapın
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Yanıtı işleyin
                        try {
                            // Atletler dizisini işle
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject atletObject = response.getJSONObject(i);
                                // Atlet özelliklerini al ve işle
                                String fullName = atletObject.getString("full_name");
                                String nationality = atletObject.getString("nationality");
                                String age = atletObject.getString("age");
                                String disciplines = atletObject.getString("disciplines");
                                // Yeni Atlet nesnesi oluştur ve listeye ekle
                                Atlet atlet = new Atlet(fullName, nationality, age, disciplines);
                                atletList.add(atlet);
                            }
                            // Adapter'a değişiklikleri bildir
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hata durumunda işlemleri ele alın
                        Log.e("API Hatası", "Hata Mesajı: " + error.getMessage());
                        error.printStackTrace(); // Hatanın izini tam olarak görmek için stack trace'i yazdırın
                    }
                }
        );

        // İstek kuyruğuna isteği ekle
        queue.add(jsonArrayRequest);
    }
}



