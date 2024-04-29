package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DrinkActivity extends AppCompatActivity implements RedbullIcecekAdapter.OnSepeteEkleClickListener{

    private RecyclerView rvRedbullIcecekler;
    private RedbullIcecekAdapter adapter;
    private RequestQueue requestQueue;
    private List<RedbullIcecek> icecekler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activitiy_drink);

        Button atletButton = findViewById(R.id.atlet_button);
        atletButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrinkActivity.this, AtletlerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DrinkActivity.this, MainPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        rvRedbullIcecekler = findViewById(R.id.rv_redbull_icecekler);
        rvRedbullIcecekler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RedbullIcecekAdapter(this, this);
        rvRedbullIcecekler.setAdapter(adapter);
        icecekler = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(this);
        icecekGetir();
    }

    private void icecekResimGetir(int id, RedbullIcecek icecek) {
        String url = "http://93.95.26.206:8080/drinks/" + id + "/image";

        ImageRequest request = new ImageRequest(url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        // Resim başarıyla yüklendiğinde
                        icecek.setResim(response); // RedbullIcecek nesnesine resmi ekle
                        adapter.notifyDataSetChanged(); // RecyclerView'i güncelle
                    }
                },
                0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hata durumunda Toast mesajı göster
                        Toast.makeText(DrinkActivity.this, "Error loading image: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Request kuyruğuna ekle
        requestQueue.add(request);
    }

    private void icecekGetir() {
        String url = "http://93.95.26.206:8080/drinks";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject icecekObject = response.getJSONObject(i);

                                int id = icecekObject.getInt("id");
                                String name = icecekObject.getString("name");
                                String description = icecekObject.getString("description");
                                int volume = icecekObject.getInt("volume");

                                // Yeni içecek oluştur
                                RedbullIcecek icecek = new RedbullIcecek(id, name, description, volume);

                                // Resmi getir ve içecek nesnesine ekle
                                icecekResimGetir(id, icecek);

                                // Adapter'a ekle
                                adapter.ekle(icecek);

                                icecekler.add(icecek);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hata durumunda Toast mesajı göster
                        Toast.makeText(DrinkActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        // Request kuyruğuna ekle
        requestQueue.add(request);
    }

    @Override
    public void onSepeteEkleClick(int position) {
        if (position >= 0 && position < icecekler.size()) {
            RedbullIcecek icecek = icecekler.get(position);
            sepeteEkle(icecek.getId());
        }
    }

    private void sepeteEkle(int drinkId) {
        String url = "http://93.95.26.206:8080/basket/add";
        int userId = 1; // Örnek olarak sabit bir kullanıcı kimliği kullandım, burada gerçek kullanıcı kimliğini almanız gerekebilir.

        // Parametreleri oluştur
        Map<String, String> params = new HashMap<>();
        params.put("user_id", String.valueOf(userId));
        params.put("drink_id", String.valueOf(drinkId));
        params.put("quantity", "1"); // Örnek olarak miktarı 1 olarak belirledim, isteğe bağlı olarak kullanıcı tarafından belirlenebilir.

        // Request oluştur
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String message = response.getString("message");
                            // Başarılı cevap durumunda burada işlem yapabilirsiniz
                            Toast.makeText(DrinkActivity.this, message, Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            // JSON yanıtı işlenirken bir hata oluştu
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Hata durumunda burada işlem yapabilirsiniz
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorMessage = new String(error.networkResponse.data);
                            Toast.makeText(DrinkActivity.this, "Hata: " + errorMessage, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DrinkActivity.this, "Bir hata oluştu", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Request kuyruğuna ekle
        requestQueue.add(request);
    }


    // RedbullIcecek class (create this class)
    public static class RedbullIcecek {
        private int id;
        private String name;
        private String description;
        private int volume;
        private Bitmap resim;

        public RedbullIcecek(int id, String name, String description, int volume) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.volume = volume;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public int getVolume() {
            return volume;
        }

        public Bitmap getResim() {
            return resim;
        }

        public void setResim(Bitmap resim) {
            this.resim = resim;
        }
    }
}
