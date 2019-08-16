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
public interface BluetoothHolder {
    String getMac();

    Observable<Float> readPower();

    ObservableTransformer<byte[], Object> notifyTransformer(UUID serviceUUID, UUID characterUUID);

    ObservableTransformer<byte[], ?> readTransformer(UUID serviceUUID, UUID characterUUID);
}
