package com.jack.test.sensor;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/13 0013
 */
public abstract class SensorData<E extends SensorData<E, T, V, S, D>, T, V, S, D> {
    public abstract T getTemperature();

    public abstract V getVibrate();

    public abstract S getSpeed();

    public abstract D getDistance();

    public abstract String getRFID();
}
