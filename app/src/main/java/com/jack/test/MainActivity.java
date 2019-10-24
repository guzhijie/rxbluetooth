package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.rx.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.js100.JS100BluetoothHolder;
import com.jack.test.js100.JS100Param;
import com.jack.test.js100.JS100SensorData;
import com.jack.test.zc1000.ZC1000BluetoothHolder;
import com.jack.test.zc1000.ZC1000SensorData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.Unbinder;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getName();
    @BindView(R.id.ble_list)
    RecyclerView bleList;
    @BindView(R.id.search_ble)
    Button searchBle;
    private Unbinder unbinder;
    private List<SearchResult> searchResults = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(this);
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


    @SuppressLint("CheckResult")
    @OnClick(value = R.id.search_ble)
    void searchBleOnClick() {
        RxBluetooth.getInstance()
                .search(new SearchRequest.Builder()
                        .searchBluetoothLeDevice(3000, 3)   // 先扫BLE设备3次，每次3s
                        .searchBluetoothClassicDevice(5000) // 再扫经典蓝牙5s
                        .searchBluetoothLeDevice(2000)      // 再扫BLE设备2s
                        .build(), 10, TimeUnit.SECONDS)
                .subscribe(searchResult -> {
                    this.searchResults.add(searchResult);
                    Objects.requireNonNull(this.bleList.getAdapter()).notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    class Adapter extends RecyclerView.Adapter {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ble_item_adapter_view, viewGroup, false);
            return new ViewHolder(itemView);
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
            TextView textView = (TextView) viewHolder.itemView;
            SearchResult searchResult = searchResults.get(i);
            String addr = searchResult.device.getAddress();
            String name = searchResult.device.getName();
            int rssi = searchResult.rssi;
            textView.setText(String.format("蓝牙设备: %s, 地址: %s, 信号强度:%d", addr, name, rssi));
        }

        @Override
        public int getItemCount() {
            return searchResults.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.item)
            TextView item;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            @OnClick(value = R.id.item)
            void itemViewOnClick() {
            }
        }
    }
}
