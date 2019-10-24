package com.jack.test.zc1000;

import com.jack.test.SensorData;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/16
 */
public class ZC1000SensorData extends SensorData<ZC1000SensorData, String, String, Void, Void> {
    private static final int PREX_SIZE = 2;
    private static final String TEMPERATURE_PREX = "T:";
    private static final String RFID_PREX = "R:";
    private static final String VIBRATE_PREX = "D:";
    private final String value;

    public ZC1000SensorData(final byte[] bytes) {
        value = new String(bytes);
    }

    @Override
    public String getTemperature() {
        return getValue(TEMPERATURE_PREX);
    }

    @Override
    public String getVibrate() {
        return getValue(VIBRATE_PREX);
    }

    @Override
    public Void getSpeed() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Void getDistance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRFID() {
        return getValue(RFID_PREX);
    }

    /**
     * 满足以下条件<br>
     * 1,字符串不为null 或者空<br>
     * 2,长度大于{@link #PREX_SIZE}，因为蓝牙笔前{@link #PREX_SIZE}个字符是表示类型的头<br>
     * 3,和当前的类型 RFID("R:"), Temperature("T:"), Vibrate("D:");匹配
     */
    private String getValue(String prex) {
        if (!value.isEmpty()
                && value.length() > PREX_SIZE
                && prex.equals(value.substring(0, PREX_SIZE))) {
            return value.substring(PREX_SIZE).trim();
        } else {
            throw new IllegalArgumentException("value = " + value + " && prex " + prex);
        }
    }
}
