package com.jack.rx.bluetooth;

import com.inuker.bluetooth.library.Constants;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @see Constants.STATUS_UNKNOWN
 * @see Constants.STATUS_DEVICE_CONNECTED
 * @see Constants.STATUS_DEVICE_CONNECTING
 * @see Constants.STATUS_DEVICE_DISCONNECTING
 * @see Constants.STATUS_DEVICE_DISCONNECTED
 * @since : 2019/7/20
 */
public enum BluetoothStatus {

    //蓝牙设备状态
    UNKNOWN(new int[]{Constants.STATUS_UNKNOWN}),
    CONNECTED(new int[]{Constants.STATUS_DEVICE_CONNECTED, Constants.STATUS_CONNECTED}),
    CONNECTING(new int[]{Constants.STATUS_DEVICE_CONNECTING}),
    DISCONNECTING(new int[]{Constants.STATUS_DEVICE_DISCONNECTING}),
    DISCONNECTED(new int[]{Constants.STATUS_DEVICE_DISCONNECTED, Constants.STATUS_DISCONNECTED});

    final private int[] m_values;

    BluetoothStatus(final int[] values) {
        m_values = values;
    }

    public static BluetoothStatus valueOf(int value) {
        for (BluetoothStatus status : BluetoothStatus.values()) {
            for (int v : status.m_values) {
                if (v == value) {
                    return status;
                }
            }
        }
        return UNKNOWN;
    }
}
