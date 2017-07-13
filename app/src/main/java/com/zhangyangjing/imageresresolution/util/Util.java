package com.zhangyangjing.imageresresolution.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by zhangyangjing on 13/07/2017.
 */

public class Util {

    public static File getScaleImageDir() {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File scaleImagePath = new File(path, "ImageScale");
        return scaleImagePath;
    }
}
