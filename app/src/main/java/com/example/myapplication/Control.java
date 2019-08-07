package com.example.myapplication;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;




import static java.lang.Thread.sleep;


//import static com.example.myapplication.BluetoothLeService.TARGET_DEVICE_BATTERY;


public class Control extends Activity {
    private final static String TAG = Control.class.getSimpleName();
    private static final String DB = "debug";
    //	private final static String UUID_KEY_DATA = "0000F001-0000-1000-8000-00805F9B34FB";
//	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
    private String mDeviceName = null;
    private String mDeviceAddress = null;

    //	private String mDeviceUuid = "0000F001-0000-1000-8000-00805F9B34FB";
    private String mDeviceUuid = null;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private boolean mConnected = false;
    private String rssi_value;
    private String mDeviceBatt;
    private String mDeviceAck;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public BluetoothGatt gatt=null;
    private BluetoothLeService mBluetoothLeService = null;
    private BluetoothGattCharacteristic target_character;
    private BluetoothGattCharacteristic rx_character;

//mainactivity.java code #1
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final long SCAN_PERIOD = 5000;//5s
    private Dialog mDialog;
    public static List<BluetoothDevice> mDevices = new ArrayList<BluetoothDevice>();
    public static Control instance = null;

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();


//end code #1


    Button but_send1,but_connect,but_stop;
    TextView tv_deviceName,tv_deviceAddr,tv_connstatus,tv_currentRSSI,tv_targetUUID,tv_rx,tv_battery;
    EditText et_duration,et_white,et_yellow;
    ExpandableListView lv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_deviceName = (TextView) findViewById(R.id.tv_Name);
        tv_deviceAddr = (TextView) findViewById(R.id.tv_MAC);
        tv_connstatus = (TextView) findViewById(R.id.tv_con);
        tv_currentRSSI = (TextView) findViewById(R.id.tv_RSSI);
        tv_currentRSSI.setText("null");
        tv_targetUUID = (TextView) findViewById(R.id.tv_UUID);
        tv_targetUUID.setText("null");
        tv_rx = (TextView) findViewById(R.id.TV_RX);
        et_duration = (EditText) findViewById(R.id.ET_TX1);
        et_white = (EditText) findViewById(R.id.ET_TX3);
        et_yellow = (EditText) findViewById(R.id.ET_TX4);


/*        lv = (ExpandableListView) this.findViewById(R.id.ELV1);
        lv.setOnChildClickListener(servicesListClickListner);*/

        Intent intent = getIntent();
        Log.d(TAG, "Control onCreate");
        mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
        mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

        mDeviceUuid = intent.getStringExtra(String.valueOf(target_character));
        Log.d(TAG, "mDeviceAddress = " + mDeviceAddress);
        Log.d(TAG, "mDeviceName = " + mDeviceName);
        Log.d(TAG, "mDeviceUUID = " + mDeviceUuid);
//		Log.d(TAG, "mDeviceBatt = " + mDeviceBatt);
        tv_deviceName.setText(mDeviceName);
        tv_deviceAddr.setText(mDeviceAddress);
        tv_targetUUID.setText(mDeviceUuid);
        et_duration.setText("3");
        et_yellow.setText("150");
        et_white.setText("150");
        Log.d(TAG, "start BluetoothLE Service");

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);


        //mainactivity.java code #2


        if (!getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Ble not supported", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Button btn = (Button) findViewById(R.id.but_scan2);
        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                scanLeDevice();
                showRoundProcessDialog(Control.this, R.layout.loading_process_dialog_anim);
                Timer mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent deviceListIntent = new Intent(getApplicationContext(),
                                Device.class);
                        startActivity(deviceListIntent);
                        mDialog.dismiss();
                    }
                }, SCAN_PERIOD);
            }
        });
        instance = this;

        //end code #2



/*		BluetoothGattCharacteristic characteristic = getIntent().getExtras("uuid");
		target_character2 = characteristic;*/

