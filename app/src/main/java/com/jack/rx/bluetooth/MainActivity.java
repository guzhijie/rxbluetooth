package com.jack.rx.bluetooth;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jack.test.SensorBluetoothHolderFactory;
import com.jack.test.SensorBluetoothHolder;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends AppCompatActivity {

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final String mac = "";
        RxBluetooth.getInstance()
                .connect(mac, new SensorBluetoothHolderFactory())
                .map(bluetoothHolder -> (SensorBluetoothHolder) bluetoothHolder)
                .subscribe(bluetoothHolder -> {
                    bluetoothHolder.sensorObservable()
                });
    }
}
