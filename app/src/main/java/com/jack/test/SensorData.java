package com.jack.test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/13 0013
 */
public abstract class SensorData<E extends SensorData<E>> {
    public abstract <T> T getTemperature();

    public abstract <V> V getVibrate();

    public abstract String getRFID();

    public Type getType() {
        ParameterizedType parameterizedType = (ParameterizedType) this.getClass().getGenericSuperclass();
        assert parameterizedType != null;
        return parameterizedType.getActualTypeArguments()[0];
    }
}
