package com.jack.rx.bluetooth;


import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/8/7
 */
public abstract class BluetoothHolder {
    public final String mac;

    protected BluetoothHolder(final String mac) {
        this.mac = mac;
    }

    public abstract Observable<Float> readPower();

    public abstract ObservableTransformer<byte[], Object> notifyTransformer(UUID serviceUUID, UUID characterUUID);

    public abstract ObservableTransformer<byte[], ?> readTransformer(UUID serviceUUID, UUID characterUUID);

}
