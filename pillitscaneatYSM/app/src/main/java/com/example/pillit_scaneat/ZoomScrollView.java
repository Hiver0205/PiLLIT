package com.example.pillit_scaneat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class ZoomScrollView extends ScrollView {

    public ZoomScrollView(Context context) {
        super(context);
    }

    public ZoomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        // 🔥 두 손가락 사용(핀치)일 경우
        // ScrollView가 이벤트를 가로채지 않고 → SuperZoomLayout에게 넘김
        if (ev.getPointerCount() > 1) {
            return false;
        }

        return super.onInterceptTouchEvent(ev);
    }
}
