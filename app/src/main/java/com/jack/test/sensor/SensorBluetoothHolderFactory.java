package com.jack.test.sensor;

import android.util.Log;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.jack.rx.bluetooth.BluetoothHolderFactory;
import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.sensor.js100.JS100BluetoothHolder;
import com.jack.test.sensor.unknow.UnKnowBluetoothHolder;
import com.jack.test.sensor.zc1000.ZC1000BluetoothHolder;

import io.reactivex.Single;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/13
 */
public final class SensorBluetoothHolderFactory implements BluetoothHolderFactory, BluetoothConstants {
    private final static String TAG = SensorBluetoothHolderFactory.class.getName();

    @Override
    public Single<SensorBluetoothHolder> create(final String mac, final BleGattProfile bleGattProfile) {
        Log.e(TAG, String.format("SensorBluetoothHolderFactory.create mac : %s", mac));
        if (bleGattProfile.containsCharacter(UUID_180A, UUID_2A29)) {
            return RxBluetooth.getInstance().read(mac, UUID_180A, UUID_2A29).map(bytes -> {
                String name = new String(bytes).trim();
                //noinspection MalformedFormatString
                Log.e(TAG, String.format("当前蓝牙设备制造商名字", name));
                return new JS100BluetoothHolder(mac, bleGattProfile);
            });
        } else if (bleGattProfile.containsCharacter(UUID_FFE0, UUID_FFE4)) {
            return Single.just(new ZC1000BluetoothHolder(mac, bleGattProfile));
        } else {
            return Single.just(new UnKnowBluetoothHolder(mac, bleGattProfile));
        }
    }

}
