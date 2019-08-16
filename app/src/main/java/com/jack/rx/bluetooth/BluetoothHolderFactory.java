package com.jack.rx.bluetooth;

import com.inuker.bluetooth.library.model.BleGattProfile;

import io.reactivex.Observable;


/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/8/7
 */
public interface BluetoothHolderFactory {
    Observable<? extends BluetoothHolder> create(String mac, BleGattProfile bleGattProfile);
}
