package com.jack.rx.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/11/6
 */
public final class WebSocketClientImpl extends WebSocketClient {
    private final PublishSubject<Object> m_subject = PublishSubject.create();

    WebSocketClientImpl(URI serverUri) {
        super(serverUri);
    }

    WebSocketClientImpl(URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
    }

    WebSocketClientImpl(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri, httpHeaders);
    }

    WebSocketClientImpl(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders) {
        super(serverUri, protocolDraft, httpHeaders);
    }

    WebSocketClientImpl(URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        m_subject.onNext(handshakedata);
    }

    @Override
    public void onMessage(String message) {
        m_subject.onNext(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        m_subject.onNext(new Object[]{code, reason, remote});
    }

    @Override
    public void onError(Exception ex) {
        m_subject.onNext(ex);
    }

    Observable<Object> onEventObservable() {
        return m_subject;
    }

}
