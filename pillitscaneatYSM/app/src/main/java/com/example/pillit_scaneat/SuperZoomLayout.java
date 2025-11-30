package com.example.pillit_scaneat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.FrameLayout;

public class SuperZoomLayout extends FrameLayout {

    private float scaleFactor = 1.0f;
    private final float MIN_SCALE = 1.0f;
    private final float MAX_SCALE = 3.0f;

    private ScaleGestureDetector scaleDetector;

    private float lastX, lastY;
    private float posX = 0f, posY = 0f;

    public SuperZoomLayout(Context context) {
        super(context);
        init(context);
    }

    public SuperZoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {

        setClickable(true);
        setFocusable(true);

        // 🔥 핀치 감지기
        scaleDetector = new ScaleGestureDetector(context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {

                        scaleFactor *= detector.getScaleFactor();
                        scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

                        if (getChildCount() > 0) {
                            getChildAt(0).setScaleX(scaleFactor);
                            getChildAt(0).setScaleY(scaleFactor);
                        }

                        clampTranslation();
                        return true;
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        // 확대 시작 시 true
                        return true;
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // 🔥 두 손가락이면 SuperZoomLayout이 우선 처리 → 핀치 가능
        if (ev.getPointerCount() > 1) {
            return true;
        }
        return false; // 기본은 내부 ScrollView가 처리
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 🔥 핀치 처리
        scaleDetector.onTouchEvent(event);

        // 🔥 핀치가 아닐 때 → 드래그 이동 처리
        if (!scaleDetector.isInProgress()) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastX = event.getX() - posX;
                    lastY = event.getY() - posY;
                    break;

                case MotionEvent.ACTION_MOVE:
                    posX = event.getX() - lastX;
                    posY = event.getY() - lastY;
                    clampTranslation();
                    break;

                case MotionEvent.ACTION_UP:
                    break;
            }
        }

        return true;
    }

    private void clampTranslation() {

        if (getChildCount() == 0) return;

        float viewWidth = getWidth();
        float viewHeight = getHeight();

        float contentWidth = getChildAt(0).getWidth() * scaleFactor;
        float contentHeight = getChildAt(0).getHeight() * scaleFactor;

        float maxX = Math.max(0, (contentWidth - viewWidth) / 2);
        float maxY = Math.max(0, (contentHeight - viewHeight) / 2);

        posX = Math.min(maxX, Math.max(-maxX, posX));
        posY = Math.min(maxY, Math.max(-maxY, posY));

        getChildAt(0).setTranslationX(posX);
        getChildAt(0).setTranslationY(posY);
    }
}
