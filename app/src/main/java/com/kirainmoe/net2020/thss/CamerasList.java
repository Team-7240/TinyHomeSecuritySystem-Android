package com.kirainmoe.net2020.thss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CamerasList extends AppCompatActivity {
    private int uid;
    private String server;
    private String authKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cameras_list);
        setTitle("摄像头列表");

        SharedPreferences sharedPref = getSharedPreferences("THSS", Context.MODE_PRIVATE);
        uid = sharedPref.getInt("uid", -1);
        server = sharedPref.getString("server", "");
        authKey = sharedPref.getString("authKey", "");

        fetchCameras();
    }

    protected void fetchCameras() {
        // not login, uid is invalid
        if (uid == -1) {
            Toast.makeText(this, "User not login!", Toast.LENGTH_SHORT).show();
            return;
        }
        // fetch datas
        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("uid", String.valueOf(uid))
                .add("authKey", authKey)
                .build();
        Request request = new Request.Builder()
                .url("http://" + server + ":5000/api/camera/get")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        CamerasList that = this;

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Looper.prepare();
                Toast.makeText(that, "Cannot connect to server.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Looper.prepare();

                if (response.isSuccessful()) {
                    String result = response.body().string();
                    try {
                        JSONObject camerasResult = new JSONObject(result);

                        if (camerasResult.getString("status").equals("failed")) {
                            Toast.makeText(that, "Cannot retrieve cameras list!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JSONArray payload = camerasResult.getJSONArray("payload");

                        ListView listView = (ListView) findViewById(R.id.camerasListView);
                        ArrayList<HashMap<String, String>> listItem = new ArrayList<HashMap<String, String>>();

                        if (payload == null) {
                            return;
                        }

                        for (int i = 0; i < payload.length(); i++) {
                            JSONObject cam = payload.getJSONObject(i);

                            String ip = cam.getString("ip"),
                                    name = cam.getString("name");
                            HashMap<String, String> item = new HashMap<String, String>();
                            item.put("ip", "地址：" + ip);
                            item.put("name", "名称：" + name);
                            item.put("raw_name", name);
                            item.put("raw_ip", ip);
                            item.put("type", "ESP32");
                            listItem.add(item);
                        }

                        SimpleAdapter adapter = new SimpleAdapter(that,
                                listItem,
                                R.layout.listitem,
                                new String[]{"name", "ip", "type"},
                                new int[]{R.id.name, R.id.ip, R.id.type});
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                HashMap<String, String> target = listItem.get(position);
                                Intent intent = new Intent(CamerasList.this, PlayerActivity.class);
                                intent.putExtra("name", target.get("raw_name"));
                                intent.putExtra("ip", target.get("raw_ip"));
                                startActivityForResult(intent, 0);
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(that, "Internal error.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(that, "Cannot connect to server.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}