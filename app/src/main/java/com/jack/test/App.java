package com.jack.test;

import android.app.Application;

import com.jack.rx.bluetooth.RxBluetooth;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/29 0029
 */
public final class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RxBluetooth.init(this);
    }
}
