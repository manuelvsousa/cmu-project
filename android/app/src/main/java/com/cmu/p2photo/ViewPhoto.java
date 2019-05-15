package com.cmu.p2photo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.cmu.p2photo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;


public class ViewPhoto extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);
        final String photoPath = getIntent().getStringExtra("path");
        ImageView imageView = findViewById(R.id.imageView);
        try {
            InputStream ims = new FileInputStream(new File(photoPath));
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap torto = BitmapFactory.decodeStream(ims);
            Bitmap bitmap = Bitmap.createBitmap(torto, 0, 0, torto.getWidth(), torto.getHeight(), matrix, true);
            imageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
