package com.jack.test.sensor.js100;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/24
 */
public enum JS100SamplePoint {
    //
    Point_256(256),
    Point_512(512),
    Point_1024(1024),
    Point_2048(2048);
    public final int point;

    JS100SamplePoint(int p) {
        this.point = p;
    }
}
