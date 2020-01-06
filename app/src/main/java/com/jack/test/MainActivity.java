package com.jack.test;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.jack.bluetooth.R;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.dbflow.User;
import com.jack.test.dbflow.User_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends RxAppCompatActivity {
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
        adapter.setOnItemClickListener((view, pos, o) -> connectBluetooth((SearchResult) o));
        bleList.setLayoutManager(new LinearLayoutManager(this));
        bleList.setAdapter(adapter);
        searchBle.setOnClickListener(v -> searchBluetooth(adapter));
        SQLite.select(User_Table.id, User_Table.name).from(User.class).query();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        m_unbinder.unbind();
    }

    @SuppressLint("CheckResult")
    private void connectBluetooth(SearchResult searchResult) {
        RxBluetooth.getInstance()
                .connect(searchResult.device.getAddress(), new SensorBluetoothHolderFactory())
                .map(bluetoothHolder -> (SensorBluetoothHolder<? extends SensorData, ?>) bluetoothHolder)
                .flatMap(SensorBluetoothHolder::deviceInfo)
                .subscribe(s -> Toast.makeText(this, s, Toast.LENGTH_LONG).show());
    }

    @SuppressLint("CheckResult")
    private void searchBluetooth(Adapter adapter) {
        final Set<String> searchResultSet = new HashSet<>();
        final SearchRequest searchRequest = new SearchRequest.Builder()
                .searchBluetoothLeDevice(3000, 3)
                .searchBluetoothClassicDevice(5000)
                .searchBluetoothLeDevice(2000)
                .build();
        new RxPermissions(this).request(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        RxBluetooth.getInstance().search(searchRequest, 10, TimeUnit.SECONDS)
                                .filter(searchResult -> !searchResultSet.contains(searchResult.getAddress()))
                                .subscribe(searchResult -> {
                                    searchResultSet.add(searchResult.getAddress());
                                    adapter.addItem(adapter.getItemCount(), searchResult);
                                    adapter.notifyItemRangeInserted(adapter.getItemCount(), 1);
                                });
                    }
                });
    }
}
