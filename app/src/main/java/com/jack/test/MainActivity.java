package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jack.rx.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.js100.JS100BluetoothHolder;
import com.jack.test.js100.JS100Param;
import com.jack.test.js100.JS100SensorData;
import com.jack.test.zc1000.ZC1000BluetoothHolder;
import com.jack.test.zc1000.ZC1000SensorData;

import java.lang.reflect.Type;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();

    @SuppressWarnings("unchecked")
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String mac = "";
        RxBluetooth.getInstance()
                .connect(mac, new SensorBluetoothHolderFactory())
                .map(bluetoothHolder -> (SensorBluetoothHolder) bluetoothHolder)
                .concatMap(sensorBluetoothHolder -> {
                    Type type = sensorBluetoothHolder.getType();
                    if (type instanceof JS100SensorData) {
                        JS100BluetoothHolder js100BluetoothHolder = (JS100BluetoothHolder) sensorBluetoothHolder;
                        return js100BluetoothHolder.sensorObservable(new JS100Param());
                    } else {
                        ZC1000BluetoothHolder zc1000BluetoothHolder = (ZC1000BluetoothHolder) sensorBluetoothHolder;
                        return zc1000BluetoothHolder.sensorObservable(null);
                    }
                })
                .subscribe(sensorData -> {
                    if (sensorData instanceof JS100SensorData) {
                        JS100SensorData js100SensorData = (JS100SensorData) sensorData;
                        Float vibrate = js100SensorData.getVibrate();
                        Float speed = js100SensorData.getSpeed();
                        Float temperature = js100SensorData.getTemperature();
                    } else if (sensorData instanceof ZC1000SensorData) {
                        ZC1000SensorData zc1000SensorData = (ZC1000SensorData) sensorData;
                        String temperature = zc1000SensorData.getTemperature();
                        String vibrate = zc1000SensorData.getVibrate();
                        String rfid = zc1000SensorData.getRFID();
                    }
                });

    }
}
