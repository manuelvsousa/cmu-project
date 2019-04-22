package com.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class P2photo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_p2photo);

        Button btnFind = findViewById(R.id.btnFindP2photo);
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, FindUser.class);
                startActivity(intent);
            }
        });

        Button btnViewAlbum = findViewById(R.id.btnViewP2photo);
        btnViewAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, ViewAlbum.class);
                startActivity(intent);
            }
        });

        Button btnLogout = findViewById(R.id.btnLogoutP2photo);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(P2photo.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}
