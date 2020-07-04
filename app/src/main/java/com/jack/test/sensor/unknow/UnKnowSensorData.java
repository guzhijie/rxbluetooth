package com.jack.test.sensor.unknow;

import com.jack.test.sensor.SensorData;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2020/7/2
 */
public final class UnKnowSensorData extends SensorData<UnKnowSensorData, Void,Void,Void,Void> {
    @Override
    public Void getTemperature() {
        return null;
    }

    @Override
    public Void getVibrate() {
        return null;
    }

    @Override
    public Void getSpeed() {
        return null;
    }

    @Override
    public Void getDistance() {
        return null;
    }

    @Override
    public String getRFID() {
        return null;
    }
}
