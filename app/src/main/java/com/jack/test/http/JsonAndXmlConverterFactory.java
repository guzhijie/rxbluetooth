package com.jack.test.http;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 描述:
 *
 * @author :jack.gu
 * @since : 2019/11/7 0007
 */
public final class JsonAndXmlConverterFactory extends Converter.Factory {
    private final Converter.Factory jsonFactory;
    private final Converter.Factory xmlFactory;

    private JsonAndXmlConverterFactory(Converter.Factory jsonFactory, Converter.Factory xmlFactory) {
        this.jsonFactory = jsonFactory;
        this.xmlFactory = xmlFactory;
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof Json) {
                return jsonFactory.responseBodyConverter(type, annotations, retrofit);
            }
            if (annotation instanceof Xml) {
                return xmlFactory.responseBodyConverter(type, annotations, retrofit);
            }
        }
        return null;
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        for (Annotation annotation : methodAnnotations) {
            if (annotation instanceof Json) {
                return jsonFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            }
            if (annotation instanceof Xml) {
                return xmlFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
            }
        }
        return null;
    }

    public static JsonAndXmlConverterFactory create(Converter.Factory jsonFactory, Converter.Factory xmlFactory) {
        return new JsonAndXmlConverterFactory(jsonFactory, xmlFactory);
    }

    @Retention(RUNTIME)
    public @interface Json {
    }

    public @Retention(RUNTIME)
    @interface Xml {
    }
}
