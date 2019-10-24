package com.jack.test.js100;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/10/24
 */
public enum JS100SampleFrequency {
    //
    Freq_100Hz(100),
    Freq_200Hz(200),
    Freq_500Hz(500),
    Freq_1kHz(1000),
    Freq_2kHz(2000),
    Freq_5kHz(5000),
    Freq_10kHz(10000);
    public final int frequency;

    JS100SampleFrequency(int f) {
        this.frequency = f;
    }
}
