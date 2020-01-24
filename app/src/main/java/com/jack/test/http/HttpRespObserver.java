package com.jack.test.http;

import com.jack.test.http.HttpRespException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/12/20
 */
public abstract class HttpRespObserver<T> implements SingleObserver<T> {
    private final List<ExpInterceptor<HttpRespException>> m_httpRespExpInterceptors = new ArrayList<>();
    private final List<ExpInterceptor<Throwable>> m_netWorkExpInterceptors = new ArrayList<>();

    @Override
    public void onSubscribe(Disposable d) {

    }


    @Override
    final public void onError(Throwable e) {
        if (e instanceof HttpRespException) {
            for (ExpInterceptor<HttpRespException> interceptor : m_httpRespExpInterceptors) {
                interceptor.onIntercept((HttpRespException) e);
            }
        } else {
            for (ExpInterceptor<Throwable> interceptor : m_netWorkExpInterceptors) {
                interceptor.onIntercept(e);
            }
        }
    }

    public HttpRespObserver<T> addHttpRespExpInterceptor(ExpInterceptor<HttpRespException> interceptor) {
        m_httpRespExpInterceptors.add(interceptor);
        return this;
    }

    public HttpRespObserver<T> addNetworkExpInterceptor(ExpInterceptor<Throwable> interceptor) {
        m_netWorkExpInterceptors.add(interceptor);
        return this;
    }

    public interface ExpInterceptor<E extends Throwable> {
        /**
         * 拦截器方法
         *
         * @param e
         */
        void onIntercept(E e);
    }
}