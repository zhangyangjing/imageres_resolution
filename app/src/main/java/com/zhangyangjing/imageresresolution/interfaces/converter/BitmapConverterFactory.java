package com.zhangyangjing.imageresresolution.interfaces.converter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by zhangyangjing on 13/07/2017.
 */

public final class BitmapConverterFactory extends Converter.Factory {

    public static BitmapConverterFactory create() {
        return new BitmapConverterFactory();
    }


    private BitmapConverterFactory() {

    }

    @Override
    public Converter<ResponseBody, Bitmap> responseBodyConverter(Type type,
                                                                 Annotation[] annotations,
                                                                 Retrofit retrofit) {
        if (type == Bitmap.class) {
            return new Converter<ResponseBody, Bitmap>(){
                @Override
                public Bitmap convert(ResponseBody value) throws IOException {
                    return BitmapFactory.decodeStream(value.byteStream());
                }
            };
        } else {
            return null;
        }
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        return null;
    }
}
