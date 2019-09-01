package com.jack.test.zc1000;

import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.SensorBluetoothHolder;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static com.jack.test.BluetoothConstants.BATTERY_LEVEL_UUID;
import static com.jack.test.BluetoothConstants.BATTERY_SERVICE_UUID;
import static com.jack.test.BluetoothConstants.UUID_FFE0;
import static com.jack.test.BluetoothConstants.UUID_FFE4;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/8/13
 */
public final class ZC1000BluetoothHolder extends SensorBluetoothHolder {
    public ZC1000BluetoothHolder(final String mac) {
        super(mac, RxBluetooth.getInstance());
    }

    @Override
    public Observable<Object> sensorObservable() {
        return rxBluetooth.notify(mac, UUID_FFE0, UUID_FFE4)
                .compose(notifyTransformer(UUID_FFE0, UUID_FFE4));
    }

    @Override
    public Observable<Float> readPower() {
        return rxBluetooth.notify(mac, BATTERY_SERVICE_UUID, BATTERY_LEVEL_UUID)
                .map(bytes -> (bytes[0] & 0xFF))
                .map(Integer::floatValue);
    }

    @Override
    public ObservableTransformer<byte[], Object> notifyTransformer(UUID serviceUUID, UUID characterUUID) {
        // UUID_FFE0, UUID_FFE4
        if (UUID_FFE0.equals(serviceUUID) && UUID_FFE4.equals(characterUUID)) {
            return upstream -> upstream.map(ZC1000SensorData::new);
        } else {
            return upstream -> upstream.map(bytes -> bytes);
        }
    }

    @Override
    public ObservableTransformer<byte[], Object> readTransformer(UUID serviceUUID, UUID characterUUID) {
        return upstream -> upstream.map(bytes -> bytes);
    }

}
