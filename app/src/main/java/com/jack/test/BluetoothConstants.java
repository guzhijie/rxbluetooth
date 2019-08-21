package com.jack.test;

import java.util.UUID;

/**
 * 描述: com.kisen.mms.v4.util.bluetooth<br>
 *
 * @author :jack.gu
 * @since : 2019/2/12
 */
public interface BluetoothConstants {
    /**
     * JS100蓝牙<br>
     *
     * @see #UUID_FFF0: 服务UUID
     * @see #UUID_FFF1: 读写特征UUID
     * @see #UUID_FFF2: 数据通知特征UUID
     * @see #UUID_FFF3: 读取缓存特征UUID
     * @see #UUID_2A19: 电量特征UUID
     *
     * @see #UUID_180A: 制造商服务UUID
     * @see #UUID_2A29: 制造商UUID
     */
    UUID UUID_FFF0 = UUID.fromString("0000fff0-0000-1000-8000-00805f9b34fb");
    UUID UUID_FFF1 = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    UUID UUID_FFF2 = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");
    UUID UUID_FFF3 = UUID.fromString("0000fff3-0000-1000-8000-00805f9b34fb");
    UUID UUID_2A19 = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb");
    UUID UUID_180A = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    UUID UUID_2A29 = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    /**
     * ZC100 蓝牙笔<br>
     *
     * @see #UUID_FFE0: 服务UUID
     * @see #UUID_FFE4: 蓝牙笔数据特征UUID
     */
    UUID UUID_FFE0 = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    UUID UUID_FFE4 = UUID.fromString("0000ffe4-0000-1000-8000-00805f9b34fb");

    /**
     * 电量显示
     *
     * @see #BATTERY_SERVICE_UUID 电量服务UUID
     * @see #BATTERY_LEVEL_UUID 电量特征UUID
     */
    UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb");
    UUID BATTERY_LEVEL_UUID = UUID.fromString("00002a1b-0000-1000-8000-00805f9b34fb");

    //写入数据最大长度
    int WRITE_DATA_MAX_LEN = 20;
    //读取数据最小长度
    int READ_DATA_MIN_LEN = 2;

    //JS100参数校验开始索引
    int JS100_COMMAND_XOR_BEGIN_INDEX = 2;
    //JS100参数校验结束索引
    int JS100_COMMAND_XOR_END_INDEX = 9;
}
