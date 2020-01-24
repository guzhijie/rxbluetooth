package com.jack.test.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.jaxb.JaxbConverterFactory;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/20
 */
public final class HttpClient {
    private static final HttpClient HTTP_CLIENT = new HttpClient();
    private final Retrofit m_retrofit;

    private HttpClient() {
        m_retrofit = new Retrofit.Builder()
                .baseUrl("http://api.androidhive.info")
                .client(new OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .callTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .build())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JsonAndXmlConverterFactory.create(GsonConverterFactory.create(),
                        JaxbConverterFactory.create()))
                .build();
    }

    public static HttpClient getInstance() {
        return HTTP_CLIENT;
    }

    public <I> I createApi(Class<I> clazz) {
        return m_retrofit.create(clazz);
    }
}