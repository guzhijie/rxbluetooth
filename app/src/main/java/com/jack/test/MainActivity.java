package com.jack.test;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.model.BleGattService;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.utils.StringUtils;
import com.jack.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.sensor.SensorBluetoothHolder;
import com.jack.test.sensor.SensorBluetoothHolderFactory;
import com.jack.test.sensor.SensorData;
import com.jack.test.sensor.zc1000.ZC1000BluetoothHolder;
import com.orhanobut.logger.Logger;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends RxAppCompatActivity {
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
        adapter.setOnItemClickListener((view, pos, o) -> connectBluetooth((SearchResult) o));
        bleList.setLayoutManager(new LinearLayoutManager(this));
        bleList.setAdapter(adapter);
        searchBle.setOnClickListener(v -> searchBluetooth(adapter));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_unbinder.unbind();
    }

    @SuppressLint("CheckResult")
    private void connectBluetooth(SearchResult searchResult) {
        String nameOrAddr = StringUtils.isBlank(searchResult.getName()) ? searchResult.getAddress() : searchResult.getName();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(this.getString(R.string.device_connecting, nameOrAddr))
                .content(R.string.waiting)
                .cancelable(false)
                .progress(true, 0)
                .show();
        RxBluetooth.getInstance()
                .connect(searchResult.device.getAddress(),
                        new BleConnectOptions.Builder()
                                .setConnectRetry(3)               // 连接如果失败重试3次
                                .setConnectTimeout(30000)         // 连接超时30s
                                .setServiceDiscoverRetry(3)       // 发现服务如果失败重试3次
                                .setServiceDiscoverTimeout(20000) // 发现服务超时20s
                                .build(),
                        new SensorBluetoothHolderFactory())
                .timeout(10, TimeUnit.SECONDS)
                .map(bluetoothHolder -> (SensorBluetoothHolder<? extends SensorData, ?>) bluetoothHolder)
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(dialog::dismiss)
                .subscribe(sensorBluetoothHolder -> {
                            List<BleGattService> services = sensorBluetoothHolder.getServices();
                            List<String> items = new ArrayList<>(services.size());
                            for (BleGattService service : services) {
                                StringBuilder sb = new StringBuilder("Service UUID:").append(service.getUUID().toString()).append("\n");
//                                for (BleGattCharacter character : service.getCharacters()) {
//                                    sb.append("\t").append("Character UUID:").append(character.getUuid().toString()).append("\n");
//                                }
                                items.add(sb.toString());
                            }
                            new MaterialDialog.Builder(this)
                                    .title(R.string.device_info)
                                    .content(R.string.waiting)
                                    .items(items)
                                    .dismissListener(dialogInterface -> disconnect(searchResult))
                                    .show();
                        },
                        throwable -> {
                            throwable.printStackTrace();
                            Toast.makeText(this, String.format("连接蓝牙设备:%s失败", nameOrAddr), Toast.LENGTH_LONG).show();
                        });
    }

    @SuppressLint("CheckResult")
    private void disconnect(SearchResult searchResult) {
        String nameOrAddr = StringUtils.isBlank(searchResult.getName()) ? searchResult.getAddress() : searchResult.getName();
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(this.getString(R.string.device_disconnecting, nameOrAddr))
                .content(R.string.waiting)
                .cancelable(false)
                .progress(true, 0)
                .show();
        RxBluetooth.getInstance()
                .disconnect(searchResult.getAddress())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(dialog::dismiss)
                .doOnError(throwable -> Logger.e("disconnect异常:%s", throwable.getMessage()))
                .subscribe(bluetoothStatus -> {
                            Logger.i("蓝牙已断开");
                            Toast.makeText(MainActivity.this, "蓝牙已断开", Toast.LENGTH_LONG).show();
                        },
                        throwable -> {
                            Logger.e("蓝牙已断开%s", throwable.getMessage());
                            Toast.makeText(MainActivity.this, "蓝牙断开失败", Toast.LENGTH_LONG).show();
                        });
    }

    @SuppressLint("CheckResult")
    private void searchBluetooth(Adapter adapter) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title(R.string.device_searching)
                .content(R.string.waiting)
                .cancelable(false)
                .progress(true, 0)
                .show();
        adapter.removeAllItem();
        final Set<String> searchResultSet = new HashSet<>();
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(1000, 3)
                .searchBluetoothClassicDevice(5000)
                .searchBluetoothLeDevice(2000)
                .build();
        new RxPermissions(this).request(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        RxBluetooth.getInstance().search(searchRequest, 5, TimeUnit.SECONDS)
                                .subscribeOn(Schedulers.newThread())
                                .filter(searchResult -> !searchResultSet.contains(searchResult.getAddress()) && searchResult.rssi != 0)
                                .observeOn(AndroidSchedulers.mainThread())
                                .doFinally(dialog::dismiss)
                                .subscribe(searchResult -> {
                                    searchResultSet.add(searchResult.getAddress());
                                    adapter.addItem(adapter.getItemCount(), searchResult);
                                    adapter.notifyDataSetChanged();
                                });
                    }
                });
    }
}
