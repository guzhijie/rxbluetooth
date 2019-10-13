package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.jack.rx.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.SensorBluetoothHolderFactory;
import com.jack.test.SensorBluetoothHolder;
import com.jack.test.js100.JS100SensorData;
import com.jack.test.zc1000.ZC1000SensorData;

import java.lang.reflect.Type;
import java.util.logging.Logger;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

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
                .map(bluetoothHolder -> (SensorBluetoothHolder<SensorData>) bluetoothHolder)
                .concatMap(SensorBluetoothHolder::sensorObservable)
                .map(sensorData -> (SensorData<? extends SensorData<?>>) sensorData)
                .subscribe(sensorData -> {
                    String temperature = sensorData.getTemperature();
                    Log.e(TAG, temperature);
                    Type type = sensorData.getType();
                    if (type instanceof JS100SensorData) {
                    } else if (type instanceof ZC1000SensorData) {
                    }
                });

    }

    public static void main(String[] argv) {
        SensorData<JS100SensorData> sensorData = new JS100SensorData();
        System.out.println(sensorData.getType() instanceof Class);
    }
}
