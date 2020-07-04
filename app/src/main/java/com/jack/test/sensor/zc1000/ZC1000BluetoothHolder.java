package com.jack.test.sensor.zc1000;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.sensor.SensorBluetoothHolder;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

import static com.jack.test.sensor.BluetoothConstants.BATTERY_LEVEL_UUID;
import static com.jack.test.sensor.BluetoothConstants.BATTERY_SERVICE_UUID;
import static com.jack.test.sensor.BluetoothConstants.UUID_FFE0;
import static com.jack.test.sensor.BluetoothConstants.UUID_FFE4;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/13
 */
public final class ZC1000BluetoothHolder extends SensorBluetoothHolder<ZC1000SensorData, Void> {
    public ZC1000BluetoothHolder(final String mac, BleGattProfile bleGattProfile) {
        super(mac, RxBluetooth.getInstance(), bleGattProfile);
    }

    @Override
    public Observable<Float> readPower() {
        return m_rxBluetooth.notify(m_mac, BATTERY_SERVICE_UUID, BATTERY_LEVEL_UUID)
                .map(bytes -> (bytes[0] & 0xFF))
                .map(Integer::floatValue);
    }

    @Override
    public <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID) {
        // UUID_FFE0, UUID_FFE4
        if (UUID_FFE0.equals(serviceUUID) && UUID_FFE4.equals(characterUUID)) {
            //noinspection unchecked
            return upstream -> upstream.map(bytes -> (T) new ZC1000SensorData(bytes));
        } else {
            return upstream -> upstream.map(bytes -> (T) bytes);
        }
    }

    @Override
    public <T> ObservableTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID) {
        return upstream -> upstream.map(bytes -> (T) bytes);
    }

    @Override
    public Observable<ZC1000SensorData> sensorObservable(Void param) {
        return m_rxBluetooth.notify(m_mac, UUID_FFE0, UUID_FFE4)
                .compose(notifyTransformer(UUID_FFE0, UUID_FFE4));
    }
}
