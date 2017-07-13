
/*
 * Copyright (C) 2015 Egor Andreevici
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zhangyangjing.imageresresolution;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.OverScroller;

/**
 * An extension to the standard Android {@link ImageView}, which makes it
 * respond to Scroll and Fling events. Uses a {@link GestureDetectorCompat} and
 * a {@link OverScroller} to provide scrolling functionality.
 *
 * @author EgorAnd
 */
public class ScrollableImageView extends ImageView {

    private GestureDetectorCompat gestureDetector;
    private OverScroller overScroller;

    private final int screenW;
    private final int screenH;

    private int positionX = 0;
    private int positionY = 0;

    public ScrollableImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;

        gestureDetector = new GestureDetectorCompat(context, gestureListener);
        overScroller = new OverScroller(context);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        // computeScrollOffset() returns true only when the scrolling isn't
        // already finished
        if (overScroller.computeScrollOffset()) {
            positionX = overScroller.getCurrX();
            positionY = overScroller.getCurrY();
            scrollTo(positionX, positionY);
        } else {
            // when scrolling is over, we will want to "spring back" if the
            // image is overscrolled
            overScroller.springBack(positionX, positionY, 0, getMaxHorizontal(), 0, getMaxVertical());
        }
    }

    private int getMaxHorizontal() {
        if (null == getDrawable())
            return 0;
        return (Math.abs(getDrawable().getBounds().width() - screenW));
    }

    private int getMaxVertical() {
        if (null == getDrawable())
            return 0;
        return (Math.abs(getDrawable().getBounds().height() - screenH));
    }

    private SimpleOnGestureListener gestureListener = new SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            overScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(ScrollableImageView.this);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            overScroller.forceFinished(true);
            overScroller.fling(positionX, positionY, (int) -velocityX, (int) -velocityY, 0, getMaxHorizontal(), 0,
                    getMaxVertical());
            ViewCompat.postInvalidateOnAnimation(ScrollableImageView.this);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            overScroller.forceFinished(true);
            // normalize scrolling distances to not overscroll the image
            int dx = (int) distanceX;
            int dy = (int) distanceY;
            int newPositionX = positionX + dx;
            int newPositionY = positionY + dy;
            if (newPositionX < 0) {
                dx -= newPositionX;
            } else if (newPositionX > getMaxHorizontal()) {
                dx -= (newPositionX - getMaxHorizontal());
            }
            if (newPositionY < 0) {
                dy -= newPositionY;
            } else if (newPositionY > getMaxVertical()) {
                dy -= (newPositionY - getMaxVertical());
            }
            overScroller.startScroll(positionX, positionY, dx, dy, 0);
            ViewCompat.postInvalidateOnAnimation(ScrollableImageView.this);
            return true;
        }
    };

}
