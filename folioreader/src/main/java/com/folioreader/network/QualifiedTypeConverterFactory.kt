package com.folioreader.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

@Retention(AnnotationRetention.RUNTIME)
internal annotation class Gson

@Retention(AnnotationRetention.RUNTIME)
internal annotation class Jackson

// Annotation based converters
// URL - https://github.com/square/retrofit/blob/b115292dba5f328fbc99e024c76d50866e94f3bf/samples/src/main/java/com/example/retrofit/JsonAndXmlConverters.java

class QualifiedTypeConverterFactory : Converter.Factory {

    val jacksonFactory: Converter.Factory
    val gsonFactory: Converter.Factory

    constructor(jacksonFactory: Converter.Factory, gsonFactory: Converter.Factory) : super() {
        this.jacksonFactory = jacksonFactory
        this.gsonFactory = gsonFactory
    }

    override fun responseBodyConverter(
        type: Type, annotations: Array<Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? {

        for (annotation in annotations) {
            if (annotation is Jackson) {
                return jacksonFactory.responseBodyConverter(type, annotations, retrofit)
            }
            if (annotation is Gson) {
                return gsonFactory.responseBodyConverter(type, annotations, retrofit)
            }
        }
        return null
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit
    ): Converter<*, RequestBody>? {

        for (annotation in parameterAnnotations) {
            if (annotation is Jackson) {
                return jacksonFactory.requestBodyConverter(
                    type, parameterAnnotations, methodAnnotations,
                    retrofit
                )
            }
            if (annotation is Gson) {
                return gsonFactory.requestBodyConverter(
                    type, parameterAnnotations, methodAnnotations,
                    retrofit
                )
            }
        }
        return null
    }
}