package com.zhangyangjing.imageresresolution;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.io.Files;
import com.zhangyangjing.imageresresolution.interfaces.APIService;
import com.zhangyangjing.imageresresolution.interfaces.Api;
import com.zhangyangjing.imageresresolution.util.SaveImgeHelper;
import com.zhangyangjing.imageresresolution.util.Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<Bitmap> {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;

    @BindView(R.id.rg_scale_rate) RadioGroup mRgScaleRate;
    @BindView(R.id.rg_scale_type) RadioGroup mRgScaleType;
    @BindView(R.id.rg_scale_noise) RadioGroup mRgScaleNoise;
    @BindView(R.id.iv_image_before) ImageView mIvBefore;
    @BindView(R.id.iv_image_after) ImageView mIvAfter;
    @BindView(R.id.tv_before) TextView mTvBefore;
    @BindView(R.id.tv_after) TextView mTvAfter;
    @BindView(R.id.btn_select_image) TextView mBtnSelectImage;
    @BindView(R.id.btn_start_process) Button mBtnProcess;
    @BindView(R.id.btn_save_image) TextView mBtnSaveImage;
    @BindView(R.id.pg_process) ProgressBar mPgProcess;

    private String mPath;
    private State mState;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        switchState(State.NORMAL);
    }

    @OnClick({R.id.btn_select_image, R.id.btn_start_process, R.id.btn_save_image})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_select_image:
                selectImage();
                break;
            case R.id.btn_start_process:
                byte[] data;
                try {
                    data = Files.toByteArray(new File(mPath));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                RequestBody type = RequestBody.create(MediaType.parse("text/plain"), getType());
                RequestBody noise = RequestBody.create(MediaType.parse("text/plain"), getNoise());
                RequestBody scale = RequestBody.create(MediaType.parse("text/plain"), getRate());
                RequestBody image = RequestBody.create(MediaType.parse("image/png"), data);
                APIService api = Api.get().getApiService();
                api.processPng(type, noise, scale, image).enqueue(this);
                switchState(State.PROCESSING);
                break;
            case R.id.btn_save_image:
                try {
                    SaveImgeHelper.saveImage(this, mBitmap);
                    Toast.makeText(this, "图片已保存", Toast.LENGTH_SHORT).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            if (false == verifyImage(uri)) {
                Toast.makeText(this, "图片太大", Toast.LENGTH_LONG).show();
                return;
            }

            switchState(State.SELECTED);
            mPath = Util.getPath(this, uri);
            mIvBefore.setImageURI(uri);
        }
    }

    @Override
    public void onResponse(Call<Bitmap> call, Response<Bitmap> response) {
        Log.d(TAG, "onResponse() called with: call = [" + call + "], response = [" + response + "]");

        mBitmap = response.body();
        if (null == mBitmap) {
            Toast.makeText(this, "缩放图片失败", Toast.LENGTH_LONG).show();
            return;
        }

        mIvAfter.setImageBitmap(mBitmap);
        switchState(State.PROCESS_FINISHED);
    }

    @Override
    public void onFailure(Call<Bitmap> call, Throwable t) {
        Toast.makeText(this, "缩放图片失败", Toast.LENGTH_LONG).show();
        switchState(State.SELECTED);
    }

    private void switchState(State state) {
        if (mState == state)
            return;

        mState = state;
        switch (state) {
            case NORMAL:
                mBitmap = null;
                mIvBefore.setImageResource(0);
                mIvAfter.setImageResource(0);
                mBtnSelectImage.setEnabled(true);
                mBtnProcess.setEnabled(false);
                mBtnSaveImage.setEnabled(false);
                mTvBefore.setVisibility(View.VISIBLE);
                mTvAfter.setVisibility(View.VISIBLE);
                mPgProcess.setVisibility(View.GONE);
                mBtnProcess.setText("开始放大图片");
                break;
            case SELECTED:
                mIvAfter.setImageResource(0);
                mBtnSelectImage.setEnabled(true);
                mBtnProcess.setEnabled(true);
                mBtnSaveImage.setEnabled(false);
                mTvBefore.setVisibility(View.GONE);
                mTvAfter.setVisibility(View.VISIBLE);
                mPgProcess.setVisibility(View.GONE);
                mBtnProcess.setText("开始放大图片");
                break;
            case PROCESSING:
                mIvAfter.setImageResource(0);
                mBtnSelectImage.setEnabled(false);
                mBtnProcess.setEnabled(false);
                mBtnSaveImage.setEnabled(false);
                mTvBefore.setVisibility(View.GONE);
                mTvAfter.setVisibility(View.GONE);
                mPgProcess.setVisibility(View.VISIBLE);
                mBtnProcess.setText("正在放大图片...");
                break;
            case PROCESS_FINISHED:
                mBtnSelectImage.setEnabled(true);
                mBtnProcess.setEnabled(true);
                mBtnSaveImage.setEnabled(true);
                mTvBefore.setVisibility(View.GONE);
                mTvAfter.setVisibility(View.GONE);
                mPgProcess.setVisibility(View.GONE);
                mBtnProcess.setText("开始放大图片");
                break;
        }
    }

    private String getType() {
        switch (mRgScaleType.getCheckedRadioButtonId()) {
            case R.id.rb_type_photo:
                return "photo";
            default:
            case R.id.rb_type_art:
                return "art";
        }
    }

    private String getRate() {
        switch (mRgScaleRate.getCheckedRadioButtonId()) {
            case R.id.rb_resize_normal:
                return "-1";
            default:
            case R.id.rb_resize_1x:
                return "1";
            case R.id.rb_resize_2x:
                return "2";
        }
    }

    private String getNoise() {
        switch (mRgScaleNoise.getCheckedRadioButtonId()) {
            case R.id.rb_noise_normal:
                return "-1";
            case R.id.rb_noise_low:
                return "0";
            case R.id.rb_noise_medium:
                return "1";
            default:
            case R.id.rb_noise_high:
                return "2";
            case R.id.rb_noise_extreme:
                return "3";
        }
    }

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private boolean verifyImage(Uri uri) {
        String path = Util.getPath(this, uri);
        if (null == path)
            return false;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        Log.v(TAG, "path:" + path + " size w:" + options.outWidth + " h:" + options.outHeight);
        if (options.outWidth > 1500 || options.outHeight > 1500)
            return false;

        return true;
    }

    private enum State {
        NORMAL, SELECTED, PROCESSING, PROCESS_FINISHED
    }
}
