package com.cmu.p2photo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cmu.p2photo.cloud.dropbox.DropboxClientFactory;
import com.cmu.p2photo.cloud.dropbox.PicassoClient;
import com.cmu.p2photo.cloud.util.Config;
import com.cmu.p2photo.wifi.MsgSenderActivity;
import com.dropbox.core.android.AuthActivity;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class P2photo extends AppCompatActivity {
    private static final String LOGOUT_URL_FEED = "user/logout";
    private static final String TAG = "CLOUD";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean wifiOn;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2photo);

        Button btnLogout = findViewById(R.id.LogInButton);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");

        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        wifiOn = prefs.getBoolean("wifi", false);
        if (!wifiOn) {
            String accessToken = prefs.getString("dropbox", null);
            if (accessToken == null) {
                throw new RuntimeException("Session Token not found in Shared Preferences");
            }

            DropboxClientFactory.init(accessToken);
            PicassoClient.init(getApplicationContext(), DropboxClientFactory.getClient());
        } else {
            Log.d("cenas", "cenas0");
            Intent serviceIntent = new Intent(this, MsgSenderActivity.class);
            this.startService(serviceIntent);
            Log.d("cenas2", "cenas2");

        }


        TextView tv = findViewById(R.id.textView8);
        if (wifiOn) {
            tv.setText("Wifi Version");
            setTitle("Wifi Version");
        } else {
            tv.setText("Cloud Version");
            setTitle("Cloud Version");
        }

        Button logButton = findViewById(R.id.logButton);
        logButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                Intent intent = new Intent(P2photo.this, ViewLogs.class);
                startActivity(intent);

            }
        });

        // Logout Listener
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject jsonParams = new JSONObject();
                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                    String token = prefs.getString("token", null);
                    if (token == null) {
                        throw new RuntimeException("Session Token not found in Shared Preferences");
                    }
                    jsonParams.put("token", token);
                    StringEntity entity = new StringEntity(jsonParams.toString());
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(getApplicationContext(), apiUrl + LOGOUT_URL_FEED, entity, "application/json",
                            new JsonHttpResponseHandler() {
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    Log.d(LOGOUT_URL_FEED, "response raw: " + response.toString());
                                    try {
                                        Gson gson = new Gson();
                                        Map<String, Object> map = new HashMap<>();
                                        map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());

                                        Log.d(LOGOUT_URL_FEED, "Gson converted to map: " + map.toString());
                                        if ((boolean) map.get("success")) {
                                            SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE); //clean all previous credentials
                                            prefs.edit().clear();
                                            prefs.edit().commit();
                                            AuthActivity.result = null;
                                            DropboxClientFactory.destroy();
                                            Toast.makeText(getApplicationContext(), "Logout Success", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(P2photo.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();
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


        Button createAlbum = findViewById(R.id.addUser);
        createAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, CreateAlbum.class);
                startActivity(intent);
            }
        });

        Button listAlbums = findViewById(R.id.viewAlbums);
        listAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, ListAlbums.class);
                startActivity(intent);
            }
        });

        Button showUsersAlbums = findViewById(R.id.showUsersAlbums);
        showUsersAlbums.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, ShowAllUsers.class);
                startActivity(intent);
            }
        });


    }
}
