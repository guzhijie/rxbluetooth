package com.jack.test.zc1000;

import com.jack.test.SensorData;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/16
 */
public class ZC1000SensorData extends SensorData<ZC1000SensorData> {
    public ZC1000SensorData(final byte[] bytes) {
    }

    @Override
    public <T> T getTemperature() {
        return null;
    }

    @Override
    public <V> V getVibrate() {
        return null;
    }
    @Override
    public String getRFID() {
        return null;
    }
}
