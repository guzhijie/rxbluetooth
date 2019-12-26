package com.jack.rx.websocket;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.enums.ReadyState;
import org.java_websocket.handshake.ServerHandshake;

import java.net.ConnectException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/11/6
 */
public final class RxWebSocketClient {
    private final static String TAG = RxWebSocketClient.class.getName();
    private final static int MAX_RECONNECT = 3;
    private final PublishSubject<ReadyState> m_reConnectSubject = PublishSubject.create();
    private final PublishSubject<String> m_messageSubject = PublishSubject.create();
    private URI m_serverUri;
    private Draft m_protocolDraft;
    private Map<String, String> m_httpHeaders;
    private int m_connectTimeout = -1;
    private WebSocketClientImpl m_webSocketClient;
    private boolean m_autoReconnect;

    public RxWebSocketClient(@NonNull URI serverUri, boolean autoReconnect) {
        this.m_serverUri = serverUri;
        this.m_autoReconnect = autoReconnect;
    }

    public RxWebSocketClient(@NonNull URI serverUri, Draft protocolDraft, boolean autoReconnect) {
        this.m_serverUri = serverUri;
        this.m_protocolDraft = protocolDraft;
        this.m_autoReconnect = autoReconnect;
    }

    public RxWebSocketClient(@NonNull URI serverUri, Map<String, String> httpHeaders,
                             boolean autoReconnect) {
        this.m_serverUri = serverUri;
        this.m_httpHeaders = httpHeaders;
        this.m_autoReconnect = autoReconnect;
    }

    public RxWebSocketClient(@NonNull URI serverUri, Draft protocolDraft,
                             Map<String, String> httpHeaders, boolean autoReconnect) {
        this.m_serverUri = serverUri;
        this.m_protocolDraft = protocolDraft;
        this.m_httpHeaders = httpHeaders;
        this.m_autoReconnect = autoReconnect;
    }

    public RxWebSocketClient(@NonNull URI serverUri, Draft protocolDraft,
                             Map<String, String> httpHeaders, int connectTimeout,
                             boolean autoReconnect) {
        this.m_serverUri = serverUri;
        this.m_protocolDraft = protocolDraft;
        this.m_httpHeaders = httpHeaders;
        this.m_connectTimeout = connectTimeout;
        this.m_autoReconnect = autoReconnect;
    }

    public Observable<String> onMessageObservable() {
        return m_messageSubject;
    }

    @SuppressLint("CheckResult")
    public Single<Boolean> connect() {
        if (m_webSocketClient == null) {
            return connect0().map(pair -> {
                m_webSocketClient = pair.first;
                if (m_autoReconnect) {
                    //noinspection ResultOfMethodCallIgnored
                    m_webSocketClient.onEventObservable()
                            .ofType(Exception.class)
                            .take(1)
                            .singleOrError()
                            .flatMap(o -> connect0()
                                    .retryWhen(throwableObservable -> throwableObservable
                                            .zipWith(Observable.range(1, MAX_RECONNECT).toFlowable(BackpressureStrategy.BUFFER), (throwable, integer) -> integer)
                                            .flatMap(i -> Observable.timer(i, TimeUnit.SECONDS).toFlowable(BackpressureStrategy.BUFFER)))
                                    .doOnSuccess(pair1 -> {
                                        m_webSocketClient = pair1.first;
                                        m_reConnectSubject.onNext(ReadyState.OPEN);
                                    })
                                    .doOnError(throwable -> {
                                        m_webSocketClient = null;
                                        m_reConnectSubject.onNext(ReadyState.CLOSED);
                                    }))
                            .subscribe(pair1 -> Log.i(TAG, String.format("reconnect ok : %s", pair1.toString())),
                                    throwable -> Log.e(TAG, String.format("reconnect failed : %s", throwable.getMessage())));
                }
                return true;
            });
        } else {
            if (m_webSocketClient.isOpen()) {
                return Single.just(true);
            } else if (m_autoReconnect) {
                return m_reConnectSubject.take(1)
                        .map(readystate -> readystate == ReadyState.OPEN)
                        .singleOrError();
            } else {
                return Single.just(false);
            }
        }
    }

    public Single<Boolean> close() {
        if (m_webSocketClient == null) {
            return Single.just(true);
        } else if (m_webSocketClient.isOpen()) {
            return close0();
        } else if (m_autoReconnect) {
            return m_reConnectSubject.take(1).singleOrError()
                    .flatMap(readystate -> readystate == ReadyState.OPEN ? close0() : Single.just(true));
        } else {
            return Single.just(true);
        }
    }

    @SuppressLint("CheckResult")
    private Single<Pair<WebSocketClientImpl, ReadyState>> connect0() {
        return Single.just(createWebSocketClientImpl())
                .flatMap(webSocketClient -> webSocketClient.onEventObservable()
                        .doOnSubscribe(disposable -> webSocketClient.connect())
                        .take(1)
                        .singleOrError()
                        .map(o -> {
                            if (o instanceof ServerHandshake) {
                                ServerHandshake serverHandshake = (ServerHandshake) o;
                                if (101 == serverHandshake.getHttpStatus()) {
                                    webSocketClient.onEventObservable()
                                            .ofType(String.class)
                                            .subscribe(m_messageSubject);
                                    return Pair.create(webSocketClient, webSocketClient.getReadyState());
                                } else {
                                    throw new ConnectException(serverHandshake.getHttpStatusMessage());
                                }
                            } else {
                                Exception e = (Exception) o;
                                throw new ConnectException(e.getMessage());
                            }
                        }));
    }

    private Single<Boolean> close0() {
        return m_webSocketClient.onEventObservable()
                .doOnSubscribe(disposable -> m_webSocketClient.close())
                .ofType(Object[].class)
                .take(1)
                .singleOrError()
                .map(objects -> {
                    int code = (int) objects[0];
                    String reason = (String) objects[1];
                    boolean remote = (boolean) objects[2];
                    return 1000 == code;
                });
    }

    private WebSocketClientImpl createWebSocketClientImpl() {
        if (m_protocolDraft == null && m_httpHeaders == null) {
            return new WebSocketClientImpl(m_serverUri);
        } else if (m_protocolDraft == null) {
            return new WebSocketClientImpl(m_serverUri, m_httpHeaders);
        } else if (m_httpHeaders == null) {
            return new WebSocketClientImpl(m_serverUri, m_protocolDraft);
        } else if (m_connectTimeout == -1) {
            return new WebSocketClientImpl(m_serverUri, m_protocolDraft, m_httpHeaders);
        } else {
            return new WebSocketClientImpl(m_serverUri, m_protocolDraft, m_httpHeaders, m_connectTimeout);
        }
    }
}
