package com.cmu.p2photo.cloud.dropbox;

import android.content.Context;

import com.dropbox.core.v2.DbxClientV2;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class PicassoClient {
    private static Picasso sPicasso;

    public static void init(Context context, DbxClientV2 dbxClient) {

        // Configure picasso to know about special thumbnail requests
        sPicasso = new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(context))
                .addRequestHandler(new FileThumbnailRequestHandler(dbxClient))
                .build();
    }


    public static Picasso getPicasso() {
        return sPicasso;
    }
}
