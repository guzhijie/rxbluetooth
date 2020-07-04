package com.jack.test.sensor;

import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.model.BleGattService;
import com.jack.rx.bluetooth.BluetoothHolder;
import com.jack.rx.bluetooth.RxBluetooth;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.jack.test.sensor.BluetoothConstants.UUID_180A;
import static com.jack.test.sensor.BluetoothConstants.UUID_2A29;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/16
 */
public abstract class SensorBluetoothHolder<T extends SensorData<T, ?, ?, ?, ?>, P> implements BluetoothHolder {
    protected final String m_mac;
    protected final RxBluetooth m_rxBluetooth;
    protected final BleGattProfile m_bleGattProfile;

    protected SensorBluetoothHolder(final String mac, final RxBluetooth rxBluetooth, BleGattProfile bleGattProfile) {
        this.m_mac = mac;
        this.m_rxBluetooth = rxBluetooth;
        this.m_bleGattProfile = bleGattProfile;
    }

    @Override
    public final String getMac() {
        return m_mac;
    }

    @Override
    public Single<String> deviceInfo() {
        return m_rxBluetooth.read(m_mac, UUID_180A, UUID_2A29)
                .map(String::new);
    }

    @Override
    public List<BleGattService> getServices() {
        return m_bleGattProfile.getServices();
    }

    @Override
    public BleGattService getService(UUID serviceId) {
        return m_bleGattProfile.getService(serviceId);
    }

    @Override
    public boolean containsCharacter(UUID serviceId, UUID characterId) {
        return m_bleGattProfile.containsCharacter(serviceId,characterId);
    }

    public abstract Observable<T> sensorObservable(P param);

    public final Type getType() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return parameterizedType.getActualTypeArguments()[0];
    }
}
