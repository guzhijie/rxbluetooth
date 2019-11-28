package com.jack.test.js100;

import android.util.Pair;

import com.jack.rx.bluetooth.RxBluetooth;
import com.jack.test.SensorData;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOperator;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;

import static com.jack.test.BluetoothConstants.UUID_FFF0;
import static com.jack.test.BluetoothConstants.UUID_FFF3;
import static com.jack.test.BluetoothConstants.WRITE_DATA_MAX_LEN;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/8/13
 */
public class JS100SensorData extends SensorData<JS100SensorData, Float, Float, Float, Float> {
    //温度
    private float temp;
    //采样类型
    private int sampleType;
    //带宽
    private int bandwidth;
    //采样点数
    private int samplePoints;
    //总包数
    private int totalPackages;
    //系数
    private float factor;
    //数据包
    private short[] data;
    //计算缓存值
    private float[] values;

    @Override
    public Float getTemperature() {
        return temp;
    }

    @Override
    public Float getVibrate() {
        return values[0];
    }

    @Override
    public Float getSpeed() {
        return values[5];
    }

    @Override
    public Float getDistance() {
        return values[3];
    }

    @Override
    public String getRFID() {
        throw new UnsupportedOperationException();
    }

    /**
     * 系数 : {@link #factor} <br>
     * 绝对值最大值 : max(|{@link #data}|) <br>
     * (最大-最小) : max({@link #data}) - min({@link #data}) <br>
     * 平均值 : average({@link #data}) <br>
     * 绝对值均值: average(|{@link #data}|)<br>
     * 平方根均值 : ∑(√￣|{@link #data}|)/{@link #samplePoints} <br>
     * 平方均值: √￣(∑{@link #data}^2/{@link #samplePoints}) <br>
     *
     * @return 返回值: <br>
     * [  绝对值最大值*系数, <br>(最大-最小)*系数, <br>平均值*系数, <br> 绝对值均值*系数, <br> 平方根均值*系数,<br> 平方均值*系数 ]
     */
    private void calcCachedRelateValue() {
        //最大值
        int max = this.data[0];
        //绝对值最大值
        float absMax = Math.abs(max);
        //最小值
        int min = this.data[0];
        //和值
        int sum = 0;
        //绝对值和
        int absSum = 0;
        //平方和
        float powerSum = 0.0f;
        //平方根和
        float sqrSum = 0.0f;
        for (int i = 0; i < this.samplePoints; ++i) {
            float absValue = Math.abs(this.data[i]);
            if (absValue > absMax) {
                absMax = absValue;
            }
            if (this.data[i] > max) {
                max = this.data[i];
            }
            if (this.data[i] < min) {
                min = this.data[i];
            }
            sum += this.data[i];
            absSum += (this.data[i] > 0 ? this.data[i] : -this.data[i]);
            powerSum += Math.pow(this.data[i], 2);
            sqrSum += Math.sqrt(Math.abs(this.data[i]));
        }
        values = new float[]{
                absMax * factor,
                (max - min) * factor,
                (sum * factor / samplePoints),
                (absSum * factor / samplePoints),
                (sqrSum * factor / samplePoints),
                (float) (Math.sqrt(powerSum / samplePoints) * factor),
        };
    }

    public static class Operator implements ObservableOperator<JS100SensorData, byte[]> {
        private JS100BluetoothHolder m_js100BluetoothHolder;

        public Operator(JS100BluetoothHolder js100BluetoothHolder) {
            this.m_js100BluetoothHolder = js100BluetoothHolder;
        }

