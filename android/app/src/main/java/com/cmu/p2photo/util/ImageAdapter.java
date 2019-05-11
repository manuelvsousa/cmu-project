package com.cmu.p2photo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.cmu.p2photo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private List<String> list = new ArrayList<>();
    private String photoPath;
    public ImageAdapter(Context c, String path) {
        context = c;
        try {

            File folder = new File(path);
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if(listOfFiles[i].getName().contains(".jpg")){
                        list.add(listOfFiles[i].getName());
                    }
                } else if (listOfFiles[i].isDirectory()) {
                    //ignore
                }
            }
            this.photoPath = path;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getCount() {
        return list.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView img;

        if (convertView == null) {

            img = new ImageView(context);
            img.setLayoutParams(new GridView.LayoutParams(350, 350));
            img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {

            img = (ImageView) convertView;
        }
        try {

            InputStream ims = new FileInputStream(new File(photoPath + list.get(position)));
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap torto =  BitmapFactory.decodeStream(ims);
            Bitmap bitmap = Bitmap.createBitmap(torto, 0, 0, torto.getWidth(), torto.getHeight(), matrix, true);
            img.setImageBitmap(bitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }}