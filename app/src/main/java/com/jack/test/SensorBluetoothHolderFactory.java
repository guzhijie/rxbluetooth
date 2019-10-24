package com.jack.test;

import android.util.Log;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.jack.rx.bluetooth.BluetoothHolderFactory;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.js100.JS100BluetoothHolder;
import com.jack.test.zc1000.ZC1000BluetoothHolder;

import io.reactivex.Observable;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/13
 */
public final class SensorBluetoothHolderFactory implements BluetoothHolderFactory, BluetoothConstants {
    private final static String TAG = SensorBluetoothHolderFactory.class.getName();

    @Override
    public Observable<SensorBluetoothHolder> create(final String mac, final BleGattProfile bleGattProfile) {
        if (bleGattProfile.containsCharacter(UUID_180A, UUID_2A29)) {
            return RxBluetooth.getInstance().read(mac, UUID_180A, UUID_2A29).map(bytes -> {
                String name = new String(bytes).trim();
                //noinspection MalformedFormatString
                Log.e(TAG, String.format("当前蓝牙设备制造商名字", name));
                return new JS100BluetoothHolder(mac);
            });
        } else {
            return Observable.just(new ZC1000BluetoothHolder(mac));
        }
    }
}
