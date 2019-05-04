package com.cmu.p2photo.drive;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.cmu.p2photo.drive.DropboxClientFactory;
import com.cmu.p2photo.util.Config;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.filerequests.FileRequest;
import com.dropbox.core.v2.files.CreateFolderErrorException;
import com.dropbox.core.v2.files.CreateFolderResult;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static android.content.Context.MODE_PRIVATE;

public class CreateFileTask extends AsyncTask<String, Void, String> {


    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(String result);
        void onError(Exception e);
    }

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
            CreateFolderResult folder = mDbxClient.files().createFolderV2(params[0]);
            String initialString = "";
            InputStream targetStream = new ByteArrayInputStream(initialString.getBytes());
            FileMetadata fr = mDbxClient.files().uploadBuilder(params[0] + "/catalog").uploadAndFinish(targetStream);
            SharedLinkMetadata meta =  mDbxClient.sharing().createSharedLinkWithSettings(params[0] + "/catalog");
            String url = meta.getUrl();
            url = url.split("\\?")[0];
            url = url + "\\?raw=1";
            Log.d("FODASSE",url);
            return url;
        } catch (CreateFolderErrorException err) {
            if (err.errorValue.isPath() && err.errorValue.getPathValue().isConflict()) {
                Log.d("CARALHO","Something already exists at the path.");
                return params[0];
            } else {
                Log.d("CARALHO","Another error occured");
            }
        } catch (Exception err) {
            Log.d("CARALHO","Very bad error occured");
        }
        return null;
    }
}
