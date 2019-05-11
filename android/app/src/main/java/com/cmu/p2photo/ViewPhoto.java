package com.cmu.p2photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.cmu.p2photo.util.Config;
import com.cmu.p2photo.util.ImageAdapter;
import com.google.gson.Gson;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ViewPhoto extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        final String photoPath = getIntent().getStringExtra("path");
        ImageView imageView = findViewById(R.id.imageView);
        try{
            InputStream ims = new FileInputStream(new File(photoPath));
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap torto =  BitmapFactory.decodeStream(ims);
            Bitmap bitmap = Bitmap.createBitmap(torto, 0, 0, torto.getWidth(), torto.getHeight(), matrix, true);
            imageView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
