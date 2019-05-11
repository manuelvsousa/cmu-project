package com.cmu.p2photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmu.p2photo.drive.CreateFileTask;
import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.drive.UploadFileTask;
import com.cmu.p2photo.util.Config;
import com.cmu.p2photo.util.ImageAdapter;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class ViewPhotos extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photos);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        final String album = getIntent().getStringExtra("album");

        Log.d("FODASSE",getApplicationContext().getFilesDir().getPath());
        final GridView gridView  =  findViewById(R.id.gridview);
        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
        ImageAdapter gridAdapter =(new ImageAdapter(this, photoPath));
        gridView.setAdapter(gridAdapter);
        (new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                            Toast.makeText(ViewPhotos.this, "" + position, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ViewPhotos.this, ViewPhoto.class);
                            File folder = new File(photoPath);
                            File[] listOfFiles = folder.listFiles();
                            List<String> list = new ArrayList<>();
                            for (int i = 0; i < listOfFiles.length; i++) {
                                if (listOfFiles[i].isFile()) {
                                    if(listOfFiles[i].getName().contains(".jpg")){
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

                } catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
        }).execute();
        //writePhotos();
    }
}
