package com.cmu.p2photo.cloud.dropbox;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


public class CreateFileTask extends AsyncTask<String, Void, String> {


    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public CreateFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
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

    @Override
    protected String doInBackground(String... params) {
        try {
            mDbxClient.files().createFolderV2(params[0]);
            String initialString = "";
            InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
            FileMetadata fr = mDbxClient.files().uploadBuilder(params[0] + "/catalog").uploadAndFinish(targetStream);
        } catch (CreateFolderErrorException err) {
            if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
                Log.d("P2PHOTO", "Something already exists at the path.");
            } else {
                Log.d("P2PHOTO", "Another error occured");
            }
        } catch (Exception err) {
            Log.d("P2PHOTO", "Very bad error occured");
        }
        try {
            SharedLinkMetadata meta = mDbxClient.sharing().createSharedLinkWithSettings(params[0] + "/catalog");
            String url = meta.getUrl();
            url = url.split("\\?")[0];
            url = url + "\\?raw=1";
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public interface Callback {
        void onUploadComplete(String result);

        void onError(Exception e);
    }
}
