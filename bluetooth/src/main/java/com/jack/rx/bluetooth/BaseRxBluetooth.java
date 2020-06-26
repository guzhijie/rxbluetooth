package com.jack.rx.bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.inuker.bluetooth.library.BluetoothClient;
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.SearchResult;
import com.inuker.bluetooth.library.search.response.SearchResponse;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.inuker.bluetooth.library.Constants.REQUEST_SUCCESS;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/7/6
 */
public abstract class BaseRxBluetooth {
    private final String TAG = BaseRxBluetooth.class.getName();
    protected final BluetoothClient m_client;

    protected BaseRxBluetooth(Context client) {
        m_client = new BluetoothClient(client);
    }

    /**
     * 判断蓝牙是否打开
     *
     * @return
     */
    public boolean isBluetoothOpened() {
        return m_client.isBluetoothOpened();
    }

    /**
     * 是否支持ble
     *
     * @return
     */
    public boolean isBleSupported() {
        return m_client.isBleSupported();
    }

    /**
     * 打开蓝牙，如果返回true，表示成功，否则表示失败
     *
     * @return
     */
    public Single<Boolean> openBluetooth() {
        final BluetoothStateListener[] listeners = new BluetoothStateListener[1];
        return Single.<Boolean>create(emitter -> {
            listeners[0] = new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(final boolean openOrClosed) {
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(openOrClosed);
                    }
                }
            };
            m_client.registerBluetoothStateListener(listeners[0]);
            if (!m_client.openBluetooth()) {
                emitter.onSuccess(false);
            }
        }).doFinally(() -> m_client.unregisterBluetoothStateListener(listeners[0]));
    }

    /**
     * 关闭蓝牙，如果返回true，表示成功，否则表示失败
     *
     * @return
     */
    public Single<Boolean> closeBluetooth() {
        final BluetoothStateListener[] listeners = new BluetoothStateListener[1];
        return Single.<Boolean>create(emitter -> {
            listeners[0] = new BluetoothStateListener() {
                @Override
                public void onBluetoothStateChanged(final boolean openOrClosed) {
                    if (!emitter.isDisposed()) {
                        emitter.onSuccess(!openOrClosed);
                    }
                }
            };
            m_client.registerBluetoothStateListener(listeners[0]);
            if (!m_client.closeBluetooth()) {
                emitter.onSuccess(false);
            }
        }).doFinally(() -> m_client.unregisterBluetoothStateListener(listeners[0]));
    }

    /**
     * 搜索蓝牙设备
     *
     * @param request
     * @param time：扫描时间
     * @param unit：时间单位
     * @return
     */
    public Observable<SearchResult> search(SearchRequest request, int time, TimeUnit unit) {
        return Observable.<SearchResult>create(emitter -> m_client.search(request,
                new SearchResponse() {
                    @Override
                    public void onSearchStarted() {

                    }

                    @Override
                    public void onDeviceFounded(final SearchResult device) {
                        emitter.onNext(device);
                    }

                    @Override
                    public void onSearchStopped() {
                        emitter.onComplete();
                    }

                    @Override
                    public void onSearchCanceled() {
                        emitter.onComplete();
                    }
                }))
                .take(time, unit)
                .doFinally(this::stopSearch);

    }

    public BluetoothStatus getConnectStatus(String mac) {
        return BluetoothStatus.valueOf(m_client.getConnectStatus(mac));
    }

    public Single<byte[]> read(String mac, UUID serviceUUID, UUID characterUUID) {
        return Single.create(emitter -> {
            m_client.read(mac, serviceUUID, characterUUID, (code, data) -> {
                if (code == REQUEST_SUCCESS) {
                    emitter.onSuccess(data);
                } else {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("read %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }
            });
        });
    }

    public Single<Boolean> write(String mac, UUID serviceUUID, UUID characterUUID, byte[] value) {
        return Single.create(emitter -> {
            m_client.write(mac, serviceUUID, characterUUID, value, code -> {
                if (code == REQUEST_SUCCESS) {
                    emitter.onSuccess(true);
                } else {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("write %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }
            });
        });
    }

    public Observable<byte[]> notify(String mac, UUID serviceUUID, UUID characterUUID) {
        return Observable.<byte[]>create(emitter -> m_client.notify(mac, serviceUUID, characterUUID, new BleNotifyResponse() {
            @Override
            public void onNotify(final UUID service, final UUID character, final byte[] value) {
                if (service.equals(serviceUUID) && character.equals(characterUUID)) {
                    emitter.onNext(value);
                }
            }

            @Override
            public void onResponse(final int code) {
                if (code != REQUEST_SUCCESS) {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("notify %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }
            }
        })).doFinally(() -> m_client.unnotify(mac, serviceUUID, characterUUID, code -> {
            Log.w(TAG, String.format("unnotify %s %b", mac, code == REQUEST_SUCCESS));
        }));
    }

    public Single<byte[]> readDescriptor(String mac, UUID serviceUUID, UUID characterUUID, UUID descriptorUUID) {
        return Single.create(emitter -> {
            m_client.readDescriptor(mac, serviceUUID, characterUUID, descriptorUUID, (code, data) -> {
                if (code == REQUEST_SUCCESS) {
                    emitter.onSuccess(data);
                } else {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("readDescriptor %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }
            });
        });
    }

    public Single<Boolean> writeDescriptor(String mac, UUID serviceUUID, UUID characterUUID, UUID descriptorUUID, byte[] value) {
        return Single.create(emitter -> {
            m_client.writeDescriptor(mac, serviceUUID, characterUUID, descriptorUUID, value, code -> {
                if (code == REQUEST_SUCCESS) {
                    emitter.onSuccess(true);
                } else {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("writeDescriptor %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }

            });
        });
    }

    public Observable<byte[]> indicate(String mac, UUID serviceUUID, UUID characterUUID) {
        return Observable.<byte[]>create(emitter -> m_client.indicate(mac, serviceUUID, characterUUID, new BleNotifyResponse() {
            @Override
            public void onNotify(final UUID service, final UUID character, final byte[] value) {
                if (service.equals(serviceUUID) && character.equals(characterUUID)) {
                    emitter.onNext(value);
                }
            }

            @Override
            public void onResponse(final int code) {
                if (code != REQUEST_SUCCESS) {
                    @SuppressLint("DefaultLocale")
                    String msg = String.format("indicate %s @ %s : %s error = %d", mac, serviceUUID.toString(), characterUUID.toString(), code);
                    emitter.onError(new BluetoothException(msg));
                }
            }
        })).doFinally(() -> m_client.unindicate(mac, serviceUUID, characterUUID, code -> {
            Log.w(TAG, String.format("unindicate %s %b", mac, code == REQUEST_SUCCESS));
        }));
    }


    /**
     * 读取ble设备信号强度，如果没有读到返回 {@link Float#NaN}
     *
     * @param mac
     * @return
     */
    public Observable<Float> readRssi(String mac) {
        return Observable.create(emitter -> m_client.readRssi(mac, (code, data) -> {
            emitter.onNext(code == REQUEST_SUCCESS ? data.floatValue() : Float.NaN);
            emitter.onComplete();
        }));
    }

    protected void clearRequest(String mac, int type) {
        m_client.clearRequest(mac, type);
    }

    protected void refreshCache(String mac) {
        m_client.refreshCache(mac);
    }

    /**
     * 连接蓝牙{@code mac}
     *
     * @param mac
     * @param options
     * @return
     */
    @SuppressLint("DefaultLocale")
    protected Single<Pair<BluetoothStatus, BleGattProfile>> connect0(String mac, BleConnectOptions options) {
        return Single.<Pair<BluetoothStatus, BleGattProfile>>create(emitter -> {
            m_client.connect(mac, options, (code, data) -> {
                if (code == REQUEST_SUCCESS) {
                    emitter.onSuccess(Pair.create(BluetoothStatus.CONNECTED, data));
                } else {
                    emitter.onError(new BluetoothException(String.format("connect code = %d", code)));
                }
            });
        }).subscribeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 断开蓝牙连接
     *
     * @param mac
     * @return
     */
    protected Single<BluetoothStatus> disconnect0(String mac) {
        final BleConnectStatusListener[] listeners = new BleConnectStatusListener[1];
        return Single.<BluetoothStatus>create(emitter -> {
            listeners[0] = new BleConnectStatusListener() {
                @Override
                public void onConnectStatusChanged(final String address, final int status) {
                    if (address.equals(mac) && !emitter.isDisposed()) {
                        emitter.onSuccess(BluetoothStatus.valueOf(status));
                    }
                }
            };
            m_client.registerConnectStatusListener(mac, listeners[0]);
            m_client.disconnect(mac);
        }).doFinally(() -> m_client.unregisterConnectStatusListener(mac, listeners[0]));
    }

    private void stopSearch() {
        m_client.stopSearch();
    }
}
