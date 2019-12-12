package com.jack.rx.bluetooth;

import com.inuker.bluetooth.library.model.BleGattProfile;

import io.reactivex.Single;


/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/7
 */
public interface BluetoothHolderFactory {
    /**
     * 根据ble设备内置的厂家信息，创建对应的{@link BluetoothHolder}
     *
     * @param mac
     * @param bleGattProfile
     * @return
     */
    Single<? extends BluetoothHolder> create(String mac, BleGattProfile bleGattProfile);
}
