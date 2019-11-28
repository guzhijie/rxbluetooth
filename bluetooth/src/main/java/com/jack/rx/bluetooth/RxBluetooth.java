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
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/7/28
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class RxBluetooth extends BaseRxBluetooth {
    private static RxBluetooth m_rxBluetooth;
    private final PublishSubject<Pair<String, BluetoothStatus>> m_bluetoothStatusBus = PublishSubject.create();
    private final PublishSubject<String> m_stopReconnect = PublishSubject.create();
    private final Map<String, BluetoothHolder> m_bluetoothMap = new ConcurrentHashMap<>(8);
    private final BleConnectStatusListener m_connectStatusListener = new BleConnectStatusListener() {
        @Override
        public void onConnectStatusChanged(final String mac, final int status) {
            final BluetoothStatus bluetoothStatus = BluetoothStatus.valueOf(status);
            m_bluetoothStatusBus.onNext(Pair.create(mac, bluetoothStatus));
            if (BluetoothStatus.CONNECTED != bluetoothStatus) {
                autoConnect(mac, m_bluetoothMap.get(mac));
            }
        }

        @SuppressLint("CheckResult")
        private void autoConnect(String mac, BluetoothHolder holder) {
            connectByMac(mac, (mac1, bleGattProfile) -> Single.just(holder))
                    .takeUntil(m_stopReconnect.filter(s -> s.equals(mac)).take(1).singleOrError())
                    .doOnSubscribe(disposable -> m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.CONNECTING)))
                    .retry(3)
                    .doFinally(() -> m_client.unregisterConnectStatusListener(mac, m_connectStatusListener))
                    .subscribe(bluetoothHolder -> {
                            },
                            throwable -> {
                                m_bluetoothMap.remove(mac);
                                m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.DISCONNECTED));
                            });
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
                .concatMap(bluetoothHolder -> m_bluetoothStatusBus.filter(pair -> pair.first.equals(bluetoothHolder.getMac()))
                        .map(statusPair -> statusPair.second)
                        .startWith(getConnectStatus(bluetoothHolder.getMac()))
                        .join(bluetoothHolder.readPower(),
                                bluetoothStatus -> Observable.empty(),
                                power -> Observable.empty(),
                                (bluetoothStatus, power) -> new BluetoothInfo.Builder()
                                        .setBluetoothStatus(bluetoothStatus)
                                        .setPower(power))
                        .join(readRssi(bluetoothHolder.getMac()),
                                builder -> Observable.empty(),
                                rssi -> Observable.empty(),
                                (builder, rssi) -> builder.setRssi(rssi).build()));
    }

    public Single<BluetoothHolder> connect(String mac, BluetoothHolderFactory factory) {
        if (m_bluetoothMap.containsKey(mac)) {
            return bluetoothStatusObservable(mac).map(bluetoothStatus -> {
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

    public Single<BluetoothStatus> disconnect(String mac) {
        if (m_bluetoothMap.containsKey(mac)) {
            return bluetoothStatusObservable(mac)
                    .doOnSubscribe(disposable -> m_stopReconnect.onNext(mac))
                    .flatMap(bluetoothStatus -> {
                        if (bluetoothStatus == BluetoothStatus.DISCONNECTED) {
                            return Single.just(BluetoothStatus.DISCONNECTED);
                        } else {
                            return disconnect0(mac).doFinally(() -> m_bluetoothMap.remove(mac));
                        }
                    });

        } else {
            return Single.just(BluetoothStatus.DISCONNECTED);
        }
    }

    /**
     * 1, 订阅6s之后，查到当前设备状态(立刻查询，可能错误，因为状态不立马反映出来),如果得到{@link BluetoothStatus#CONNECTED}则结束;<br>
     * 2, 状态总线上，开启一个12s窗口，如果得到{@link BluetoothStatus#CONNECTED}则结束;<br>
     *
     * @param mac
     * @return
     */
    private Single<BluetoothStatus> bluetoothStatusObservable(String mac) {
        return m_bluetoothStatusBus.filter(pair -> pair.first.equals(mac) && pair.second == BluetoothStatus.CONNECTED)
                .takeLast(1, 12, TimeUnit.SECONDS)
                .map(stringBluetoothStatusPair -> stringBluetoothStatusPair.second)
                .mergeWith(Observable.timer(6, TimeUnit.SECONDS)
                        .map(aLong -> getConnectStatus(mac))
                        .filter(bluetoothStatus -> bluetoothStatus == BluetoothStatus.CONNECTED))
                .first(BluetoothStatus.DISCONNECTED);
    }

    private Single<BluetoothHolder> connectByMac(String mac, BluetoothHolderFactory factory) {
        BleConnectOptions options = new BleConnectOptions.Builder()
                .setConnectRetry(3)               // 连接如果失败重试3次
                .setConnectTimeout(30000)         // 连接超时30s
                .setServiceDiscoverRetry(3)       // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000) // 发现服务超时20s
                .build();
        return connect0(mac, options)
                .flatMap(profilePair -> factory.create(mac, profilePair.second).map(bluetoothHolder -> {
                    m_bluetoothMap.put(mac, bluetoothHolder);
                    m_bluetoothStatusBus.onNext(Pair.create(mac, BluetoothStatus.CONNECTED));
                    m_client.registerConnectStatusListener(mac, m_connectStatusListener);
                    return bluetoothHolder;
                }));
    }
}
