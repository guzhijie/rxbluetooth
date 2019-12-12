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
    /**
     * 获得当前ble设备的mac地址
     *
     * @return
     */
    String getMac();

    /**
     * 获得当前ble设备的电量
     *
     * @return
     */
    Observable<Float> readPower();

    /**
     * 获得当前ble设备的厂家信息
     *
     * @return
     */
    Single<String> deviceInfo();

    /**
     * {@link BaseRxBluetooth#notify(String, UUID, UUID)} 接口返回的数据转成所需的类型
     *
     * @param serviceUUID
     * @param characterUUID
     * @param <T>
     * @return
     */
    <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID);

    /**
     * {@link BaseRxBluetooth#read(String, UUID, UUID)}  接口返回的数据转成所需的类型
     *
     * @param serviceUUID
     * @param characterUUID
     * @param <T>
     * @return
     */
    <T> ObservableTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID);
}
