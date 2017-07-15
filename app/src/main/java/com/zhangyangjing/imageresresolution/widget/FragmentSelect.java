package com.zhangyangjing.imageresresolution.widget;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhangyangjing.imageresresolution.R;

import java.io.FileNotFoundException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class FragmentSelect extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MAX_IAMGE_SIZE = 1500;

    public static FragmentSelect instance() {
        FragmentSelect fragment = new FragmentSelect();
        return fragment;
    }

    public FragmentSelect() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.btn_find)
    void onClick(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PICK_IMAGE_REQUEST == requestCode
                && RESULT_OK == resultCode && null != data && null != data.getData()) {
            Uri uri = data.getData();
//            if (false == verifyImage(uri)) {
//                Toast.makeText(getActivity(), "图片太大", Toast.LENGTH_LONG).show();
//                return;
//            }

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frg_docker, FragmentPreview.instance(uri))
                    .addToBackStack("select")
                    .commitAllowingStateLoss();
        }
    }

    private boolean verifyImage(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = getActivity().getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        if (options.outWidth > MAX_IAMGE_SIZE || options.outHeight > MAX_IAMGE_SIZE)
            return false;

        return true;
    }
}
