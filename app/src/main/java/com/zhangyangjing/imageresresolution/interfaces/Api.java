package com.zhangyangjing.imageresresolution.interfaces;

import com.zhangyangjing.imageresresolution.interfaces.converter.BitmapConverterFactory;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Created by zhangyangjing on 13/07/2017.
 */

public class Api {
    public static final String ENDPOINT_URL = "http://waifu2x.udp.jp/";

    private static Api sInstance;
    private APIService mService;

    public static synchronized Api get() {
        if (sInstance == null)
            sInstance= new Api();
        return sInstance;
    }

    public synchronized APIService getApiService() {
        if (null == mService) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ENDPOINT_URL)
                    .client(client)
                    .addConverterFactory(BitmapConverterFactory.create())
                    .build();
            mService = retrofit.create(APIService.class);
        }

        return mService;
    }
}
