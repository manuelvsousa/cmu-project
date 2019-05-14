package com.cmu.p2photo.wifi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.cmu.p2photo.R;

import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;

public class WifiDirect extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_direct);
        SimWifiP2pSocketManager.Init(getApplicationContext());
    }
}