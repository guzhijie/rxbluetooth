package com.jack.rx.bluetooth;

/**
 * 描述:
 *
 * @author :jack.gu  Email: guzhijie1981@163.com
 * @since : 2019/7/20
 */
public class BluetoothException extends Exception {
    public BluetoothException(final String message) {
        super(message);
    }

    public BluetoothException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
