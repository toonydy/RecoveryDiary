package com.example.yeodayeong.recoverydiary;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class SwipeGesture extends GestureDetector.SimpleOnGestureListener {

         final int swipeMinDistance;
         final int swipeThresholdVelocity;

        public SwipeGesture(Context context) {
            final ViewConfiguration viewConfig = ViewConfiguration.get(context);
            swipeMinDistance = viewConfig.getScaledTouchSlop();
            swipeThresholdVelocity = viewConfig.getScaledMinimumFlingVelocity();
        }

/*        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (e1.getX() - e2.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                onNextMonth();
            } else if (e2.getX() - e1.getX() > swipeMinDistance && Math.abs(velocityX) > swipeThresholdVelocity) {
                onPreviousMonth();
            }
            return false;
        }*/


}
