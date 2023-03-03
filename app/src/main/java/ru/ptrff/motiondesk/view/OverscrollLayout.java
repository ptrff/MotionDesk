package ru.ptrff.motiondesk.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;

import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ru.ptrff.motiondesk.R;

public class OverscrollLayout extends RelativeLayout {

    private int animDuration;
    private int overScrollSize;
    private int overScrollStateChangeSize;
    private float damping;
    private float indicatorDamping;
    private RecyclerView mChildView;
    private ImageView overScrollIcon;
    private final Rect originalRect = new Rect();
    private float startX;
    private float startY;
    private int scrollX;
    private boolean isMoved;
    private boolean intercept;
    private boolean canOverScroll;
    private OnOverScrollReleaseListener mOnOverScrollReleaseListener;
    private final NestedScrollingChildHelper mNestedScrollingChildHelper;
    boolean vibrated = false;


    public OverscrollLayout(Context context) {
        this(context, null);
    }

    public OverscrollLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OverscrollLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.OverscrollLayout);
        canOverScroll = ta.getBoolean(R.styleable.OverscrollLayout_canOverScroll, true);
        animDuration = ta.getInteger(R.styleable.OverscrollLayout_animDuration, 400);
        overScrollSize = ta.getInteger(R.styleable.OverscrollLayout_overScrollSize, 120);
        overScrollStateChangeSize = ta.getInt(R.styleable.OverscrollLayout_overScrollStateChangeSize, 96);
        damping = ta.getFloat(R.styleable.OverscrollLayout_damping, .3f);
        indicatorDamping = ta.getFloat(R.styleable.OverscrollLayout_indicatorDamping, .2f);
        ta.recycle();

        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        overScrollIcon = new ImageView(getContext());
        overScrollIcon.setBackgroundResource(R.drawable.ic_more_vert);
        addView(overScrollIcon);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildView == null) {
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof RecyclerView) {
                    mChildView = (RecyclerView) getChildAt(i);
                }
            }
        }
        mChildView.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.AT_MOST));

        overScrollIcon.measure(
                MeasureSpec.makeMeasureSpec(overScrollSize, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.AT_MOST));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = mChildView.getMeasuredWidth();
        int height = mChildView.getMeasuredHeight();
        mChildView.layout(0, 0, width, height);

        int textTop = (int) (height / 2f - overScrollIcon.getMeasuredHeight() / 2f);
        int textBottom = (int) (height / 2f + overScrollIcon.getMeasuredHeight() / 2f);
        overScrollIcon.layout(r, textTop, r + overScrollIcon.getMeasuredWidth(), textBottom);

        originalRect.set(l, t, t + width, t + height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
                startY = ev.getY();
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                float nowX = ev.getX();
                float nowY = ev.getY();
                float dx = nowX - startX;
                float dy = nowY - startY;

                if (Math.abs(dx) > ViewConfiguration.get(getContext()).getScaledTouchSlop() && Math.abs(dx) > Math.abs(dy)) {
                    isMoved = true;
                    return false;
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = ev.getX();
            case MotionEvent.ACTION_MOVE:
                float nowX = ev.getX();
                scrollX = (int) (nowX - startX);
                if (isCanPullLeft() && scrollX < 0) {
                    int absScrollX = Math.abs((int) ((nowX - startX) * damping));
                    int textScrollX = Math.abs((int) ((nowX - startX) * indicatorDamping));
                    mChildView.setTranslationX(-absScrollX);
                    if (absScrollX < overScrollSize) {
                        requestDisallowInterceptTouchEvent(true);
                        if (absScrollX >= overScrollStateChangeSize) {
                            overScrollIcon.setBackgroundResource(R.drawable.ic_arrow_forward);
                            if (!vibrated) {
                                Vibrator v = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
                                } else {
                                    v.vibrate(100);
                                }
                                vibrated = true;
                            }
                        } else {
                            vibrated = false;
                            overScrollIcon.setBackgroundResource(R.drawable.ic_more_vert);
                        }
                        overScrollIcon.setTranslationX(-textScrollX);
                    }
                    isMoved = true;
                    intercept = false;
                    return true;
                } else {
                    startX = ev.getX();
                    isMoved = false;
                    intercept = true;
                    recoverLayout();
                    return super.dispatchTouchEvent(ev);
                }
            case MotionEvent.ACTION_UP:
                if (isMoved) {
                    recoverLayout();
                }
                if (intercept) {
                    return super.dispatchTouchEvent(ev);
                } else {
                    return true;
                }
            default:
                return super.dispatchTouchEvent(ev);
        }
    }


    private void recoverLayout() {
        if (!isMoved) {
            return;
        }

        mChildView.animate()
                .setDuration(animDuration)
                .translationX(-mChildView.getLeft());

        overScrollIcon.animate()
                .setDuration((long) (animDuration * (damping / indicatorDamping)))
                .translationX(-scrollX * indicatorDamping);

        if (overScrollSize >= overScrollStateChangeSize) {
            if (mOnOverScrollReleaseListener != null && vibrated) {
                mOnOverScrollReleaseListener.onRelease();
            }
        }
    }

    private boolean isCanPullLeft() {
        if (!canOverScroll) {
            return false;
        }

        final RecyclerView.Adapter adapter = mChildView.getAdapter();
        if (adapter == null) {
            return true;
        }
        final int lastItemPosition = adapter.getItemCount() - 1;
        final int lastVisiblePosition = ((LinearLayoutManager) mChildView.getLayoutManager()).findLastVisibleItemPosition();

        if (lastVisiblePosition >= lastItemPosition) {
            final int childIndex = lastVisiblePosition - ((LinearLayoutManager) mChildView.getLayoutManager()).findFirstVisibleItemPosition();
            final int childCount = mChildView.getChildCount();
            final int index = Math.min(childIndex, childCount - 1);
            final View lastVisibleChild = mChildView.getChildAt(index);
            if (lastVisibleChild != null) {
                return lastVisibleChild.getRight() + ((MarginLayoutParams) lastVisibleChild.getLayoutParams()).rightMargin
                        <= mChildView.getRight() - mChildView.getLeft();
            }
        }

        return false;
    }

    public void enableOverScroll() {
        canOverScroll = true;
    }

    public void disableOverScroll() {
        canOverScroll = false;
    }

    public int getAnimDuration() {
        return animDuration;
    }

    public void setAnimDuration(int animDuration) {
        this.animDuration = animDuration;
    }

    public int getOverScrollSize() {
        return overScrollSize;
    }

    public void setOverScrollSize(int overScrollSize) {
        this.overScrollSize = overScrollSize;
    }

    public int getOverScrollStateChangeSize() {
        return overScrollStateChangeSize;
    }

    public void setOverScrollStateChangeSize(int overScrollStateChangeSize) {
        this.overScrollStateChangeSize = overScrollStateChangeSize;
    }

    public float getDamping() {
        return damping;
    }

    public void setDamping(float damping) {
        this.damping = damping;
    }

    public float getTextDamping() {
        return indicatorDamping;
    }

    public void setTextDamping(float textDamping) {
        this.indicatorDamping = textDamping;
    }

    public void setOnOverScrollReleaseListener(OnOverScrollReleaseListener onOverScrollReleaseListener) {
        mOnOverScrollReleaseListener = onOverScrollReleaseListener;
    }

    public interface OnOverScrollReleaseListener {
        void onRelease();
    }
}
