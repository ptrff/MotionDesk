package ru.ptrff.motiondesk.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.Transformation;

import ru.ptrff.motiondesk.R;

public class ShimmerView extends View {
    private static final int DEFAULT_ANIMATION_DURATION = 1000;
    private static final int DEFAULT_RADIUS = 30;
    private static final int DEFAULT_COLOR = Color.WHITE;
    private static final int DEFAULT_BACKGROUND_COLOR = Color.BLACK;

    private final Paint shimmerPaint;
    private final Paint backgroundPaint;
    private int shimmerColor;
    private int backgroundColor;
    private int shimmerAnimationDuration;
    private int radius;
    private int maxTranslateYAnimHeight;
    private int shimmerTranslateX;
    private int shimmerTranslateY;
    private final ShimmerAnimation shimmerAnimation;
    private static long globalTimer;
    private boolean isCubic = false;

    public ShimmerView(Context context) {
        this(context, null);
    }

    public ShimmerView(Context context, boolean isCubic) {
        this(context, null);
        this.isCubic=isCubic;
    }

    public ShimmerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShimmerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        shimmerPaint = new Paint();
        backgroundPaint = new Paint();
        shimmerAnimationDuration = DEFAULT_ANIMATION_DURATION;
        radius = DEFAULT_RADIUS;
        shimmerColor = DEFAULT_COLOR;
        backgroundColor = DEFAULT_BACKGROUND_COLOR;
        shimmerAnimation = new ShimmerAnimation();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ShimmerView);
        shimmerColor = a.getColor(R.styleable.ShimmerView_shimmerColor, Color.BLACK);
        backgroundColor = a.getColor(R.styleable.ShimmerView_shimmerColorBackground, Color.BLACK);
        shimmerAnimationDuration = a.getInt(R.styleable.ShimmerView_shimmerAnimationDuration, DEFAULT_ANIMATION_DURATION);
        radius = a.getDimensionPixelSize(R.styleable.ShimmerView_shimmerRadius, DEFAULT_RADIUS);
        a.recycle();
        globalTimer = SystemClock.elapsedRealtime();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredHeight;
        if(isCubic) desiredHeight = widthMeasureSpec;
        else desiredHeight = widthMeasureSpec*3/2;

        int newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(desiredHeight, MeasureSpec.EXACTLY);

        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        setLayerType(LAYER_TYPE_HARDWARE, backgroundPaint);
        setLayerType(LAYER_TYPE_HARDWARE, shimmerPaint);

        shimmerPaint.setColor(shimmerColor);
        backgroundPaint.setColor(backgroundColor);

        maxTranslateYAnimHeight = (int) ((getWidth()*0.8-radius*10)*0.5);

        canvas.drawRect(0, 0, getWidth(), getHeight(), backgroundPaint);
        canvas.drawCircle(shimmerTranslateX+ radius *7, getHeight()/2+shimmerTranslateY-maxTranslateYAnimHeight, radius, shimmerPaint);
        canvas.drawCircle(getWidth()-shimmerTranslateX- radius *7, getHeight()/2-shimmerTranslateY+maxTranslateYAnimHeight, radius, shimmerPaint);


        updateAnimationState();
    }

    private boolean back=false;
    private void updateAnimationState() {
        long elapsedTime = SystemClock.elapsedRealtime() - globalTimer;
        float fraction = (float) elapsedTime / shimmerAnimationDuration;
        float val = fraction % 0.5f * 2;
        if(!back){
            if(val==1) back=true;
        }else{
            val = val % 1.0f;
            if(val==0) back=false;
        }
        AnticipateOvershootInterpolator interpolator = new AnticipateOvershootInterpolator();
        float smoothedVariable = interpolator.getInterpolation(val);
        if (fraction % 1.0f < 0.5f) {
            shimmerTranslateX = (int) ((getWidth()- radius *14) * smoothedVariable);
        } else {
            shimmerTranslateX = (int) ((getWidth()- radius *14) * (1 -smoothedVariable));
        }
        shimmerTranslateY = (int) Math.abs((getWidth()*0.8- radius *10)*0.5-((getWidth()*0.8- radius *10) * smoothedVariable));
    }

    public void setShimmerColor(int shimmerColor) {
        this.shimmerColor = shimmerColor;
    }

    public void setShimmerAnimationDuration(int shimmerAnimationDuration) {
        this.shimmerAnimationDuration = shimmerAnimationDuration;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void startShimmerAnimation() {
        shimmerAnimation.setDuration(shimmerAnimationDuration);
        shimmerAnimation.setRepeatCount(Animation.INFINITE);
        startAnimation(shimmerAnimation);
    }

    public void stopShimmerAnimation() {
        clearAnimation();
        invalidate();
    }

    private class ShimmerAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            invalidate();
        }
    }
}