/*		Log.w(TAG, "cmd onservicesdiscovered startingggg. target char: "+target_character);
		Log.w(TAG, "cmd onservicesdiscovered startingggg. target char: "+mBluetoothLeService.target_character);*/

        //battery indicator
        tv_battery = (TextView) this.findViewById(R.id.tv_BATTERY);
        //	this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));


        but_send1 = (Button) findViewById(R.id.but_send);
        but_send1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (target_character != null) {
                    Log.d(TAG, "cmd start");


                    String cmd_duration = et_duration.getText().toString();
                    String cmd_white = et_white.getText().toString();
                    String cmd_yellow = et_yellow.getText().toString();


                    if (!cmd_duration.isEmpty() && !cmd_white.isEmpty() && !cmd_yellow.isEmpty()) {

                        int int_Duration = Integer.parseInt(et_duration.getText().toString());
                        String hex_Duration = Integer.toHexString(int_Duration);
                        String hex_Duration2 = ("00" + hex_Duration).substring(hex_Duration.length());


                        Log.d(TAG, "before cmd: hex_duration" + hex_Duration + " hexDuration2: " + hex_Duration2);


                        int int_White = Integer.parseInt(et_white.getText().toString());
                        String hex_white = Integer.toHexString(int_White);
                        String hex_white2 = ("00" + hex_white).substring(hex_white.length());
                        Log.d(TAG, "before cmd: hex_white" + hex_white + " hexWhite2: " + hex_white2);

                        int int_Yellow = Integer.parseInt(et_yellow.getText().toString());
                        String hex_Yellow = Integer.toHexString(int_Yellow);
                        String hex_Yellow2 = ("00" + hex_Yellow).substring(hex_Yellow.length());
                        Log.d(TAG, "before cmd: hex_yellow" + hex_Yellow + " hexYellow2: " + hex_Yellow2);


                        String hexarray = "005903" + hex_Duration2 + "00" + hex_white2 + hex_Yellow2;
                        Log.d(TAG, "before cmd hexarray " + hexarray);

                        byte[] dataarray = hexStringToByteArray(hexarray);
                        Log.d(TAG, "after cmd data array " + Arrays.toString(dataarray));

                        target_character.setValue(dataarray);
                        mBluetoothLeService.writeCharacteristic(target_character);
                        Toast.makeText(Control.this, "S " + target_character, Toast.LENGTH_SHORT).show();
                        Log.d(DB, (cmd_duration));
                        Log.d(TAG, "sent cmd:" + "array sent data array " + Arrays.toString(dataarray));
                        //                 tv_rx.setText("Command Send: "+ Arrays.toString(dataarray));

                        long endTime = System.currentTimeMillis() + 1000;
                        while (System.currentTimeMillis() < endTime) {
                            mBluetoothLeService.readCharacteristic(rx_character);

                            Log.d(TAG, "cmd readcharacteristic triggered");
                        }


                    } else {
                        Toast.makeText(Control.this, "Please type your command. No empty fields allowed.", Toast.LENGTH_SHORT).show();
                        Log.d(DB, (cmd_duration));
                    }
                }

            }
        });

        but_stop = (Button) findViewById(R.id.but_stop);
        but_stop.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.disconnect();
            }
        });

        but_connect = (Button) findViewById(R.id.but_connect1);
        but_connect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothLeService.connect(mDeviceAddress);
            }
        });
    }

    //main activity.java code #3

        public void showRoundProcessDialog(Context mContext, int layout) {
            DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode,
                                     KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_HOME
                            || keyCode == KeyEvent.KEYCODE_SEARCH) {
                        return true;
                    }
                    return false;
                }
            };

            mDialog = new AlertDialog.Builder(mContext).create();
            mDialog.setOnKeyListener(keyListener);
            mDialog.show();
            mDialog.setContentView(layout);
        }

        private void scanLeDevice() {
           // Log.w(TAG, "scan scanledevice initiated");
            new Thread() {

                @Override
                public void run() {
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                    try {
                        Thread.sleep(SCAN_PERIOD);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }.start();
        }

        private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,
                                 byte[] scanRecord) {
              //  Log.w(TAG, "scan lescancallback initiated");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null) {
                            if (mDevices.indexOf(device) == -1)
                                mDevices.add(device);
                        }
                    }
                });
            }
        };

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            // User chose not to enable Bluetooth.
            if (requestCode == REQUEST_ENABLE_BT
                    && resultCode == Activity.RESULT_CANCELED) {

                return;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }

/*        @Override
        protected void onDestroy() {
            super.onDestroy();

        }*/


// end code #3



    public static byte[] hexStringToByteArray(String s) {

        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
            Log.d(TAG, "sent cmd:" + i + ": "  + (Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;

    }


    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            Log.d(TAG, "start service Connection");
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            Log.w(TAG, "cmd onservicesdiscovered startinggggonserviceconnected. target char: "+target_character);
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up
            // initialization.
            mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "mDeviceAddress = " + mDeviceAddress);
            boolean status = mBluetoothLeService.connect(mDeviceAddress);
            if(status == true){
                Log.d(TAG, "connection OK");


            }else{
                Log.d(TAG, "Connection failed");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(TAG, "end Service Connection");
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "enter BroadcastReceiver");
            final String action = intent.getAction();
            Log.d(TAG, "action = " + action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(mConnected);
                System.out.println("BroadcastReceiver :"+"device connected");


            } else if(BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(mConnected);

            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                Log.d(TAG, "services discovered!!!");
                //getGattService(mBluetoothLeService.getSupportedGattServices());
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                startReadRssi();
                startReadBat();
                //startReadInformation();

            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "receive data");
    //            byte[] value = intent.getByteArrayExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "BroadCast + BattLevel");
                mDeviceBatt = intent.getStringExtra(BluetoothLeService.TARGET_DEVICE_BATTERY);
                mDeviceBatt += "%";
                Log.d(TAG, "TARGET_DEVICE_BATTERY" + mDeviceBatt);
                updatebattery(mDeviceBatt);
                mDeviceAck = intent.getStringExtra(BluetoothLeService.EXTRA_ACK_DATA);
     //           mDeviceBatt += "%";
                Log.d(TAG, "acknowledgement" + mDeviceAck);
                updateACK(mDeviceAck);
                if(mDeviceAck != null){
                //    displayData(mDeviceAck);
                    Log.d(TAG, "rx value" + mDeviceAck);


                }else{
                    Log.d(TAG, "value = null");
                }
            } else if (BluetoothLeService.ACTION_GATT_RSSI.equals(action)) {
                Log.d(TAG, "BroadCast + RSSI");
                rssi_value = intent.getStringExtra(BluetoothLeService.EXTRA_RSSI);
                rssi_value += "dB";
                Log.d(TAG, "rssi_value = " + rssi_value);
                updateRSSI(rssi_value);
            }
   //         } else if (BluetoothLeService.TARGET_DEVICE_BATTERY.equals(action)) {


            //Battery Related infomation

