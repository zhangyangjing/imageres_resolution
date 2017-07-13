package com.zhangyangjing.imageresresolution.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.zhangyangjing.imageresresolution.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zhangyangjing on 13/07/2017.
 */

public class SaveImgeHelper {
    private static final int NOTIFICATION_ID = 1234;
    private static final String KEY_IMAGE_URI = "image_uri_id";

    public static Uri saveImage(Context context, Bitmap image) throws FileNotFoundException {
        if (null == image)
            return null;

        File scaleImageDir = Util.getScaleImageDir();
        if (false == scaleImageDir.exists() && false == scaleImageDir.mkdirs())
            return null;

        long imageTime = System.currentTimeMillis();
        long dateSeconds = imageTime / 1000;

        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date(imageTime));
        String imageFileName = String.format("image_scale_%s.png", imageDate);
        File file = new File(scaleImageDir, imageFileName);
        image.compress(Bitmap.CompressFormat.PNG, 80, new FileOutputStream(file));

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.ImageColumns.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.ImageColumns.TITLE, imageFileName);
        values.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, imageTime);
        values.put(MediaStore.Images.ImageColumns.DATE_ADDED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.DATE_MODIFIED, dateSeconds);
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.ImageColumns.WIDTH, image.getWidth());
        values.put(MediaStore.Images.ImageColumns.HEIGHT, image.getHeight());
        values.put(MediaStore.Images.ImageColumns.SIZE, file.length());
        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        Notification notification = makeNotification(context, image, uri, imageTime);
        notificationManager.notify(NOTIFICATION_ID, notification);

        return uri;
    }

    private static Notification makeNotification(Context context, Bitmap picture,
                                                 Uri uri, long imageTime) {
        Intent launchIntent = new Intent(Intent.ACTION_VIEW);
        launchIntent.setDataAndType(uri, "image/png");
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Notification publicNotification = new NotificationCompat.Builder(context)
                .setContentTitle("已保存缩放图片")
                .setContentText("点击查看放大后的图片")
                .setContentIntent(PendingIntent.getActivity(context, 0, launchIntent, 0))
                .setSmallIcon(R.drawable.ic_insert_photo_black_24dp)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setColor(context.getResources().getColor(R.color.notification_accent_color))
                .build();

        NotificationCompat.Builder mNotificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("已保存缩放图片")
                .setContentText("点击查看放大后的图片")
                .setContentIntent(PendingIntent.getActivity(context, 0, launchIntent, 0))
                .setSmallIcon(R.drawable.ic_insert_photo_black_24dp)
                .setLargeIcon(picture)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setColor(context.getResources().getColor(R.color.notification_accent_color))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(picture))
                .setAutoCancel(true)
                .setPublicVersion(publicNotification);

        String subjectDate = DateFormat.getDateTimeInstance().format(new Date(imageTime));
        String subject = String.format("图片_%s", subjectDate);
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        PendingIntent chooseAction = PendingIntent.getBroadcast(context, 0,
                new Intent(context, TargetChosenReceiver.class),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        Intent chooserIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
            chooserIntent = Intent.createChooser(sharingIntent, null, chooseAction.getIntentSender());
        else
            chooserIntent = Intent.createChooser(sharingIntent, null);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent shareAction = PendingIntent.getActivity(context, 0, chooserIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Action.Builder shareActionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_share_black_32dp, "分享", shareAction);
        mNotificationBuilder.addAction(shareActionBuilder.build());

        PendingIntent deleteAction = PendingIntent.getBroadcast(context, 0,
                new Intent(context, DeleteShotReceiver.class)
                        .putExtra(KEY_IMAGE_URI, uri.toString()),
                PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Action.Builder deleteActionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_delete_black_24dp, "删除", deleteAction);
        mNotificationBuilder.addAction(deleteActionBuilder.build());

        return mNotificationBuilder.build();
    }

    public static class TargetChosenReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(NOTIFICATION_ID);
        }
    }

    public static class DeleteShotReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            if (!intent.hasExtra(KEY_IMAGE_URI)) {
                return;
            }

            NotificationManager nm =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            Uri uri = Uri.parse(intent.getStringExtra(KEY_IMAGE_URI));
            nm.cancel(NOTIFICATION_ID);

            Observable.just(uri)
                    .observeOn(Schedulers.io())
                    .subscribe(new Action1<Uri>() {
                        @Override
                        public void call(Uri uri) {
                            context.getContentResolver().delete(uri, null, null);
                        }
                    });
        }
    }
}
