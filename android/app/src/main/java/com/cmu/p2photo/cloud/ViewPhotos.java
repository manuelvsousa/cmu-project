package com.cmu.p2photo.cloud;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.cmu.p2photo.R;
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
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        final String album = getIntent().getStringExtra("album");

        Log.d("FODASSE", getApplicationContext().getFilesDir().getPath());
        final GridView gridView = findViewById(R.id.gridview);
        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
        ImageAdapter gridAdapter = (new ImageAdapter(this, photoPath));
        gridView.setAdapter(gridAdapter);
        (new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            Toast.makeText(ViewPhotos.this, "" + position, Toast.LENGTH_SHORT).show();
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