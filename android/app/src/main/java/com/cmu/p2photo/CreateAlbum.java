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

import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.drive.CreateFileTask;
import com.cmu.p2photo.util.Config;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_album);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        Button btnFind = findViewById(R.id.createAlbum);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText albumname = findViewById(R.id.albumName);

               new CreateFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new CreateFileTask.Callback() {
                    @Override
                    public void onUploadComplete(String result) {

                        EditText albumname = findViewById(R.id.albumName);

                        try {
                            JSONObject jsonParams = new JSONObject();
                            SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                            String token = prefs.getString("token", null);
                            if (token == null) {
                                throw new RuntimeException("Session Token not found in Shared Preferences");
                            }

                            Log.d("FODASSE",token.toString());
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
                                                    if(map.get("message").equals("album already exists")){
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
                Intent intent = new Intent(CreateAlbum.this, P2photo.class);
                startActivity(intent);

            }
        });




//        btnFind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final EditText username = findViewById(R.id.albumName);
//
//                /* GET USERS */
//                try {
//                    JSONObject jsonParams = new JSONObject();
//                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
//                    String token = prefs.getString("token", null);
//                    if (token == null) {
//                        throw new RuntimeException("Session Token not found in Shared Preferences");
//                    }
//                    jsonParams.put("token", token);
//                    StringEntity entity = new StringEntity(jsonParams.toString());
//                    new AsyncHttpClient().post(getApplicationContext(), apiUrl + URL_FEED, entity, "application/json",
//                            new JsonHttpResponseHandler() {
//                                ArrayList<String> a = null;
//                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                    Log.d(URL_FEED, "response raw: " + response.toString());
//                                    try {
//                                        Gson gson = new Gson();
//                                        Map<String, Object> map = new HashMap<>();
//                                        map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());
//                                        ArrayList<String> users = (ArrayList<String>) map.get("users");
//                                        if (!(boolean) map.get("success")) {
//                                            Toast.makeText(getApplicationContext(), "Failed to return users", Toast.LENGTH_SHORT).show();
//                                        }
//                                    } catch (Exception e) {
//                                        e.printStackTrace();
//                                    }
//                                }
//
//                                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
//                                    Gson gson = new Gson();
//                                    Map<String, Object> map = new HashMap<>();
//                                    map = (Map<String, Object>) gson.fromJson(errorResponse.toString(), map.getClass());
//                                    Toast.makeText(getApplicationContext(), map.get("message").toString(), Toast.LENGTH_SHORT).show();
//                                }
//
//                            });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//
//
//            }
//        });

    }

}