        @Override
        public Observer<? super byte[]> apply(Observer<? super JS100SensorData> observer) throws Exception {
            return new Observer<byte[]>() {
                private Disposable m_disposable;
                private JS100SensorData m_js100SensorData = new JS100SensorData();
                private byte[] m_buffer;
                private boolean[] m_recvFlag;
                private int m_maxIndex = 0;
                private boolean m_firstPackageReceived = false;

                @Override
                public void onSubscribe(final Disposable d) {
                    m_disposable = d;
                    observer.onSubscribe(d);
                }

                @Override
                public void onNext(final byte[] value) {
                    int index = (value[0] & 0xFF);
                    //如果未接接受到第一个数据包，则只接受第一个包。
                    if (!m_firstPackageReceived && 0 == index) {
                        m_firstPackageReceived = true;
                        if (0 == firstDataPackage(value)) {
                            m_js100SensorData.data = new short[0];
                            observer.onNext(m_js100SensorData);
                            reset();
                        } else {
                            m_buffer = new byte[m_js100SensorData.totalPackages * (WRITE_DATA_MAX_LEN - 1)];
                            m_recvFlag = new boolean[1 + m_js100SensorData.totalPackages];
                            m_recvFlag[0] = true;
                        }
                    } else if (m_firstPackageReceived && index > 0) {
                        //如果已经接受到了第一个数据包，那么只接受后续的数据包。
                        if (!m_recvFlag[index]) {
                            m_recvFlag[index] = true;
                            System.arraycopy(value, 1, m_buffer, (index - 1) * (WRITE_DATA_MAX_LEN - 1), (WRITE_DATA_MAX_LEN - 1));
                            m_maxIndex = index > m_maxIndex ? index : m_maxIndex;
                        }
                        Pair<Integer, byte[]> pair = getLostDataPackageCountAndIndexArray();
                        if (0 != pair.first) {
                            Single.timer(200, TimeUnit.MICROSECONDS)
                                    .flatMap(aLong -> RxBluetooth.getInstance()
                                            .write(m_js100BluetoothHolder.getMac(), UUID_FFF0, UUID_FFF3, pair.second))
                                    .onErrorReturn(throwable -> false)
                                    .subscribe();
                        } else if (m_maxIndex == m_recvFlag.length - 1) {
                            m_js100SensorData.data = new short[m_buffer.length / 2];
                            for (int i = 0; i < m_js100SensorData.data.length; ++i) {
                                m_js100SensorData.data[i] = (short) (m_buffer[i * 2] & 0xFF | m_buffer[i * 2 + 1] << 8 & 0xFF00);
                            }
                            m_js100SensorData.calcCachedRelateValue();
                            observer.onNext(m_js100SensorData);
                            reset();
                        }
                    }
                }

                @Override
                public void onError(final Throwable e) {
                    observer.onError(e);
                    m_disposable.dispose();
                }

                @Override
                public void onComplete() {
                    observer.onComplete();
                    m_disposable.dispose();
                }

                private void reset() {
                    this.m_js100SensorData = new JS100SensorData();
                    this.m_buffer = null;
                    this.m_recvFlag = null;
                    this.m_maxIndex = 0;
                    this.m_firstPackageReceived = false;
                }

                /**
                 * 返回丢失的数据索引数量和索引数组
                 * @return
                 */
                private Pair<Integer, byte[]> getLostDataPackageCountAndIndexArray() {
                    byte[] lostIndex = new byte[WRITE_DATA_MAX_LEN];
                    //初始化丢失的索引数组。
                    for (int i = 0; i < lostIndex.length; ++i) {
                        lostIndex[i] = (byte) 0xFF;
                    }
                    int nextIndex = 0;
                    /**
                     * 1,i之所以从1开始，因为0表示第一个数据包，并且已经设置为true
                     * 2,i需要小于{@link m_maxIndex}因为当前最大的数据包索引为{@link m_maxIndex}
                     * 3,增加nextIndex < WRITE_DATA_MAX_LEN 判断是因为一次最多重取{@link WRITE_DATA_MAX_LEN}
                     */
                    for (int i = 1; i < m_maxIndex && i < m_recvFlag.length && nextIndex < WRITE_DATA_MAX_LEN; ++i) {
                        if (!m_recvFlag[i]) {
                            lostIndex[nextIndex++] = (byte) i;
                        }
                    }
                    return Pair.create(nextIndex, lostIndex);
                }

                /**
                 * 处理接受到的第一个数据包，返回数据包总长度
                 * @param value
                 * @return
                 */
                private int firstDataPackage(byte[] value) {
                    int temp = (value[1] << 8 & 0xFF00) | (value[2] & 0xFF);
                    int sampleType = value[3] & 0xFF;
                    int bandwidth = (value[4] << 8 & 0xFF00) | (value[5] & 0xFF);
                    int samplePoints = (value[6] << 8 & 0xFF00) | (value[7] & 0xFF);
                    int totalPackages = samplePoints * 2 / (WRITE_DATA_MAX_LEN - 1) + (0 == (samplePoints * 2) % (WRITE_DATA_MAX_LEN - 1) ? 0 : 1);
                    int factor = (value[12] << 24 & 0xFF000000) | (value[11] << 16 & 0xFF0000) | (value[10] << 8 & 0xFF00) | (value[9] & 0xFF);
                    m_js100SensorData.temp = temp / 100.0f;
                    m_js100SensorData.sampleType = sampleType;
                    m_js100SensorData.bandwidth = bandwidth;
                    m_js100SensorData.samplePoints = samplePoints;
                    m_js100SensorData.totalPackages = totalPackages;
                    m_js100SensorData.factor = Float.intBitsToFloat(factor);
                    return m_js100SensorData.totalPackages;
                }
            };
        }
    }
}
