package com.jack.test;

import com.jack.rx.bluetooth.BluetoothHolder;
import com.jack.rx.bluetooth.RxBluetooth;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.reactivex.Observable;
import io.reactivex.Single;

import static com.jack.test.BluetoothConstants.UUID_180A;
import static com.jack.test.BluetoothConstants.UUID_2A29;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/16
 */
public abstract class SensorBluetoothHolder<T extends SensorData<T, ?, ?, ?, ?>, P> implements BluetoothHolder {
    protected final String m_mac;
    protected final RxBluetooth m_rxBluetooth;

    protected SensorBluetoothHolder(final String mac, final RxBluetooth rxBluetooth) {
        this.m_mac = mac;
        this.m_rxBluetooth = rxBluetooth;
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

    public abstract Observable<T> sensorObservable(P param);

    public final Type getType() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return parameterizedType.getActualTypeArguments()[0];
    }

}
