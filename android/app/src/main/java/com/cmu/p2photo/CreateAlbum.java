package com.cmu.p2photo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cmu.p2photo.cloud.dropbox.CreateFileTask;
import com.cmu.p2photo.cloud.dropbox.DropboxClientFactory;
import com.cmu.p2photo.cloud.util.Config;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class CreateAlbum extends AppCompatActivity {
    private static final String URL_FEED = "album/create";
    private static final String URL_FEED2 = "album/check";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);
        setTitle("Create Album");
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        final Boolean isWifi = prefs.getBoolean("wifi", false);
        Button btnFind = findViewById(R.id.buttonAddAlbumname);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText albumname = findViewById(R.id.editText2AlbumName);


                /* check album status */
                try {
                    JSONObject jsonParams = new JSONObject();
                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                    String token = prefs.getString("token", null);
                    if (token == null) {
                        throw new RuntimeException("Session Token not found in Shared Preferences");
                    }

                    jsonParams.put("token", token);
                    jsonParams.put("albumName", albumname.getText().toString());
                    StringEntity entity = new StringEntity(jsonParams.toString());
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(getApplicationContext(), apiUrl + URL_FEED2, entity, "application/json",
                            new JsonHttpResponseHandler() {
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    Log.d(URL_FEED2, "response raw: " + response.toString());
                                    try {
                                        Gson gson = new Gson();
                                        Map<String, Object> map = new HashMap<>();
                                        map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());

                                        Log.d(URL_FEED2, "Gson converted to map: " + map.toString());
                                        if (!(boolean) map.get("success")) {
                                            Toast.makeText(getApplicationContext(), "Huge Problem Occured", Toast.LENGTH_SHORT).show();
                                        } else {
                                            EditText albumname = findViewById(R.id.editText2AlbumName);

                                            if (!isWifi) {
                                                new CreateFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new CreateFileTask.Callback() {
                                                    @Override
                                                    public void onUploadComplete(String result) {

                                                        try {
                                                            JSONObject jsonParams = new JSONObject();
                                                            SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                                                            String token = prefs.getString("token", null);
                                                            if (token == null) {
                                                                throw new RuntimeException("Session Token not found in Shared Preferences");
                                                            }
                                                            EditText albumname = findViewById(R.id.editText2AlbumName);
                                                            jsonParams.put("token", token);
                                                            jsonParams.put("albumName", albumname.getText().toString());
                                                            jsonParams.put("link", result);
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
                                                                                if ((boolean) map.get("success")) {
                                                                                    Toast.makeText(CreateAlbum.this, "Album Criado com Sucesso", Toast.LENGTH_SHORT)
                                                                                            .show();
                                                                                } else {
                                                                                    if (map.get("message").equals("album already exists")) {
                                                                                        //ignore
                                                                                    } else {
                                                                                        Toast.makeText(getApplicationContext(), "Huge Problem Occured", Toast.LENGTH_SHORT).show();
                                                                                    }
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

                                                    @Override
                                                    public void onError(Exception e) {
                                                        Log.e(URL_FEED, "Failed to upload file.", e);
                                                    }
                                                }).execute("/" + albumname.getText().toString());
                                            } else {
                                                try {
                                                    JSONObject jsonParams = new JSONObject();
                                                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                                                    String token = prefs.getString("token", null);
                                                    if (token == null) {
                                                        throw new RuntimeException("Session Token not found in Shared Preferences");
                                                    }
                                                    jsonParams.put("token", token);
                                                    jsonParams.put("albumName", albumname.getText().toString());
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
                                                                        if ((boolean) map.get("success")) {
                                                                            Toast.makeText(CreateAlbum.this, "Album Criado com Sucesso", Toast.LENGTH_SHORT)
                                                                                    .show();
                                                                        } else {
                                                                            if (map.get("message").equals("album already exists")) {
                                                                                //ignore
                                                                            } else {
                                                                                Toast.makeText(getApplicationContext(), "Huge Problem Occured", Toast.LENGTH_SHORT).show();
                                                                            }
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
                                            Intent intent = new Intent(CreateAlbum.this, P2photo.class);
                                            startActivity(intent);
                                            finish();
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
                                    Intent intent = new Intent(CreateAlbum.this, P2photo.class);
                                    startActivity(intent);
                                    return;
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /* end check album status */

            }
        });

    }

}
