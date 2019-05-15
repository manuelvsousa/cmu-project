package com.cmu.p2photo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.cmu.p2photo.cloud.Dropbox;
import com.cmu.p2photo.cloud.util.Config;
import com.cmu.p2photo.wifi.WifiDirect;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity {
    private static final String URL_FEED = "user/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnLogin = findViewById(R.id.loginButton);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText username = findViewById(R.id.LoginUsername);
                EditText password = findViewById(R.id.LoginPassword);
                try {
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("username", username.getText().toString());
                    jsonParams.put("password", password.getText().toString());
                    StringEntity entity = new StringEntity(jsonParams.toString());
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(getApplicationContext(), apiUrl + URL_FEED, entity, "application/json",
                            new JsonHttpResponseHandler() {
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    Log.d(URL_FEED, "response raw: " + response.toString());
                                    try {
                                        Gson gson = new Gson();
                                        Map<String, Object> map = new HashMap<>();
                                        map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());

                                        Log.d(URL_FEED, "Gson converted to map: " + map.toString());
                                        Log.d(URL_FEED, "Session token: " + map.get("token"));
                                        SharedPreferences.Editor editor = getSharedPreferences(sp, MODE_PRIVATE).edit();
                                        editor.clear().commit();
                                        editor.putString("token", map.get("token").toString());
                                        editor.putString("username", username.getText().toString());
                                        editor.apply();
                                        Log.d(URL_FEED, "Passou: " + map.get("token"));
                                        if ((boolean) map.get("success")) {
                                            Switch switch3 = (Switch) findViewById(R.id.switch3);
                                            if (!switch3.isChecked()) {
                                                editor.putBoolean("wifi",false);
                                                editor.apply();
                                                if (map.get("dropbox").toString().equals("")) {
                                                    Toast.makeText(getApplicationContext(), "Welcome " + username.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(MainActivity.this, Dropbox.class);
                                                    intent.putExtra("wifi",false);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    editor.putString("dropbox", map.get("dropbox").toString());
                                                    editor.apply();
                                                    Toast.makeText(getApplicationContext(), "Welcome Back " + username.getText().toString(), Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(MainActivity.this, P2photo.class);
                                                    intent.putExtra("wifi",false);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            } else {
                                                editor.putBoolean("wifi",true);
                                                editor.apply();
                                                Toast.makeText(getApplicationContext(), "Welcome 1" + username.getText().toString(), Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(MainActivity.this, P2photo.class);
                                                intent.putExtra("wifi",true);
                                                startActivity(intent);
                                                finish();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Huge Problem Occured", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                                    Gson gson = new Gson();
                                    Map<String, Object> map = new HashMap<>();
                                    map = (Map<String, Object>) gson.fromJson(errorResponse.toString(), map.getClass());
                                    Toast.makeText(getApplicationContext(), map.get("message").toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        Button signUpButton = findViewById(R.id.SignUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Sign_up.class);
                startActivity(intent);
            }
        });

    }
}
