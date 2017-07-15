package com.zhangyangjing.imageresresolution;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.zhangyangjing.imageresresolution.widget.FragmentSelect;

public class MainActivity extends AppCompatActivity implements IActivity {
    private Bitmap mImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.frg_docker, FragmentSelect.instance())
                .commitAllowingStateLoss();
    }

    @Override
    public void setImage(Bitmap image) {
        mImage = image;
    }

    @Override
    public Bitmap getImage() {
        return mImage;
    }
}
