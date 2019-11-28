package com.jack.rx.bluetooth;


import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/7
 */
public interface BluetoothHolder {
    String getMac();

    Observable<Float> readPower();

    Single<String> deviceInfo();

    <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID);

    <T> ObservableTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID);
}
