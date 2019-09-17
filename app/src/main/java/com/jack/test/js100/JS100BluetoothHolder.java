package com.jack.test.js100;

import com.jack.rx.bluetooth.BluetoothException;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.SensorBluetoothHolder;

import java.util.Arrays;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.jack.test.BluetoothConstants.BATTERY_SERVICE_UUID;
import static com.jack.test.BluetoothConstants.READ_DATA_MIN_LEN;
import static com.jack.test.BluetoothConstants.UUID_2A19;
import static com.jack.test.BluetoothConstants.UUID_FFF0;
import static com.jack.test.BluetoothConstants.UUID_FFF1;
import static com.jack.test.BluetoothConstants.UUID_FFF2;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/13
 */
public final class JS100BluetoothHolder extends SensorBluetoothHolder<JS100SensorData> {
    public JS100BluetoothHolder(final String mac) {
        super(mac, RxBluetooth.getInstance());
    }

    @Override
    public Observable<JS100SensorData> sensorObservable() {
        return rxBluetooth.write(mac, UUID_FFF0, UUID_FFF1, sensorParameter())
                .concatMap(aBoolean -> rxBluetooth.notify(mac, UUID_FFF0, UUID_FFF2))
                .compose(notifyTransformer(UUID_FFF0, UUID_FFF2));
    }

    @Override
    public Observable<Float> readPower() {
        return rxBluetooth.notify(mac, BATTERY_SERVICE_UUID, UUID_2A19)
                .map(bytes -> bytes[0] & 0xFF)
                .map(Integer::floatValue);
    }

    @Override
    public <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID) {
        //UUID_FFF0, UUID_FFF1
        if (UUID_FFF0.equals(serviceUUID) && UUID_FFF2.equals(characterUUID)) {
            return upstream -> upstream.lift((ObservableOperator<T, byte[]>) observer -> {
                return new Observer<byte[]>() {
                    private Disposable m_disposable;
                    private JS100SensorData m_js100SensorData = new JS100SensorData();

                    @Override
                    public void onSubscribe(final Disposable d) {
                        m_disposable = d;
                        observer.onSubscribe(d);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public void onNext(final byte[] bytes) {
                        observer.onNext((T) m_js100SensorData);
                    }

                    @Override
                    public void onError(final Throwable e) {
                        observer.onError(e);
                        m_disposable.dispose();
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                        m_disposable.dispose();
                    }
                };
            });
        } else {
            return upstream -> upstream.map(bytes -> (T) bytes);
        }
    }

    @Override
    public <T> ObservableTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID) {
        //UUID_FFF0, UUID_FFF1
        if (UUID_FFF0.equals(serviceUUID) && UUID_FFF1.equals(characterUUID)) {
            return upstream -> upstream.map(data -> {
                if (data.length > READ_DATA_MIN_LEN
                        && data[1] == data.length - READ_DATA_MIN_LEN
                        && ('S' == (data[0] & 0xFF) || 'C' == (data[0] & 0xFF))) {
                    String s = new String(data, READ_DATA_MIN_LEN, data[1]).trim();
                    switch (s) {
                        case "OK":
                            //noinspection unchecked
                            return (T) Boolean.TRUE;
                        case "ERR":
                        default:
                            throw new BluetoothException(String.format("read: data = %s", s));
                    }
                } else {
                    throw new BluetoothException(String.format("read: data = %s", Arrays.toString(data)));
                }
            });
        } else {
            return upstream -> upstream.map(bytes -> (T) bytes);
        }
    }

    private byte[] sensorParameter() {
        return null;
    }
}
