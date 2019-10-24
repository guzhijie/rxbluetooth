package com.jack.test.js100;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/24
 */
public enum JS100SampleType {
    //温度
    Temperature(1 | 16),
    Temperature_Vibrate(1 | 16),
    Temperature_Speed(2 | 16),
    Temperature_Distance(4 | 16);

    public final int type;

    JS100SampleType(int t) {
        this.type = t;
    }
}
