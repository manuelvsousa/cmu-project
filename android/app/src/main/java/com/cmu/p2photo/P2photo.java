package com.cmu.p2photo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class P2photo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2photo);

        Button btnFind = (Button) findViewById(R.id.btnFindP2photo);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, FindUser.class);
                startActivity(intent);
            }
        });

        Button btnViewAlbum = (Button) findViewById(R.id.btnViewP2photo);
        btnViewAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, ViewAlbum.class);
                startActivity(intent);
            }
        });

        Button btnLogout = (Button) findViewById(R.id.btnLogoutP2photo);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
