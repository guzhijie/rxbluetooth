package com.jack.test.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;

import java.util.List;
import java.util.Objects;

import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Converter;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/20
 */
public final class TransformerUtil {
    /**
     * @param clazz:期望转化的数据类型
     * @param converter:从http返回数据转成JSON类型的赚钱
     * @param <U>:http返回的原始数据类型
     * @param <D>:期望转化的数据类型
     * @return
     */
    public static <U, D> SingleTransformer<U, D> httpResponseToJavaObject(@NonNull Class<D> clazz, @NonNull Converter<U, JSON> converter) {
        return upstream -> upstream.subscribeOn(Schedulers.newThread())
                .map(u -> Objects.requireNonNull(converter.convert(u)).toJavaObject(clazz))
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * @param clazz
     * @param converter
     * @param <U>
     * @param <D>
     * @return
     * @see #httpResponseToJavaObject(Class, Converter)
     */
    public static <U, D> SingleTransformer<U, List<D>> httpResponseToJavaList(@NonNull Class<D> clazz, @NonNull Converter<U, JSON> converter) {
        return upstream -> upstream.compose(httpResponseToJavaObject(JSONArray.class, converter))
                .map(jsonArray -> jsonArray.toJavaList(clazz));
    }
}
