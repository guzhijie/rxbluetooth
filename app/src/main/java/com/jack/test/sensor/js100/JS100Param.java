package com.jack.test.sensor.js100;

import static com.jack.test.sensor.BluetoothConstants.JS100_COMMAND_XOR_BEGIN_INDEX;
import static com.jack.test.sensor.BluetoothConstants.JS100_COMMAND_XOR_END_INDEX;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/24
 */
public final class JS100Param {
    private JS100SampleType m_type;
    private JS100SampleFrequency m_frequency;
    private JS100SamplePoint m_point;
    private int m_factor;

    public JS100SampleType getType() {
        return m_type;
    }

    public JS100Param setType(JS100SampleType type) {
        m_type = type;
        return this;
    }

    public JS100SampleFrequency getFrequency() {
        return m_frequency;
    }

    public JS100Param setFrequency(JS100SampleFrequency frequency) {
        m_frequency = frequency;
        return this;
    }

    public JS100SamplePoint getPoint() {
        return m_point;
    }

    public JS100Param setPoint(JS100SamplePoint point) {
        m_point = point;
        return this;
    }

    public int getFactor() {
        return m_factor;
    }

    public JS100Param setFactor(int factor) {
        m_factor = factor;
        return this;
    }

    public byte[] toByteArray() {
        byte[] bytes = new byte[20];
        bytes[0] = 'S';
        bytes[1] = 8;
        //采样类型
        bytes[2] = (byte) (m_type.type & 0xFF);
        //带宽
        bytes[3] = (byte) ((m_frequency.frequency >> 8) & 0xFF);
        bytes[4] = (byte) (m_frequency.frequency & 0xFF);
        //采样点数
        bytes[5] = (byte) ((m_point.point >> 8) & 0xFF);
        bytes[6] = (byte) (m_point.point & 0xFF);
        //只有温度,才有发射率系数
        bytes[7] = (byte) ((m_factor >> 8) & 0xFF);
        bytes[8] = (byte) (m_factor & 0xFF);
        //异或校验位
        bytes[9] = 0;
        for (int index = JS100_COMMAND_XOR_BEGIN_INDEX; index < JS100_COMMAND_XOR_END_INDEX; ++index) {
            bytes[9] ^= bytes[index];
        }
        return bytes;
    }
}
