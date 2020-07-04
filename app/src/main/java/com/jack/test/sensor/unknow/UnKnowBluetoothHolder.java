package com.jack.test.sensor.unknow;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.sensor.SensorBluetoothHolder;

import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;

import static com.jack.test.sensor.BluetoothConstants.UUID_180A;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2020/7/2
 */
public class UnKnowBluetoothHolder extends SensorBluetoothHolder<UnKnowSensorData,Void> {
    public UnKnowBluetoothHolder(String mac, BleGattProfile bleGattProfile) {
        super(mac, RxBluetooth.getInstance(), bleGattProfile);
    }

    @Override
    public Observable<UnKnowSensorData> sensorObservable(Void param) {
        return null;
    }

    @Override
    public Observable<Float> readPower() {
        return null;
    }

    @Override
    public <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID) {
        return null;
    }

    @Override
    public <T> ObservableTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID) {
        return null;
    }

    @Override
    public Single<String> deviceInfo() {
        return m_rxBluetooth.read(m_mac, UUID_180A, UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb"))
                .map(String::new);
    }
}
