package com.zhangyangjing.imageresresolution.widget;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.zhangyangjing.imageresresolution.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentPreview extends Fragment {
    @BindView(R.id.rg_scale_rate) RadioGroup mRgScaleRate;
    @BindView(R.id.rg_scale_type) RadioGroup mRgScaleType;
    @BindView(R.id.rg_scale_noise) RadioGroup mRgScaleNoise;
    @BindView(R.id.iv_preview) ImageView mIvPreview;

    private static final String ARG_IMAGE_URI = "image_uri";

    private Uri mUri;

    public static FragmentPreview instance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_IMAGE_URI, uri.toString());
        FragmentPreview fragment = new FragmentPreview();
        fragment.setArguments(bundle);
        return fragment;
    }

    public FragmentPreview() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        String uriStr = getArguments().getString(ARG_IMAGE_URI);
        mUri = Uri.parse(uriStr);
        mIvPreview.setImageURI(mUri);
    }

    @OnClick(R.id.btn_start_process)
    void onClick(View view) {
        String type = getType();
        String rate = getRate();
        String noise = getNoise();

        Fragment fragment = FragmentProcess.instance(mUri, type, rate, noise);
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

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.frg_docker, fragment)
                .addToBackStack("preview")
                .addSharedElement(mIvPreview, ViewCompat.getTransitionName(mIvPreview))
                .commitAllowingStateLoss();
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
}