//				String level1 = intent.getStringExtra(TARGET_DEVICE_BATTERY);
//				int level = intent.getIntExtra(BluetoothLeService.TARGET_DEVICE_BATTERY, 0);
//				battery_value = intent.getStringExtra(BluetoothLeService.TARGET_DEVICE_BATTERY);
        }

        private void updatebattery(String value) {
            // TODO Auto-generated method stub
            if(value != null){
                tv_battery.setText("Battery: "+value);
            }
        }
        private void updateRSSI(String value) {
            // TODO Auto-generated method stub
            if(value != null){
                tv_currentRSSI.setText(value);
            }
        }
        private void updateACK(String value) {
            // TODO Auto-generated method stub
            if(value != null){
                tv_rx.setText(value);
            }
        }
        private void updateConnectionState(boolean status) {
            // TODO Auto-generated method stub
            if(status){
                tv_connstatus.setText("connected");
            }else{
                tv_connstatus.setText("unconnected");
            }
        }
    };

    private void startReadRssi() {
        new Thread() {
            public void run() {

                while (true) {
                    try {
                        mBluetoothLeService.readRssi();
                        sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }

    private void startReadBat() {
        new Thread() {
            public void run() {

                while (true) {
                    try {
                        mBluetoothLeService.getbattery();


   /*                     Log.d(TAG, "sent cmd:" + "array sent data array "  + Arrays.toString());
                        tv_rx.setText("Command Send: "+ Arrays.toString(dataarray));*/
                        sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
        }.start();
    }
//not necessary
    /*
    private void displayData(byte[] data) {
        if (data != null) {
            String dataArray = new String(data);
            Log.d(TAG, "data = " + dataArray);
            tv_rx.setText(dataArray);
        }

    }
    */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }


    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothLeService.disconnect();
        mBluetoothLeService.close();

        System.exit(0);
    }



    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_RSSI);
        intentFilter.addAction(BluetoothLeService.TARGET_DEVICE_BATTERY);
        return intentFilter;
    }


    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {

        if (gattServices == null) return;
        String uuid = "unknown_UUID";
        String unknownServiceString = "unknown_service";
        String unknownCharaString = "unknown_characteristic";

        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();

        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            System.out.println("Service uuid:"+uuid);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();

            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();

            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);

                System.out.println("GattCharacteristic uuid:"+uuid);
                // System.out.println("--GattCharacteristic Properties:"+gattCharacteristic.getProperties());
                mBluetoothLeService.readCharacteristic(gattCharacteristic);
                // System.out.println("--GattCharacteristic value2:"+gattCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0));
                BluetoothGattDescriptor Descriptor=gattCharacteristic.getDescriptor(gattCharacteristic.getUuid());

                //System.out.println("--GattCharacteristic Descriptor:"+Descriptor.toString());

                List<BluetoothGattDescriptor> descriptors= gattCharacteristic.getDescriptors();
                for(BluetoothGattDescriptor descriptor:descriptors){
                    //System.out.println("---descriptor UUID:"+descriptor.getUuid());
                    mBluetoothLeService.getCharacteristicDescriptor(descriptor);
                }
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 },
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2 }
        );


/*        lv.setAdapter(gattServiceAdapter);*/



        Log.w(TAG, "cmd onservicesdiscovered startinggggafterservicesenum. target char: "+target_character);
//		 Log.w(TAG, "cmd onservicesdiscovered startinggggafterservicesenum2. target char: "+mBluetoothLeService.target_character3);
//		 Log.w(TAG, "cmd onservicesdiscovered startinggggafterservicesenum3. target char: "+BluetoothLeService.target_character2);
//		 final BluetoothGattCharacteristic characteristic = BluetoothLeService.target_character2;
        target_character = mBluetoothLeService.target_character;
        rx_character = mBluetoothLeService.rx_character;
        Log.w(TAG, "cmd onservicesdiscovered startinggggafterservicesenum4. target char: "+target_character);
        tv_targetUUID.setText(target_character.getUuid().toString());

    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.

    private final ExpandableListView.OnChildClickListener servicesListClickListner =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);

                        target_character = characteristic;

                        tv_targetUUID.setText(characteristic.getUuid().toString());

                        final int charaProp = characteristic.getProperties();
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) { mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                            mNotifyCharacteristic = characteristic;
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                        return true;
                    }
                    return false;
                }
            };
}
