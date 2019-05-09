package com.cmu.p2photo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.drive.PicassoClient;
import com.cmu.p2photo.util.Config;
import com.dropbox.core.android.Auth;
import com.dropbox.core.android.AuthActivity;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Dropbox extends AppCompatActivity {
    private static final String URL_FEED = "user/register/drive";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);
        final String dropbox_key = Config.getConfigValue(this, "dropbox_key");
        Button loginButton = findViewById(R.id.LogInButton);
        Log.d("PASSOU", "PASSOU AQUI");
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.startOAuth2Authentication(Dropbox.this, dropbox_key);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        String accessToken = Auth.getOAuth2Token();
        SharedPreferences.Editor editor = getSharedPreferences(sp, MODE_PRIVATE).edit();
        editor.putString("dropbox",accessToken);
        editor.apply();
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            JSONObject jsonParams = new JSONObject();
            String sessionToken = prefs.getString("token", null);
            if (sessionToken == null) {
                throw new RuntimeException("Session Token not found in Shared Preferences");
            }
            jsonParams.put("dropbox", accessToken);
            jsonParams.put("token", sessionToken);
            StringEntity entity = new StringEntity(jsonParams.toString());
            client.post(getApplicationContext(), apiUrl + URL_FEED, entity, "application/json",
                    new JsonHttpResponseHandler() {
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            Log.d(URL_FEED, "response raw: " + response.toString());
                            try {
                                Gson gson = new Gson();
                                Map<String, Object> map = new HashMap<>();
                                map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());
                                Log.d(URL_FEED, "Gson converted to map: " + map.toString());
                                if ((boolean) map.get("success")) {
                                    Intent intent = new Intent(Dropbox.this, P2photo.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Not Welcome", Toast.LENGTH_SHORT).show();
                                    Log.d(URL_FEED, "Could not save dropbox token in server" + map.get("token"));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Gson gson = new Gson();
                            Map<String, Object> map = new HashMap<>();
                            map = (Map<String, Object>) gson.fromJson(errorResponse.toString(), map.getClass());
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
