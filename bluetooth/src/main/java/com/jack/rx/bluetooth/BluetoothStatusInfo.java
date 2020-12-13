package com.jack.rx.bluetooth;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2020/12/10
 */
public final class BluetoothStatusInfo {
    public final String mac;
    public final BluetoothStatus status;

    public BluetoothStatusInfo(String mac, BluetoothStatus status) {
        this.mac = mac;
        this.status = status;
    }
}
