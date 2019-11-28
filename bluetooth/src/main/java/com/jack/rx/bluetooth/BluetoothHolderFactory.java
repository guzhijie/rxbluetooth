package com.jack.rx.bluetooth;

import com.inuker.bluetooth.library.model.BleGattProfile;

import io.reactivex.Observable;
import io.reactivex.Single;


/**
 * 描述:
 *
 * @author :jack.gu  
 * @since : 2019/8/7
 */
public interface BluetoothHolderFactory {
    Single<? extends BluetoothHolder> create(String mac, BleGattProfile bleGattProfile);
}
