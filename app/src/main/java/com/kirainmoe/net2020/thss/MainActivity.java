package com.kirainmoe.net2020.thss;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindEvents();
    }

    protected void bindEvents() {
        Button loginButton = findViewById(R.id.loginButton);
        MainActivity ref = this;
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.onLogin();
            }
        });
    }


    public void onLogin() {
        EditText usernameView = (EditText) findViewById(R.id.usernameEditText);
        String username = usernameView.getText().toString().trim();

        EditText passwordView = (EditText) findViewById(R.id.passwordEditText);
        String password = passwordView.getText().toString().trim();

        EditText addressView = (EditText) findViewById(R.id.serverAddrEditText);
        String address = addressView.getText().toString().trim();
        if (username.length() == 0) {
            Toast.makeText(this, "Username is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() == 0) {
            Toast.makeText(this, "Password is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (address.length() == 0) {
            Toast.makeText(this, "Server Address is required", Toast.LENGTH_SHORT).show();
            return;
        }

        OkHttpClient client = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();
        Request request = new Request.Builder()
                .url("http://" + address + ":5000/api/user/login")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);

        MainActivity that = this;

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
                        JSONObject loginResult = new JSONObject(result);

                        if (loginResult.getString("status").equals("failed")) {
                            Toast.makeText(that, "Username or password is incorrect.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int uid = loginResult.getInt("uid");
                        String authKey = loginResult.getString("authKey");

                        SharedPreferences sharedPerf = getSharedPreferences("THSS", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPerf.edit();
                        editor.putInt("uid", uid);
                        editor.putString("authKey", authKey);
                        editor.putString("server", address);
                        editor.apply();

                        Intent intent = new Intent();

                        intent.setClass(that, CamerasList.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(that, "Cannot connect to server.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(that, "Cannot connect to server.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}