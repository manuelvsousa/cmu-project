package com.cmu.p2photo.wifi;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.Messenger;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.cmu.p2photo.R;
import com.cmu.p2photo.cloud.util.Config;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.inesc.termite.wifidirect.SimWifiP2pBroadcast;
import pt.inesc.termite.wifidirect.SimWifiP2pDevice;
import pt.inesc.termite.wifidirect.SimWifiP2pDeviceList;
import pt.inesc.termite.wifidirect.SimWifiP2pInfo;
import pt.inesc.termite.wifidirect.SimWifiP2pManager;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.Channel;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.GroupInfoListener;
import pt.inesc.termite.wifidirect.SimWifiP2pManager.PeerListListener;
import pt.inesc.termite.wifidirect.service.SimWifiP2pService;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocket;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketManager;
import pt.inesc.termite.wifidirect.sockets.SimWifiP2pSocketServer;

public class MsgSenderActivity extends Service implements
        PeerListListener, GroupInfoListener {

    public static final String TAG = "msgsender";
    String fim = "";
    private SimWifiP2pManager mManager = null;
    private Channel mChannel = null;
    private Messenger mService = null;
    private boolean mBound = false;
    private SimWifiP2pSocketServer mSrvSocket = null;
    private SimWifiP2pSocket mCliSocket;
    private SimWifiP2pBroadcastReceiver mReceiver;
    private ServiceConnection mConnection = new ServiceConnection() {
        // callbacks for service binding, passed to bindService()

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mManager = new SimWifiP2pManager(mService);
            mChannel = mManager.initialize(getApplication(), getMainLooper(), null);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
            mManager = null;
            mChannel = null;
            mBound = false;
        }
    };

    @Override
    public void onCreate() {
        final String sp = Config.getConfigValue(getApplicationContext(), "shared_preferences");
        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
        String username = prefs.getString("username", null);
        final String catalogPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/catalog";
        File file = new File(catalogPath);
        Log.d("MYAPP", catalogPath + " was created");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                Log.d("MYAPP", catalogPath + "catalog was created");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Log.d("MYAPP", catalogPath + " already exists");
        }


        Log.d("MYAPP", "ENTROUENTROU");
        // initialize the WDSim API
        SimWifiP2pSocketManager.Init(getApplicationContext());

        // register broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_NETWORK_MEMBERSHIP_CHANGED_ACTION);
        filter.addAction(SimWifiP2pBroadcast.WIFI_P2P_GROUP_OWNERSHIP_CHANGED_ACTION);
        mReceiver = new SimWifiP2pBroadcastReceiver(this);
        registerReceiver(mReceiver, filter);

        Intent intent = new Intent(getBaseContext(), SimWifiP2pService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        mBound = true;

        // spawn the chat server background task
        new IncommingCommTask().executeOnExecutor(
                AsyncTask.THREAD_POOL_EXECUTOR);

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(30000);
                        Log.d("MYAPP", "WIFI UPDATE THREAD JUST RAN!");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    PeerinRange();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
    }

    public void fazertrocas(SimWifiP2pDeviceList peers, SimWifiP2pInfo groupInfo) {
        Log.d("MYAPP", "NUMERO DE DISPOSITIVOS " + peers.getDeviceList().size());
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = peers.getByName(deviceName);
            Log.d("MYAPP", "Esta a ir para um Dispositivo");

            new OutgoingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    device.getVirtIp());
            Log.d("MYAPP", "SendCommTask");
            Log.d("MYAPP", device.getVirtIp().split(":")[0]);
            Log.d("MYAPP", device.getRealIp());
            Log.d("MYAPP", device.getVirtIp());
            Log.d("MYAPP", device.deviceName);


            new SendCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    device.getVirtIp());

            //DISCONECT AFTER SENDING
            /*if (mCliSocket != null) {
                try {
                    mCliSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }*/

            //mCliSocket = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }






    /*
     * Asynctasks implementing message exchange
     */

    private byte[] readFileBytes(String path) {
        byte[] getBytes = {};
        try {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getBytes;
    }

    public void PeerinRange() {
        if (mBound) {
            mManager.requestGroupInfo(mChannel, MsgSenderActivity.this);
        }
    }

    private String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }

    @Override
    public void onPeersAvailable(SimWifiP2pDeviceList peers) {
        // compile list of devices in range
        //fazertrocas(peers);
    }

    /*
     * Listeners associated to Termite
     */

    @Override
    public void onGroupInfoAvailable(SimWifiP2pDeviceList devices,
                                     SimWifiP2pInfo groupInfo) {
        // compile list of network members
        StringBuilder peersStr = new StringBuilder();
        for (String deviceName : groupInfo.getDevicesInNetwork()) {
            SimWifiP2pDevice device = devices.getByName(deviceName);
            Log.d("MYAPP", device.getVirtIp());
            new OutgoingCommTask().executeOnExecutor(
                    AsyncTask.THREAD_POOL_EXECUTOR,
                    device.getVirtIp());


            String devstr = "" + deviceName + " (" +
                    ((device == null) ? "??" : device.getVirtIp()) + ")\n";
            peersStr.append(devstr);
        }
    }

    public class IncommingCommTask extends AsyncTask<Void, String, Void> {


        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... params) {

            Log.d("MYAPP", "IncommingCommTask started (" + this.hashCode() + ").");

            try {
                mSrvSocket = new SimWifiP2pSocketServer(
                        Integer.parseInt(getString(R.string.port)));
            } catch (IOException e) {
                Log.d("MYAPP", "IncommingCommTask started (" + this.hashCode() + ").");
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SimWifiP2pSocket sock = mSrvSocket.accept();
                    try {
                        /*BufferedReader sockIn = new BufferedReader(
                                new InputStreamReader(sock.getInputStream()));
                        String st = sockIn.readLine();
                        publishProgress(st);
                        //simular enviar o seu username*/

                        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
                        ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
                        String receiveCatalog = (String) in.readObject();


                        Map<String, List<String>> catalogrec;
                        Log.d("MYAPP", "IN CATALOG FILE: " + receiveCatalog);
                        if (!receiveCatalog.equals("")) {
                            Log.d("MYAPP", "FILE NOT EMPY");
                            catalogrec = new Gson().fromJson(receiveCatalog, new TypeToken<Map<String, ArrayList<String>>>() {
                            }.getType());
                        } else {
                            Log.d("MYAPP", "FILE EMPTY");
                            catalogrec = new HashMap<>();
                        }
                        Log.d("MYAPP", "CATALOG RECEBIDO " + catalogrec.toString());
                        final String sp = Config.getConfigValue(getApplicationContext(), "shared_preferences");
                        SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                        String username = prefs.getString("username", null);
                        final String catalogPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/catalog";

                        String json = readFile(catalogPath);

                        Map<String, List<String>> catalogown;
                        Log.d("MYAPP", "IN CATALOG FILE1: " + json);
                        if (!json.equals("")) {
                            Log.d("MYAPP", "FILE NOT EMPY1");
                            catalogown = new Gson().fromJson(json, new TypeToken<Map<String, ArrayList<String>>>() {
                            }.getType());
                        } else {
                            Log.d("MYAPP", "FILE EMPTY1");
                            catalogown = new HashMap<>();
                        }

                        Log.d("MYAPP", "OWN RECEBIDO " + catalogown.toString());
                        Log.d("MYAPP", "ESTRANGEIRO RECEBIDO " + catalogrec.toString());

                        Map<String, List<String>> quero = new HashMap<>();
                        for (String album : catalogown.keySet()) {
                            if (catalogrec.containsKey(album)) {
                                for (String photo : catalogrec.get(album)) {
                                    if (!catalogown.get(album).contains(photo)) {
                                        if (quero.containsKey(album)) {
                                            quero.get(album).add(photo);
                                        } else {
                                            List<String> asd = new ArrayList<>();
                                            asd.add(photo);
                                            quero.put(album, asd);
                                        }
                                    }
                                }
                            }
                        }
                        Log.d("MYAPP", "QUERO FOTOS " + quero.toString());

                        out.writeObject(quero);
                        out.flush();


                        //TODO vai receber e guardar as fotos aqui

                        Map<String, List<Map<String, byte[]>>> fotosRecebidas = (HashMap<String, List<Map<String, byte[]>>>) in.readObject();
                        Log.d("MYAPP", "Fotos Recebidas: " + fotosRecebidas.toString());

                        for (String album : fotosRecebidas.keySet()) {
                            for (Map<String, byte[]> mapa : fotosRecebidas.get(album)) {
                                for (String fotoName : mapa.keySet()) {
                                    String fotoPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/" + album + "/" + fotoName;
                                    Log.d("MYAPP", "VAI ESCREVER FOTO!!! " + fotoPath);
                                    FileOutputStream fos = new FileOutputStream(fotoPath);
                                    fos.write(mapa.get(fotoName));
                                    fos.close();

                                    File file = new File(catalogPath);
                                    Log.d("MYAPP", catalogPath + " was created");
                                    if (!file.exists()) {
                                        try {
                                            file.createNewFile();
                                            String json2 = readFile(catalogPath);
                                            Map<String, List<String>> events;
                                            Log.d("MYAPP", "IN CATALOG FILE: " + json);
                                            if (!json.equals("")) {
                                                Log.d("MYAPP", "FILE NOT EMPY");
                                                events = new Gson().fromJson(json2, new TypeToken<Map<String, ArrayList<String>>>() {
                                                }.getType());
                                            } else {
                                                Log.d("MYAPP", "FILE EMPTY");
                                                events = new HashMap<>();
                                            }
                                            if (!events.containsKey(album)) {
                                                List<String> empty = new ArrayList<>();
                                                events.put(album, empty);
                                            }
                                            File fnew = new File(catalogPath);
                                            fnew.createNewFile();
                                            FileWriter fw = new FileWriter(catalogPath);
                                            Gson gson = new GsonBuilder().create();
                                            Log.d("MYAPP", gson.toJson(events));
                                            fw.write(gson.toJson(events));
                                            fw.close();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Log.d("MYAPP", fotoPath + "catalog already exists");
                                    }

                                    String jsonCatalog = readFile(catalogPath);

                                    Map<String, List<String>> events;
                                    Log.d("MYAPP", "IN CATALOG FILE: " + jsonCatalog);
                                    if (!jsonCatalog.equals("")) {
                                        Log.d("MYAPP", "FILE NOT EMPY");
                                        events = new Gson().fromJson(jsonCatalog, new TypeToken<Map<String, ArrayList<String>>>() {
                                        }.getType());
                                    } else {
                                        Log.d("MYAPP", "FILE EMPTY");
                                        events = new HashMap<>();
                                    }
                                    if (events.containsKey(album)) {
                                        events.get(album).add(fotoName);
                                    } else {
                                        events.put(album, Arrays.asList(fotoName));
                                    }
                                    File fnew = new File(catalogPath);
                                    fnew.createNewFile();
                                    FileWriter fw = new FileWriter(catalogPath);
                                    Gson gson = new GsonBuilder().create();
                                    Log.d("MYAPP", gson.toJson(events));
                                    fw.write(gson.toJson(events));
                                    fw.close();
                                }
                            }
                        }


                        out.writeObject("kk");
                        out.flush();
                        /*final String sp1 = Config.getConfigValue(getApplicationContext(), "shared_preferences");
                        SharedPreferences prefs1 = getSharedPreferences(sp1, MODE_PRIVATE);
                        String usernameMeu = prefs1.getString("username", null);
                        out.writeObject(usernameMeu);
                        Log.d("Testes",username);
                        out.flush();*/


                    } catch (IOException e) {
                        Log.d("MYAPP", "Error reading socket: " + e.getMessage());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } finally {
                        sock.close();
                    }
                } catch (IOException e) {
                    Log.d("MYAPP", "Error reading socket: " + e.getMessage());
                    break;
                    //e.printStackTrace();
                }
            }
            return null;
        }


    }

    public class OutgoingCommTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            Log.d("MYAPP", "PreOUTGOING");
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("MYAPP", "BACKGROUNDOUTGOING");
            try {
                Log.d("MYAPP", "FUNCIONOU McliSOCKET");
                mCliSocket = new SimWifiP2pSocket(params[0], Integer.parseInt(getString(R.string.port)));

                new SendCommTask().executeOnExecutor(
                        AsyncTask.THREAD_POOL_EXECUTOR,
                        params[0]);
                Log.d("MYAPP", "----------------------------------------------------------------------------------");
            } catch (UnknownHostException e) {
                return "Unknown Host:" + e.getMessage();
            } catch (IOException e) {
                return "IO error:" + e.getMessage();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

            } else {

            }
        }
    }

    public class SendCommTask extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... msg) {
            if (mCliSocket != null) {


                try {
                    Log.d("MYAPP", "1");
                    //mCliSocket.getOutputStream().write(("   iuj"+"\n").getBytes());
                    Log.d("MYAPP", "2");
                    ObjectOutputStream out = new ObjectOutputStream(mCliSocket.getOutputStream());
                    Log.d("MYAPP", "3");
                    ObjectInputStream in = new ObjectInputStream(mCliSocket.getInputStream());
                    //USERNAME
                    final String sp = Config.getConfigValue(getApplicationContext(), "shared_preferences");
                    SharedPreferences prefs = getSharedPreferences(sp, MODE_PRIVATE);
                    String username = prefs.getString("username", null);


                    //enviar catalogo

                    final String catalogPath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/catalog";

                    String json = readFile(catalogPath);

                    //enviar catalogo
                    out.writeObject(json);
                    out.flush();
                    //Buscar Map de fotos pretendidas
                    Map<String, List<String>> userquer = (HashMap<String, List<String>>) in.readObject();
                    Log.d("MYAPP", userquer.toString());


                    Map<String, List<Map<String, byte[]>>> mapdefotos = new HashMap<>();
                    for (String album : userquer.keySet()) {
                        String albumpath = getApplicationContext().getFilesDir().getPath() + "/wifi/" + username + "/" + album + "/";
                        Log.d("MYAPP", "albumpath " + albumpath);
                        for (String photoName : userquer.get(album)) {
                            String fotopath = albumpath + photoName;
                            File asd = new File(fotopath);
                            if (asd.exists()) {
                                Map<String, byte[]> itemToAdd = new HashMap<>();
                                itemToAdd.put(photoName, readFileBytes(fotopath));
                                if (mapdefotos.containsKey(album)) {
                                    try {
                                        mapdefotos.get(album).add(itemToAdd);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    List<Map<String, byte[]>> itemToAdd2 = new ArrayList<>();
                                    itemToAdd2.add(itemToAdd);
                                    mapdefotos.put(album, itemToAdd2);
                                }
                            }
                        }
                    }

                    Log.d("MYAPP", "FOTOS A ENVIAR " + mapdefotos.toString());


                    out.writeObject(mapdefotos);
                    out.flush();
                    in.readObject();
                    mCliSocket.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }


            }
            mCliSocket = null;
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }
    }


}
