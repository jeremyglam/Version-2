package com.example.myapplication;

import java.util.HashMap;
import java.util.UUID;

import android.bluetooth.BluetoothGattCharacteristic;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
	private static HashMap<String, String> attributes = new HashMap<String, String>();
	
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String UUID_BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String UUID_BATTERY_SERVICE = "0000180f-0000-1000-8000-00805f9b34fb";
    public static String HM_10_CONF = "0000F001-0000-1000-8000-00805F9B34FB";
	public static String BLE_RX = "0000f002-0000-1000-8000-00805f9b34fb";
	public static String BLE_TX = "0000f001-0000-1000-8000-00805f9b34fb";
	public static String BLE_SERVICE = "0000f000-0000-1000-8000-00805f9b34fb";
    static {
        // Sample Services.
  //  	attributes.put("0000fff0-0000-1000-8000-00805f9b34fb", "�����Զ���ͨ��Э�����������");
  //  	attributes.put("00001800-0000-1000-8000-00805f9b34fb", "Generic Access Profile Service");
   // 	attributes.put("00001801-0000-1000-8000-00805f9b34fb", "Generic Attribute Profile Service");
        attributes.put("0000f000-0000-1000-8000-00805f9b34fb", "This service");
 //       attributes.put("0000180d-0000-1000-8000-00805f9b34fb", "Heart Rate Service");
        attributes.put("0000180a-0000-1000-8000-00805f9b34fb", "Device Information Service");
        attributes.put("0000180f-0000-1000-8000-00805f9b34fb", "Battery Level Service");
        // Sample Characteristics.
        attributes.put("0000ffe1-0000-1000-8000-00805f9b34fb", "Heart Rate Measurement");
        attributes.put("00002a19-0000-1000-8000-00805f9b34fb", "Battery Level Characteristic");
        attributes.put("00002a29-0000-1000-8000-00805f9b34fb", "Manufacturer Name String");
        attributes.put("0000f001-0000-1000-8000-00805f9b34fb", "Write");
        attributes.put("0000f002-0000-1000-8000-00805f9b34fb", "Read");
 //       attributes.put("0000fff1-0000-1000-8000-00805f9b34fb", "��һ����ֵ");
 //       attributes.put("0000fff2-0000-1000-8000-00805f9b34fb", "�ڶ�����ֵ");
 //       attributes.put("0000fff3-0000-1000-8000-00805f9b34fb", "��������ֵ");
 //       attributes.put("0000fff4-0000-1000-8000-00805f9b34fb", "��������ֵ");
 //       attributes.put("0000fff5-0000-1000-8000-00805f9b34fb", "��������ֵ");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}