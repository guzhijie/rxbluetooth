package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jack.rx.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.SensorBluetoothHolderFactory;
import com.jack.test.SensorBluetoothHolder;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

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
                .map(SensorBluetoothHolder::sensorObservable)
                .subscribe(bluetoothHolder -> {

                });
    }
}
