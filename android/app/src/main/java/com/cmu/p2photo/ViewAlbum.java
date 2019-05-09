package com.cmu.p2photo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cmu.p2photo.drive.CreateFileTask;
import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.drive.UploadFileTask;
import com.cmu.p2photo.util.Config;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


public class ViewAlbum extends AppCompatActivity {
    private static final String URL_FEED = "album/create";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        final String apiUrl = Config.getConfigValue(this, "api_url");
        final String sp = Config.getConfigValue(this, "shared_preferences");

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
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),1);

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




        //checks if album exists or not. If not, then creates empty catalog file






        new CreateFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new CreateFileTask.Callback() {
            @Override
            public void onUploadComplete(String result) {

                EditText albumname = findViewById(R.id.userName);

                try {
                    JSONObject jsonParams = new JSONObject();
                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                    String token = prefs.getString("token", null);
                    if (token == null) {
                        throw new RuntimeException("Session Token not found in Shared Preferences");
                    }

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
                                            Toast.makeText(ViewAlbum.this, "Album Criado com Sucesso", Toast.LENGTH_SHORT)
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
        }).execute("/" + album);

        // TODO ver se album ja existe ou nao, se nao, criar catalog









    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1337) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("OLA", "heehehhehehhehhe");
            } else {
                // User refused to grant permission.
            }
        }
    }

    //                    ImageView asd = findViewById(R.id.imageView);
//                    asd.setImageURI(selectedImageUri);
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK) {
                if (requestCode == 1) {
                    Uri selectedImageUri = data.getData();
                    // Get the path from the Uri
                    final String path = getRealPathFromURI_API19(getApplicationContext(),selectedImageUri);
                    if (path != null) {
                        File f = new File(path);
                        Log.d("CARALHO", path);
                        Log.d("NAO PODE", path);
                        Log.d("FODASSE", f.getName());
                        Log.d("FODASSE", f.getAbsolutePath());
                        selectedImageUri = Uri.fromFile(f);
                    }


                    final String album = getIntent().getStringExtra("album");
                    // Set the image in ImageView
                    Log.d("FODASSE", selectedImageUri.toString());
                    Log.d("FODASSE", selectedImageUri.toString() + "caralho");
                    new UploadFileTask(getApplicationContext(), DropboxClientFactory.getClient(), new UploadFileTask.Callback() {
                        @Override
                        public void onUploadComplete(String result) {
                            Toast.makeText(getApplicationContext(), "Image uploaded with success", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("FODASSE", "Failed to upload file.", e);
                        }
                    }).execute(path,album,getApplicationContext().getFilesDir().getPath());

//
                }
            }
        } catch (Exception e) {
            Log.e("FileSelectorActivity", "File select error", e);
        }
    }

    private String getPathFromURI(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
        inputStream.close();
        return stringBuilder.toString();
    }

    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API19(Context context, Uri uri){



        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }


    @SuppressLint("NewApi")
    public static String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        String result = null;

        CursorLoader cursorLoader = new CursorLoader(
                context,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        if(cursor != null){
            int column_index =
                    cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        return result;
    }

    public static String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index
                = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}
