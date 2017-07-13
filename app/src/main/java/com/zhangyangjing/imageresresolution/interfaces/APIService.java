package com.zhangyangjing.imageresresolution.interfaces;

import android.graphics.Bitmap;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by zhangyangjing on 13/07/2017.
 */

public interface APIService {
    @POST("api")
    @Multipart
    Call<Bitmap> processPng(@Part("style") RequestBody type,
                            @Part("noise") RequestBody noise,
                            @Part("scale") RequestBody scale,
                            @Part("file\"; filename=\"test.png") RequestBody img);
}
