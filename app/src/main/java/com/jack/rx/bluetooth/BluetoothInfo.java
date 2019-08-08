package com.jack.rx.bluetooth;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/8/7
 */
public final class BluetoothInfo {
    private float m_power;
    private float m_rssi;
    private BluetoothStatus m_bluetoothStatus;
    private BluetoothHolder m_bluetoothHolder;

    private BluetoothInfo() {

    }

    public float getPower() {
        return m_power;
    }


    public float getRssi() {
        return m_rssi;
    }


    public BluetoothStatus getBluetoothStatus() {
        return m_bluetoothStatus;
    }


    public BluetoothHolder getBluetoothHolder() {
        return m_bluetoothHolder;
    }


    public static class Builder {
        private BluetoothInfo m_info;

        public Builder() {

        }

        public Builder setPower(final float power) {
            m_info.m_power = power;
            return this;
        }

        public Builder setBluetoothHolder(final BluetoothHolder bluetoothHolder) {
            m_info.m_bluetoothHolder = bluetoothHolder;
            return this;
        }

        public Builder setBluetoothStatus(final BluetoothStatus bluetoothStatus) {
            m_info.m_bluetoothStatus = bluetoothStatus;
            return this;
        }

        public Builder setRssi(final float rssi) {
            m_info.m_rssi = rssi;
            return this;
        }

        public BluetoothInfo build() {
            return m_info;
        }
    }
}
