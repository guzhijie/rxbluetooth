package com.jack.test;

import com.jack.rx.bluetooth.BluetoothHolder;
import com.jack.rx.bluetooth.RxBluetooth;

import io.reactivex.Observable;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/16
 */
public abstract class SensorBluetoothHolder implements BluetoothHolder {
    protected final String mac;
    protected final RxBluetooth rxBluetooth;

    protected SensorBluetoothHolder(final String mac, final RxBluetooth rxBluetooth) {
        this.mac = mac;
        this.rxBluetooth = rxBluetooth;
    }

    @Override
    final public String getMac() {
        return mac;
    }

    public abstract Observable<Object> sensorObservable();

}
