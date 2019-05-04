package com.cmu.p2photo.drive;


import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.WriteMode;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;


public class UploadFileTask extends AsyncTask<String, Void, String> {


    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;
    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";


    public interface Callback {
        void onUploadComplete(String result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    public static String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int)(Math.random()*ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    @Override
    protected String doInBackground(String... params) {
        String randomString = randomAlphaNumeric(32);
        String[]  originalSplitedFileName = params[0].split("/");
        String fileName =  "/" + params[1] + "/" + randomString + originalSplitedFileName[originalSplitedFileName.length - 1];
        try {
            try {
                File asd = new File(params[0]);

                Log.d("FODASSE",asd.toString());
                Log.d("FODASSE",fileName);
                InputStream inputStream = new FileInputStream(asd);
                mDbxClient.files().uploadBuilder(fileName)
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream);

            } catch (DbxException | IOException e) {
                mException = e;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        try{
            SharedLinkMetadata meta =  mDbxClient.sharing().createSharedLinkWithSettings(fileName);
            String url = meta.getUrl();
            url = url.split("\\?")[0];
            url = url + "\\?raw=1";
            Log.d("FODASSE",url);
            return url;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
