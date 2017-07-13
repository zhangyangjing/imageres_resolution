package com.zhangyangjing.imageresresolution.util;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

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

    /**
     * 根据返回的Uri得到文件的路径，从而得到需要获取的资源
     */
    public static String getPath(final Context context, final Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            return getPathKitkat(context, uri);
        else
            return getPathIceCream(context, uri);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getPathKitkat(Context context, Uri uri) {
        if (isExternalStorageDocument(uri)) {//判断是否是手机SD卡根目录下的Document
            final String docId = DocumentsContract.getDocumentId(uri);//获取“类型:id”
            final String[] split = docId.split(":");//通过:来分隔类型和ID
            final String type = split[0];

            if ("primary".equalsIgnoreCase(type)) {//判断类型是否是primary(手机SD卡根目录path类型)
                //Environment提供对环境变量的访问。方法getExternalStorageDirectory()是返回手机根目录path
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            }

            // TODO handle non-primary volumes
        }
        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {//判断是否在系统下载path下的文件
            final String id = DocumentsContract.getDocumentId(uri);
            //ContentUris对于Data Scheme采用content://的Uri的有效方法。其中withAppendedId（Uri uri，Long id）方法用于在给定的路径后面追加id
            final Uri contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

            return getDataColumn(context, contentUri, null, null);
        }
        // MediaProvider
        else if (isMediaDocument(uri)) {//判断是否是media类型的文件
            final String docId = DocumentsContract.getDocumentId(uri);
            final String[] split = docId.split(":");
            final String type = split[0];

            Uri contentUri = null;
            if ("image".equals(type)) {//判断是否是image
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;//返回content://样式的Uri
            } else if ("video".equals(type)) {//判断是否是video
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;//返回content://样式的Uri
            } else if ("audio".equals(type)) {//判断是否是audio
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//返回content://样式的Uri
            }

            final String selection = "_id=?";
            final String[] selectionArgs = new String[]{
                    split[1]
            };

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }

        return null;
    }

    private static String getPathIceCream(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }
}
