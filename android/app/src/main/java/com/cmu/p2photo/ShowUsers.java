package com.cmu.p2photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.drive.UploadFileTask;
import com.cmu.p2photo.util.Config;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class ShowUsers extends AppCompatActivity {
    private static final String URL_FEED = "album/user/list";
    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_users);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.listView);


        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");




        /* get albums from user */
        try {
            JSONObject jsonParams = new JSONObject();
            SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
            String token = prefs.getString("token", null);
            if (token == null) {
                throw new RuntimeException("Session Token not found in Shared Preferences");
            }
            final String album = getIntent().getStringExtra("album");
            jsonParams.put("token", token);
            jsonParams.put("albumName", album);
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

                                List<String> users = (List<String>) map.get("users");
                                callback(users);
                                if (!(boolean) map.get("success")) {
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
        /* end get albuns from user */
    }

    void callback(List<String> users){

        // Initialize a new ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                users
        );


        // Assign adapter to ListView
        listView.setAdapter(adapter);
    }
}
