package com.jack.rx.bluetooth;


import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;

import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/7
 */
public abstract class BluetoothHolder {
    protected final String m_mac;
    protected final RxBluetooth m_rxBluetooth;
    protected final BleGattProfile m_bleGattProfile;

    protected BluetoothHolder(String mac, RxBluetooth rxBluetooth, BleGattProfile bleGattProfile) {
        m_mac = mac;
        m_rxBluetooth = rxBluetooth;
        m_bleGattProfile = bleGattProfile;
    }

    /**
     * 获得当前ble设备的mac地址
     *
     * @return
     */
    public final String getMac() {
        return m_mac;
    }

    public List<BleGattService> getServices() {
        return m_bleGattProfile.getServices();
    }

    public BleGattService getService(UUID serviceId) {
        return m_bleGattProfile.getService(serviceId);
    }

    public boolean containsCharacter(UUID serviceId, UUID characterId) {
        return m_bleGattProfile.containsCharacter(serviceId, characterId);
    }

    /**
     * 获得当前ble设备的电量
     *
     * @return
     */
    public abstract Observable<Float> readPower();

    public Single<Boolean> write(UUID serviceUUID, UUID characterUUID, byte[] value) {
        return this.m_rxBluetooth.write(m_mac, serviceUUID, characterUUID, value);
    }

    public <T> Observable<T> notify(UUID serviceUUID, UUID characterUUID) {
        return this.m_rxBluetooth.notify(m_mac, serviceUUID, characterUUID)
                .compose(notifyTransformer(serviceUUID, characterUUID));
    }

    public <T> Single<T> read(UUID serviceUUID, UUID characterUUID) {
        return this.m_rxBluetooth.read(m_mac, serviceUUID, characterUUID)
                .compose(readTransformer(serviceUUID, characterUUID));
    }

    /**
     * {@link BaseRxBluetooth#notify(String, UUID, UUID)} 接口返回的数据转成所需的类型
     *
     * @param serviceUUID
     * @param characterUUID
     * @param <T>
     * @return
     */
    protected abstract <T> ObservableTransformer<byte[], T> notifyTransformer(UUID serviceUUID, UUID characterUUID);

    /**
     * {@link BaseRxBluetooth#read(String, UUID, UUID)}  接口返回的数据转成所需的类型
     *
     * @param serviceUUID
     * @param characterUUID
     * @param <T>
     * @return
     */
    protected abstract <T> SingleTransformer<byte[], T> readTransformer(UUID serviceUUID, UUID characterUUID);

}
