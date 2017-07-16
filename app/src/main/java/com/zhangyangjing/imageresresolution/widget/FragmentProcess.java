package com.zhangyangjing.imageresresolution.widget;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.io.ByteStreams;
import com.zhangyangjing.imageresresolution.IActivity;
import com.zhangyangjing.imageresresolution.R;
import com.zhangyangjing.imageresresolution.interfaces.APIService;
import com.zhangyangjing.imageresresolution.interfaces.Api;

import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentProcess extends Fragment implements Callback<Bitmap>, View.OnKeyListener {
    private static final String ARG_IMAGE_URI = "image_uri";
    private static final String ARG_SCALE_TYPE = "scale_type";
    private static final String ARG_SCALE_RATE = "scale_rate";
    private static final String ARG_SCALE_NOISE = "scale_noise";

    @BindView(R.id.iv_process) ImageView mIvProcess;

    private Uri mUri;
    private Call mCall;

    public static FragmentProcess instance(Uri uri, String type, String rate, String noise) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_IMAGE_URI, uri.toString());
        bundle.putString(ARG_SCALE_TYPE, type);
        bundle.putString(ARG_SCALE_RATE, rate);
        bundle.putString(ARG_SCALE_NOISE, noise);
        FragmentProcess fragment = new FragmentProcess();
        fragment.setArguments(bundle);
        return fragment;
    }

    public FragmentProcess() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_process, null);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String uriStr = getArguments().getString(ARG_IMAGE_URI);
        String type = getArguments().getString(ARG_SCALE_TYPE);
        String rate = getArguments().getString(ARG_SCALE_RATE);
        String noise = getArguments().getString(ARG_SCALE_NOISE);

        mUri = Uri.parse(uriStr);
        mIvProcess.setImageURI(mUri);

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(this);

        byte[] data;
        try {
            InputStream is = getActivity().getContentResolver().openInputStream(mUri);
            data = ByteStreams.toByteArray(is);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        RequestBody bodyType = RequestBody.create(MediaType.parse("text/plain"), type);
        RequestBody bodyRate = RequestBody.create(MediaType.parse("text/plain"), rate);
        RequestBody bodyNoise = RequestBody.create(MediaType.parse("text/plain"), noise);
        RequestBody bodyImage = RequestBody.create(MediaType.parse("image/png"), data);

        APIService api = Api.get().getApiService();
        mCall = api.processPng(bodyType, bodyRate, bodyNoise, bodyImage);
        mCall.enqueue(this);
//
//
//        getView().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                fake();
//            }
//        }, 1000);
    }
//
//    private void fake() {
//        InputStream is = null;
//        try {
//            is = getContext().getContentResolver().openInputStream(mUri);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        Bitmap image = BitmapFactory.decodeStream(is);
//        IActivity activity = (IActivity) getActivity();
//        activity.setImage(image);
//
//        Fragment fragment = FragmentResult.instance(mUri);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//            int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);
//
//            Slide slideTransition = new Slide(Gravity.LEFT);
//            slideTransition.setDuration(duration);
//            setExitTransition(slideTransition);
//
//            Slide slideTransition2 = new Slide(Gravity.RIGHT);
//            slideTransition2.setDuration(duration);
//            fragment.setEnterTransition(slideTransition2);
//
//            TransitionInflater inflater = TransitionInflater.from(getContext());
//            Transition transition = inflater.inflateTransition(R.transition.transition_default);
//            fragment.setSharedElementEnterTransition(transition);
//        }
//
//        getFragmentManager()
//                .beginTransaction()
//                .replace(R.id.frg_docker, fragment)
//                .addSharedElement(mIvProcess, ViewCompat.getTransitionName(mIvProcess))
//                .commitAllowingStateLoss();
//    }

    @Override
    public void onResponse(Call<Bitmap> call, Response<Bitmap> response) {
        Bitmap image = response.body();
        if (null == image) {
            Toast.makeText(getActivity(), "缩放图片失败", Toast.LENGTH_LONG).show();
            getFragmentManager().popBackStack();
            return;
        }

        Fragment fragment = FragmentResult.instance(mUri);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int duration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

            Slide slideTransition = new Slide(Gravity.LEFT);
            slideTransition.setDuration(duration);
            setExitTransition(slideTransition);

            Slide slideTransition2 = new Slide(Gravity.RIGHT);
            slideTransition2.setDuration(duration);
            fragment.setEnterTransition(slideTransition2);

            TransitionInflater inflater = TransitionInflater.from(getContext());
            Transition transition = inflater.inflateTransition(R.transition.transition_default);
            fragment.setSharedElementEnterTransition(transition);
        }

        IActivity activity = (IActivity) getActivity();
        activity.setImage(image);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frg_docker, fragment)
                .addSharedElement(mIvProcess, ViewCompat.getTransitionName(mIvProcess))
                .commitAllowingStateLoss();
    }

    @Override
    public void onFailure(Call<Bitmap> call, Throwable t) {
        Toast.makeText(getActivity(), "缩放图片失败", Toast.LENGTH_LONG).show();
        getFragmentManager().popBackStack();
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
            mCall.cancel();
            Toast.makeText(getActivity(), "取消处理", Toast.LENGTH_LONG).show();
        }
        return false;
    }
}
