package com.cmu.p2photo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.cmu.p2photo.cloud.util.Config;
import com.cmu.p2photo.cloud.util.ImageAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ViewPhotos extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photos);
        final String sp = Config.getConfigValue(this, "shared_preferences");
        final String album = getIntent().getStringExtra("album");
        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        final boolean isWifi = prefs.getBoolean("wifi", false);
        final String username = prefs.getString("username", null);
        final GridView gridView = findViewById(R.id.gridview);
        String photoPathNotFinal;
        Log.d("P2PHOTO", getApplicationContext().getFilesDir().getPath());
        if (isWifi) {
            photoPathNotFinal = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/" + album + "/";
        } else {
            photoPathNotFinal = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
        }
        final String photoPath = photoPathNotFinal;


        ImageAdapter gridAdapter = (new ImageAdapter(this, photoPath));
        gridView.setAdapter(gridAdapter);
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            Intent intent = new Intent(ViewPhotos.this, ViewPhoto.class);
                            File folder = new File(photoPath);
                            File[] listOfFiles = folder.listFiles();
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].isFile()) {
                                    if (listOfFiles[i].getName().contains(".jpg")) {
                                        list.add(listOfFiles[i].getName());
                                    }
                                } else if (listOfFiles[i].isDirectory()) {
                                    //ignore
                                }
                            }

                            intent.putExtra("path", photoPath + list.get(position));
                            startActivity(intent);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }).execute();
        //writePhotos();
    }
}
