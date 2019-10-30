package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.js100.JS100BluetoothHolder;
import com.jack.test.js100.JS100Param;
import com.jack.test.js100.JS100SampleFrequency;
import com.jack.test.js100.JS100SamplePoint;
import com.jack.test.js100.JS100SampleType;
import com.jack.test.js100.JS100SensorData;
import com.jack.test.zc1000.ZC1000BluetoothHolder;
import com.jack.test.zc1000.ZC1000SensorData;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();
    @BindView(R.id.ble_list)
    RecyclerView bleList;
    @BindView(R.id.search_ble)
    Button searchBle;
    private Unbinder m_unbinder;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_unbinder = ButterKnife.bind(this);
        Adapter adapter = new Adapter();
        adapter.setOnItemClickListener((view, pos, o) -> {
            SearchResult searchResult = (SearchResult) o;
            RxBluetooth.getInstance().connect(searchResult.device.getAddress(), new SensorBluetoothHolderFactory())
                    .map(bluetoothHolder -> (SensorBluetoothHolder) bluetoothHolder)
                    .concatMap(sensorBluetoothHolder -> {
                        Type type = sensorBluetoothHolder.getType();
                        if (type instanceof JS100SensorData) {
                            JS100BluetoothHolder js100BluetoothHolder = (JS100BluetoothHolder) sensorBluetoothHolder;
                            return js100BluetoothHolder.sensorObservable(new JS100Param()
                                    .setType(JS100SampleType.Temperature_Vibrate)
                                    .setFrequency(JS100SampleFrequency.Freq_1kHz)
                                    .setFactor(100)
                                    .setPoint(JS100SamplePoint.Point_512)                            );
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
        });
        bleList.setAdapter(adapter);
        searchBle.setOnClickListener(v -> RxBluetooth.getInstance()
                .search(new SearchRequest.Builder()
                        .searchBluetoothLeDevice(3000, 3)
                        .searchBluetoothClassicDevice(5000)
                        .searchBluetoothLeDevice(2000)
                        .build(), 10, TimeUnit.SECONDS)
                .subscribe(searchResult -> {
                    adapter.m_searchResults.add(searchResult);
                    adapter.notifyItemInserted(adapter.m_searchResults.size());
                }));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_unbinder.unbind();
    }

}
