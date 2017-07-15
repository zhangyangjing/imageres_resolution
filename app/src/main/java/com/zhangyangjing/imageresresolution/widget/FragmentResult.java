package com.zhangyangjing.imageresresolution.widget;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.zhangyangjing.imageresresolution.IActivity;
import com.zhangyangjing.imageresresolution.R;
import com.zhangyangjing.imageresresolution.util.SaveImgeHelper;

import java.io.FileNotFoundException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FragmentResult extends Fragment implements View.OnKeyListener {
    private static final String ARG_IMAGE_URI = "image_uri";

    private Bitmap mImage;

    @BindView(R.id.btn_save) View mBtnSave;
    @BindView(R.id.iv_src) ImageView mIvSrc;
    @BindView(R.id.iv_dst) ImageView mIvDst;

    public static FragmentResult instance(Uri uri) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_IMAGE_URI, uri.toString());
        FragmentResult fragment = new FragmentResult();
        fragment.setArguments(bundle);
        return fragment;
    }

    public FragmentResult() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_result, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mBtnSave.setOnKeyListener(this);

        String uriStr = getArguments().getString(ARG_IMAGE_URI);
        Uri uri = Uri.parse(uriStr);

        IActivity iActivity = (IActivity) getActivity();
        mImage = iActivity.getImage();
        iActivity.setImage(null);

        mIvSrc.setImageURI(uri);
        mIvDst.setImageBitmap(mImage);
    }

    @OnClick(R.id.btn_save)
    void onClick(View view) {
        try {
            SaveImgeHelper.saveImage(getActivity(), mImage);
            Toast.makeText(getActivity(), "图片已保存", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (KeyEvent.KEYCODE_BACK == keyCode && KeyEvent.ACTION_DOWN == event.getAction()) {
            getFragmentManager().popBackStack("preview", 0);
            return true;
        }
        return false;
    }
}
