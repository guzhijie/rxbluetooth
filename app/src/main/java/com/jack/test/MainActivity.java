package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.Toast;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

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
                    .map(bluetoothHolder -> (SensorBluetoothHolder<? extends SensorData, ?>) bluetoothHolder)
                    .concatMap(SensorBluetoothHolder::deviceInfo)
                    .subscribe(s -> Toast.makeText(this, s, Toast.LENGTH_LONG).show());
//                    .concatMap(sensorBluetoothHolder -> {
//                        Type type = sensorBluetoothHolder.getType();
//                        if (type instanceof JS100SensorData) {
//                            JS100BluetoothHolder js100BluetoothHolder = (JS100BluetoothHolder) sensorBluetoothHolder;
//                            return js100BluetoothHolder.sensorObservable(new JS100Param()
//                                    .setType(JS100SampleType.Temperature_Vibrate)
//                                    .setFrequency(JS100SampleFrequency.Freq_1kHz)
//                                    .setFactor(100)
//                                    .setPoint(JS100SamplePoint.Point_512));
//                        } else {
//                            ZC1000BluetoothHolder zc1000BluetoothHolder = (ZC1000BluetoothHolder) sensorBluetoothHolder;
//                            return zc1000BluetoothHolder.sensorObservable(null);
//                        }
//                    })
//                    .subscribe(sensorData -> {
//                        if (sensorData instanceof JS100SensorData) {
//                            JS100SensorData js100SensorData = (JS100SensorData) sensorData;
//                            Float vibrate = js100SensorData.getVibrate();
//                            Float speed = js100SensorData.getSpeed();
//                            Float temperature = js100SensorData.getTemperature();
//                        } else if (sensorData instanceof ZC1000SensorData) {
//                            ZC1000SensorData zc1000SensorData = (ZC1000SensorData) sensorData;
//                            String temperature = zc1000SensorData.getTemperature();
//                            String vibrate = zc1000SensorData.getVibrate();
//                            String rfid = zc1000SensorData.getRFID();
//                        }
//                    }, Throwable::printStackTrace);
        });
        bleList.setLayoutManager(new LinearLayoutManager(this));
        bleList.setAdapter(adapter);
        searchBle.setOnClickListener(v -> new RxPermissions(this).request(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .subscribe(aBoolean -> {
                            if (aBoolean) {
                                Set<String> searchResultSet = new HashSet<>();
                                RxBluetooth.getInstance()
                                        .search(new SearchRequest.Builder()
                                                .searchBluetoothLeDevice(3000, 3)
                                                .searchBluetoothClassicDevice(5000)
                                                .searchBluetoothLeDevice(2000)
                                                .build(), 10, TimeUnit.SECONDS)
                                        .subscribe(searchResult -> {
                                            if (!searchResultSet.contains(searchResult.getAddress())) {
                                                searchResultSet.add(searchResult.getAddress());
                                                adapter.addItem(adapter.getItemCount(), searchResult);
                                                adapter.notifyItemRangeInserted(adapter.getItemCount(), 1);
                                            }
                                        });
                            }
                        }
                ));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_unbinder.unbind();
    }

}
