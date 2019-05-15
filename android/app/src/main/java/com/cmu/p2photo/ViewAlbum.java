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
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.cmu.p2photo.cloud.dropbox.CreateFileTask;
import com.cmu.p2photo.cloud.dropbox.DropboxClientFactory;
import com.cmu.p2photo.cloud.dropbox.UploadFileTask;
import com.cmu.p2photo.cloud.util.Config;
import com.cmu.p2photo.wifi.MsgSenderActivity;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class ViewAlbum extends AppCompatActivity {
    private static final String URL_FEED = "album/user/add/dropbox";
    private static final String URL_FEED2 = "/album/catalog/list";
    private String urls = new String();
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private Boolean isWifi = false;

    @SuppressLint("NewApi")
    public static String getRealPathFromURI(Context context, Uri uri) {


        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = {MediaStore.Images.Media.DATA};

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{id}, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        this.isWifi = prefs.getBoolean("wifi", false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1337);
        }

        final String album = getIntent().getStringExtra("album");
        TextView tv = findViewById(R.id.albumTextView);
        tv.setText("Album: " + album);


        Button addPhotoButton = findViewById(R.id.addPhoto);
        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);

            }
        });


        Button showUsers = findViewById(R.id.showUsers);
        showUsers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                Intent intent = new Intent(ViewAlbum.this, ShowUsers.class);
                intent.putExtra("album", album);
                startActivity(intent);

            }
        });

        Button addUser = findViewById(R.id.addUser);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//
                Intent intent = new Intent(ViewAlbum.this, AddUser.class);
                intent.putExtra("album", album);
                startActivity(intent);

            }
        });

           Button viewPhotos = findViewById(R.id.viewPhotos);
           viewPhotos.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   Intent intent = new Intent(ViewAlbum.this, ViewPhotos.class);
                   intent.putExtra("album", album);
                   startActivity(intent);
               }
           });



        if(this.isWifi){
            final String photoPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + album + "/";

            File dir = new File(photoPath);

            if (!dir.exists()) {
                dir.mkdir();
                Log.d("FODASSE", photoPath + " was created");
                File file = new File(photoPath + "catalog");
                if(!file.exists()){
                    try{
                        file.createNewFile();
                        Log.d("FODASSE", photoPath + "catalog was created");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    Log.d("FODASSE", photoPath + "catalog already exists");
                }
            } else {
                Log.d("FODASSE", photoPath + " already exists");
            }

        } else {
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

                        jsonParams.put("token", token);
                        jsonParams.put("albumName", album);
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
                }

                @Override
                public void onError(Exception e) {
                    Log.e(URL_FEED, "Failed to upload file.", e);
                }
            }).execute("/" + album);
            fetchCatalogs();
        }
    }


    public void writePhotos() {
        final String album = getIntent().getStringExtra("album");

        if (urls.length() > 0 && urls.charAt(0) == ',') {
            urls = urls.substring(1);
        }

        final List<String> photos = new ArrayList<String>(Arrays.asList(urls.split(",")));
        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";


        File dir = new File(photoPath);


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


        if (!dir.exists()) {
            dir.mkdir();
            File file = new File(photoPath + "images.json");
            try {
                file.delete();
                if (file.createNewFile()) {
                    //good, created
                }
                List<String> foo = new ArrayList<String>();
                String json = new Gson().toJson(foo);
                saveToJsonCatalog(json);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("FODASSE", "ELLELELLE");
        for (int i = 0; i < photos.size(); i++) {
            String parsedPhotoName = photos.get(i).split("/")[photos.get(i).split("/").length - 1];
            Log.d("FODASSE", parsedPhotoName);
            Log.d("FODASSE", parsedPhotoName.split("\\.").toString());
            String photoName = parsedPhotoName.split("\\.")[0];
            Log.d("FODASSE", photoName);
            if (savePhoto(photoName)) {
                (new AsyncTask<String, Void, Void>() {
                    @Override
                    protected Void doInBackground(String... params) {
                        try {
                            URL url = new URL(params[0]);
                            Log.d("FODASSE", params[0]);
                            InputStream in = new BufferedInputStream(url.openStream());
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buf = new byte[1024];
                            int n = 0;
                            while (-1 != (n = in.read(buf))) {
                                out.write(buf, 0, n);
                            }
                            out.close();
                            in.close();
                            byte[] response = out.toByteArray();
                            savePhotoToDisk(response, params[1]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;

                    }
                }).execute(photos.get(i), photoName);
            }
        }
    }

    private void saveToJsonCatalog(String json) {
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

    private void savePhotoToDisk(byte[] photoBytes, String photoName) {
        final String album = getIntent().getStringExtra("album");
        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/" + album + "/";
        File photo = new File(photoPath + photoName + ".jpg");
        try {
            if (!photo.exists()) {
                photo.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(photo.getPath());

            fos.write(photoBytes);
            fos.close();
            Log.d("FODASSE", photoName + " was written to folder");
        } catch (java.io.IOException e) {
            Log.e("P2PHOTO", "Exception in photoCallback", e);
        }
    }


    private boolean savePhoto(String fileName) {
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
            Log.d("FODASSE", sb.toString());
            if (sb.toString().equals("")) {
                savedPhotos = new ArrayList<String>();
            } else {
                savedPhotos = gson.fromJson(sb.toString(), List.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!savedPhotos.contains(fileName)) {
            savedPhotos.add(fileName);
            Log.d("FODASSE", new Gson().toJson(savedPhotos));
            saveToJsonCatalog(new Gson().toJson(savedPhotos));
            return true;
        } else {
            Log.d("FODASSE", "skipped");
            return false;
        }


    }


    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1337) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // accepted
            } else {
                // User refused to grant permission.
            }
        }
    }

    private void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        if (!dest.getParentFile().exists())
            dest.getParentFile().mkdirs();
        if (!dest.exists())
            dest.createNewFile();
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }



    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(this.isWifi){
            try {
                if (resultCode == RESULT_OK) {
                    if (requestCode == 1) {
                        Uri selectedImageUri = data.getData();
                        // Get the path from the Uri
                        final String path = getRealPathFromURI(getApplicationContext(), selectedImageUri);
                        if (path != null) {
                            File f = new File(path);
                            selectedImageUri = Uri.fromFile(f);
                        }

                        final String album = getIntent().getStringExtra("album");
                        final String photoPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + album + "/";
                        String[] splitedPhotoPath = path.split("/");
                        final String fileName = randomAlphaNumeric(32) + splitedPhotoPath[splitedPhotoPath.length - 1];
                        Log.d("FODASSE","wifi mode onActivityResult");
                        Log.d("FODASSE",path);
                        Log.d("FODASSE",photoPath + fileName);
                        copyFileUsingStream(new File(path),new File(photoPath + fileName));
                        Log.d("FODASSE","copyFileUsingStream done");


                        FileWriter fw = new FileWriter(photoPath + "catalog",true); //the true will append the new data
                        fw.write("," + fileName);//appends the string to the file
                        fw.close();

                        Log.d("FODASSE","photo file name written to catalog");


//                        File file = new File(photoPath + "catalog");
//
//                        StringBuilder text = new StringBuilder();
//
//                        try {
//                            BufferedReader br = new BufferedReader(new FileReader(file));
//                            String line;
//
//                            while ((line = br.readLine()) != null) {
//                                text.append(line);
//                                text.append('\n');
//                            }
//                            br.close();
//                        }
//                        catch (IOException e) {
//                            //You'll need to add proper error handling here
//                        }
//
//                        Log.d("FODASSE",text.toString());



                    }
                }
            } catch (Exception e) {
                Log.e("FileSelectorActivity", "File select error", e);
            }



        } else {
            try {
                if (resultCode == RESULT_OK) {
                    if (requestCode == 1) {
                        Uri selectedImageUri = data.getData();
                        // Get the path from the Uri
                        final String path = getRealPathFromURI(getApplicationContext(), selectedImageUri);
                        if (path != null) {
                            File f = new File(path);
                            selectedImageUri = Uri.fromFile(f);
                        }


                        final String album = getIntent().getStringExtra("album");
                        // Set the image in ImageView
                        new UploadFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                            @Override
                            public void onUploadComplete(String result) {
                                fetchCatalogs();
                                Toast.makeText(getApplicationContext(), "Image uploaded with success", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("P2PHOTO", "Failed to upload file.", e);
                            }
                        }).execute(path, album, getApplicationContext().getFilesDir().getPath());

//
                    }
                }
            } catch (Exception e) {
                Log.e("FileSelectorActivity", "File select error", e);
            }
        }





    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!this.isWifi){
            fetchCatalogs();
        }
    }

    private void fetchCatalogs() {
        final String album = getIntent().getStringExtra("album");
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");
        try {
            JSONObject jsonParams = new JSONObject();
            SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
            String token = prefs.getString("token", null);
            if (token == null) {
                throw new RuntimeException("Session Token not found in Shared Preferences");
            }

            jsonParams.put("token", token);
            jsonParams.put("albumName", album);
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

                                List<String> catalogs = (List<String>) map.get("catalogs");
                                Log.d("FODASSE", "YEEEEEEP 1");
                                catalogProcessor(catalogs);
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
    }

    private void catalogProcessor(final List<String> catalogs) {
        urls = "";
        final CountDownLatch latch = new CountDownLatch(catalogs.size());
        for (int i = 0; i < catalogs.size(); i++) {
            AsyncHttpClient client = new AsyncHttpClient();
            client.get(catalogs.get(i), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    urls += new String(response);
                    Log.d("FODASSE", "YEEEEEEP 2");
                    writePhotos();
                    latch.countDown();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                }
            });
        }
    }
}
