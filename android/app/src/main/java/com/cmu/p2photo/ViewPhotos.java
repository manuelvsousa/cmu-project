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
        final String photosS = getIntent().getStringExtra("photos");
        final String album = getIntent().getStringExtra("album");

        final List<String> photos = new ArrayList<String>(Arrays.asList(photosS.split(",")));


        Log.d("FODASSE",getApplicationContext().getFilesDir().getPath());

        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";


        File dir = new File(photoPath);
        dir.delete();

        final GridView gridView  =  findViewById(R.id.gridview);
        ImageAdapter gridAdapter =(new ImageAdapter(this, photoPath));
        gridView.setAdapter(gridAdapter);


        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES
//        String[]entries = dir.list();
//        for(String s: entries){
//            File currentFile = new File(dir.getPath(),s);
//            currentFile.delete();
//        }
//        dir.delete();
        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES
        //TESTING PURPOSES


        if(!dir.exists()) {
            dir.mkdir();
            File file = new File(photoPath + "images.json");
            try{
                file.delete();
                if (file.createNewFile()) {
                    //good, created
                }
                List<String> foo = new ArrayList<String>();
                String json = new Gson().toJson(foo);
                saveToJsonCatalog(json);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        Log.d("FODASSE","ELLELELLE");
        final CountDownLatch latch = new CountDownLatch(photos.size());
        for(int i =0; i < photos.size() ; i++){
            String parsedPhotoName = photos.get(i).split("/")[photos.get(i).split("/").length -1];
            Log.d("FODASSE",parsedPhotoName);
            Log.d("FODASSE",parsedPhotoName.split("\\.").toString());
            String photoName = parsedPhotoName.split("\\.")[0];
            Log.d("FODASSE",photoName);
            if(savePhoto(photoName)){
                (new AsyncTask<String,Void,Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                    try{
                        URL url = new URL(params[0]);
                        InputStream in = new BufferedInputStream(url.openStream());
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        byte[] buf = new byte[1024];
                        int n = 0;
                        while (-1!=(n=in.read(buf)))
                        {
                            out.write(buf, 0, n);
                        }
                        out.close();
                        in.close();
                        byte[] response = out.toByteArray();
                        savePhotoToDisk(response,params[1]);
                        latch.countDown();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                        return null;

                    }
                }).execute(photos.get(i),photoName);
            } else {
                latch.countDown();
            }
        }
        (new AsyncTask<Void,Void,Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try{
                    latch.await();
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
    }

    private void savePhotoToDisk(byte[] photoBytes, String photoName){
        final String album = getIntent().getStringExtra("album");
        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
        File photo=new File(photoPath + photoName + ".jpg");
        try {
            if (!photo.exists()) {
                photo.createNewFile();
            }
            FileOutputStream fos=new FileOutputStream(photo.getPath());

            fos.write(photoBytes);
            fos.close();
            Log.d("FODASSE",photoName + " was written to folder");
        }
        catch (java.io.IOException e) {
            Log.e("P2PHOTO", "Exception in photoCallback", e);
        }
    }

    private boolean savePhoto(String fileName){
        List<String> savedPhotos = null;
        try {
            final String album = getIntent().getStringExtra("album");
            final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
            FileInputStream fis = new FileInputStream(new File(photoPath + "images.json"));
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }
            Gson gson = new Gson(); // Or use new GsonBuilder().create();
            Log.d("FODASSE",sb.toString());
            if(sb.toString().equals("")){
                savedPhotos = new ArrayList<String>();
            } else {
                savedPhotos = gson.fromJson(sb.toString(), List.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(!savedPhotos.contains(fileName)){
            savedPhotos.add(fileName);
            Log.d("FODASSE",new Gson().toJson(savedPhotos));
            saveToJsonCatalog(new Gson().toJson(savedPhotos));
            return true;
        } else {
            Log.d("FODASSE","skipped");
            return false;
        }
    }

    private void saveToJsonCatalog(String json){
        try {
            final String album = getIntent().getStringExtra("album");
            final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
            FileOutputStream fos = new FileOutputStream(new File(photoPath + "images.json"));
            if (json != null) {
                fos.write(json.getBytes());
            }
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
