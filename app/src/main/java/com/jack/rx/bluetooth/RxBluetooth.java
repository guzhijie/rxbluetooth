package com.jack.rx.bluetooth;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Pair;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/7/28
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class RxBluetooth extends BaseRxBluetooth {
    private static RxBluetooth m_rxBluetooth;
    private final PublishSubject<Pair<String, BluetoothStatus>> m_bluetoothStatusBus = PublishSubject.create();
    private final PublishSubject<String> m_stopReconnect = PublishSubject.create();
    private final Map<String, BluetoothHolder> m_bluetoothMap = new ConcurrentHashMap<>(8);
    private final BleConnectStatusListener m_connectStatusListener = new BleConnectStatusListener() {
        @SuppressLint("CheckResult")
        @Override
        public void onConnectStatusChanged(final String mac, final int status) {
            final BluetoothStatus bluetoothStatus = BluetoothStatus.valueOf(status);
            if (BluetoothStatus.CONNECTED != bluetoothStatus) {
                BluetoothHolder holder = m_bluetoothMap.get(mac);
                if (null != holder) {
                    //noinspection SingleStatementInBlock
                    connectByMac(mac, (mac1, bleGattProfile) -> Observable.just(holder))
                            .takeUntil(m_stopReconnect.filter(s -> s.equals(mac)))
                            .doOnSubscribe(disposable -> m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.CONNECTING)))
                            .retry(3)
                            .doOnSubscribe(disposable -> m_client.unregisterConnectStatusListener(mac, m_connectStatusListener))
                            .subscribe(bluetoothHolder -> {
                            }, throwable -> {
                                m_bluetoothMap.remove(mac);
                                m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.DISCONNECTED));
                            });
                }
            }
            m_bluetoothStatusBus.onNext(Pair.create(mac, bluetoothStatus));
        }
    };

    private RxBluetooth(final Context client) {
        super(client);
    }

    public static void init(Application application) {
        m_rxBluetooth = new RxBluetooth(application);
    }

    public static RxBluetooth getInstance() {
        if (null == m_rxBluetooth) {
            throw new NullPointerException("init(Application application) is not called.");
        }
        return m_rxBluetooth;
    }

    public Collection<BluetoothHolder> getConnectedBluetoothStatus() {
        return m_bluetoothMap.values();
    }

    public Observable<BluetoothInfo> bluetoothStatusObservable() {
        return Observable.fromIterable(getConnectedBluetoothStatus())
                .concatMap(bluetoothHolder -> m_bluetoothStatusBus.filter(pair -> pair.first.equals(bluetoothHolder.mac))
                        .map(statusPair -> statusPair.second)
                        .startWith(getConnectStatus(bluetoothHolder.mac))
                        .join(bluetoothHolder.readPower(),
                                bluetoothStatus -> Observable.empty(),
                                power -> Observable.empty(),
                                (bluetoothStatus, power) -> new BluetoothInfo.Builder()
                                        .setBluetoothStatus(bluetoothStatus)
                                        .setPower(power))
                        .join(readRssi(bluetoothHolder.mac),
                                builder -> Observable.empty(),
                                rssi -> Observable.empty(),
                                (builder, rssi) -> builder.setRssi(rssi).build()));
    }

    public Observable<BluetoothHolder> connect(String mac, BluetoothHolderFactory factory) {
        if (m_bluetoothMap.containsKey(mac)) {
            return bluetoothStatusObservable(mac)
                    .takeLast(1)
                    .map(bluetoothStatus -> {
                        if (bluetoothStatus == BluetoothStatus.CONNECTED) {
                            return m_bluetoothMap.get(mac);
                        } else {
                            throw new BluetoothException(String.format("connect %s failed", mac));
                        }
                    });
        } else {
            return connectByMac(mac, factory);
        }
    }

    public Observable<BluetoothStatus> disconnect(String mac) {
        if (m_bluetoothMap.containsKey(mac)) {
            return bluetoothStatusObservable(mac)
                    .doOnSubscribe(disposable -> m_stopReconnect.onNext(mac))
                    .takeLast(1)
                    .concatMap(bluetoothStatus -> {
                        if (bluetoothStatus == BluetoothStatus.DISCONNECTED) {
                            return Observable.just(BluetoothStatus.DISCONNECTED);
                        } else {
                            return disconnect0(mac).doFinally(() -> m_bluetoothMap.remove(mac));
                        }
                    });

        } else {
            return Observable.just(BluetoothStatus.DISCONNECTED);
        }
    }

    private Observable<BluetoothStatus> bluetoothStatusObservable(String mac) {
        return m_bluetoothStatusBus.filter(pair -> pair.first.equals(mac))
                .map(stringBluetoothStatusPair -> stringBluetoothStatusPair.second)
                .timeout(12, TimeUnit.SECONDS)
                .onErrorReturn(throwable -> BluetoothStatus.DISCONNECTED)
                .takeUntil(m_bluetoothStatusBus.filter(pair -> pair.first.equals(mac) && pair.second == BluetoothStatus.CONNECTED))
                .mergeWith(Observable.timer(6, TimeUnit.SECONDS)
                        .map(aLong -> getConnectStatus(mac))
                        .takeUntil(m_bluetoothStatusBus.filter(pair -> pair.first.equals(mac))));

    }

    private Observable<BluetoothHolder> connectByMac(String mac, BluetoothHolderFactory factory) {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)   // 连接如果失败重试3次
                .setConnectTimeout(30000)   // 连接超时30s
                .setServiceDiscoverRetry(3)  // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000)  // 发现服务超时20s
                .build();
        return connect0(mac, options)
                .concatMap(profilePair -> factory.create(mac, profilePair.second)
                        .map(bluetoothHolder -> {
                            m_bluetoothMap.put(mac, bluetoothHolder);
                            m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.CONNECTED));
                            m_client.registerConnectStatusListener(mac, m_connectStatusListener);
                            return bluetoothHolder;
                        }));
    }
}
