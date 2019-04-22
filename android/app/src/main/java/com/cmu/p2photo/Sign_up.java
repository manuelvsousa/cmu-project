package com.cmu.p2photo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Sign_up extends AppCompatActivity {
    private static final String URL_FEED = "user/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Button btnSignUp = findViewById(R.id.btnSignUp_Signup);
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText username = findViewById(R.id.UserSignup);
                EditText password = findViewById(R.id.PassSignup);
                try {
                    JSONObject jsonParams = new JSONObject();
                    jsonParams.put("username", username.getText().toString());
                    jsonParams.put("password", password.getText().toString());
                    jsonParams.put("passwordConfirmation", password.getText().toString());
                    StringEntity entity = new StringEntity(jsonParams.toString());
                    AsyncHttpClient client = new AsyncHttpClient();
                    client.post(getApplicationContext(), "http://192.168.1.72:5000/user/register", entity, "application/json",
                            new JsonHttpResponseHandler() {
                                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                    Log.d(URL_FEED, "response raw: " + response.toString());
                                    try {
                                        Gson gson = new Gson();
                                        Map<String, Object> map = new HashMap<>();
                                        map = (Map<String, Object>) gson.fromJson(response.toString(), map.getClass());

                                        Log.d(URL_FEED, "Gson converted to map: " + map.toString());
                                        if ((boolean) map.get("success")) {
                                            Toast.makeText(getApplicationContext(), "Registered with Success", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(Sign_up.this, Login.class);
                                            startActivity(intent);
                                        } else {
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
        });

    }

    public void registerUser(View v) {
        Toast.makeText(this, "Clicked on Button", Toast.LENGTH_LONG).show();
    }
}
