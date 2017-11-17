package com.fei.guideview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 *
 * @author Administrator
 * @date 2017/11/16
 */
public class GuideView extends View {

    public enum TargetStyle {
        TARGETVIEWSTYLE_RECT,
        TARGETVIEWSTYLE_CIRCLE,
        TARGETVIEWSTYLE_OVAL;
    }

    public enum MaskStyle {
        MASKSTYLE_NORMAL, MASKSTYLE_SOLID;
    }

    private int maskColor = 0x99000000;// 蒙版层颜色
    private Bitmap fgBitmap;//背景
    private Bitmap jtUpLeft, jtUpCenter, jtUpRight, jtDownLeft, jtDownCenter, jtDownRight;
    private Bitmap tipViewBitmap;
    private int targetStyle;
    private int maskStyle;
    private int screenWidth, screenHeigt;
    private ArrayList<View> targetViews;
    private View targetView;
    private Rect tipViewHitRect;
    private Paint mPaint;
    private Canvas mCanvas;
    private int margin = 10;
    private boolean touchOutsideCancel = true;
    private OnDissmissListener onDissmissListener;

    public GuideView(Context context) {
        this(context, null);
    }

    public GuideView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public GuideView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs == null) {
            targetStyle = TargetStyle.TARGETVIEWSTYLE_RECT.ordinal();
            maskStyle = MaskStyle.MASKSTYLE_NORMAL.ordinal();
            tipViewBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.tip_view);
            jtDownCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_center);
            jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
            jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
            jtUpCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_center);
            jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
            jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
        } else {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GuideView);
            targetStyle = typedArray.getInt(R.styleable.GuideView_targetStyle, TargetStyle.TARGETVIEWSTYLE_RECT.ordinal());
            maskStyle = typedArray.getInt(R.styleable.GuideView_maskStyle, MaskStyle.MASKSTYLE_NORMAL.ordinal());
            maskColor = typedArray.getColor(R.styleable.GuideView_maskColor, maskColor);
            BitmapDrawable tipViewDrawable = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_tipView);
            if (tipViewDrawable != null) {
                tipViewBitmap = tipViewDrawable.getBitmap();
            }
            BitmapDrawable jtDownCenter = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_down_center);
            BitmapDrawable jtDownLeft = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_down_left);
            BitmapDrawable jtDownRight = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_down_right);
            BitmapDrawable jtUpCenter = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_up_center);
            BitmapDrawable jtUpLeft = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_up_left);
            BitmapDrawable jtUpRight = (BitmapDrawable) typedArray.getDrawable(R.styleable.GuideView_indicator_arrow_up_right);
            if (jtDownCenter != null) {
                this.jtDownCenter = jtDownCenter.getBitmap();
            } else {
                this.jtDownCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_center);
            }
            if (jtDownLeft != null) {
                this.jtDownLeft = jtDownLeft.getBitmap();
            } else {
                this.jtDownLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_left);
            }
            if (jtDownRight != null) {
                this.jtDownRight = jtDownRight.getBitmap();
            } else {
                this.jtDownRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_down_right);
            }
            if (jtUpCenter != null) {
                this.jtUpCenter = jtUpCenter.getBitmap();
            } else {
                this.jtUpCenter = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_center);
            }
            if (jtUpLeft != null) {
                this.jtUpLeft = jtUpLeft.getBitmap();
            } else {
                this.jtUpLeft = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_left);
            }
            if (jtUpRight != null) {
                this.jtUpRight = jtUpRight.getBitmap();
            } else {
                this.jtUpRight = BitmapFactory.decodeResource(getResources(), R.drawable.jt_up_right);
            }
            typedArray.recycle();
        }
        cal(context);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        BlurMaskFilter.Blur blur = null;
        switch (maskStyle) {
            case 0:
                blur = BlurMaskFilter.Blur.NORMAL;
                break;
            case 1:
                blur = BlurMaskFilter.Blur.SOLID;
                break;
        }
        BlurMaskFilter blurMaskFilter = new BlurMaskFilter(15, blur);
        mPaint.setMaskFilter(blurMaskFilter);
        mPaint.setStyle(Paint.Style.FILL);

        mPaint.setARGB(0, 0, 0, 0);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        fgBitmap = Util.createBitmapSafely(screenWidth, screenHeigt, Bitmap.Config.ARGB_8888, 2);
        if (fgBitmap == null) {
            throw new RuntimeException("out of memery cause fgbitmap create fail");
        }

        mCanvas = new Canvas(fgBitmap);
        mCanvas.drawColor(maskColor);
    }

    /**
     * 计算参数
     *
     * @param context
     */
    private void cal(Context context) {
        int[] screenSize = Util.getScreenSize(context);
        screenWidth = screenSize[0];
        screenHeigt = screenSize[1];
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (targetView == null) {
            return;
        }

        canvas.drawBitmap(fgBitmap, 0, 0, null);

        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;
        int radius = 0;//圆半径
        int cx = 0;//圆x轴
        int cy = 0;//圆y轴
        int vWidth = targetView.getWidth();
        int vHeight = targetView.getHeight();
        int tipViewWidth = tipViewBitmap.getWidth();
        int tipViewHeight = tipViewBitmap.getHeight();
        //扩大区域
        left = targetView.getLeft() - margin;
        top = targetView.getTop() - margin;
        right = targetView.getRight() + margin;
        bottom = targetView.getBottom() + margin;

        if (left <= 0) {
            left = margin;
        }
        if (top <= 0) {
            top = margin;
        }
        if (right >= screenWidth) {
            right = screenWidth - margin;
        }
        if (bottom >= screenHeigt) {
            bottom = screenHeigt - margin;
        }

        if (targetStyle == TargetStyle.TARGETVIEWSTYLE_CIRCLE.ordinal()) {
            radius = vWidth <= vHeight ? vHeight / 2 + margin * 2 : vWidth / 2 + margin * 2;
            cx = left + vWidth / 2 + margin;
            cy = top + vHeight / 2 + margin;
            mCanvas.drawCircle(cx, cy, radius, mPaint);
        } else if (targetStyle == TargetStyle.TARGETVIEWSTYLE_OVAL.ordinal()) {
            RectF rectF = new RectF(left, top, right, bottom);
            mCanvas.drawOval(rectF, mPaint);
        } else if (targetStyle == TargetStyle.TARGETVIEWSTYLE_RECT.ordinal()) {
            RectF rect = new RectF(left, top, right, bottom);
            mCanvas.drawRoundRect(rect, 20, 20, mPaint);
        }

        //画箭头
        if (bottom <= screenHeigt / 2 || screenHeigt / 2 - top > bottom - screenHeigt / 2) {
            //top
            int bTop = getJtUpTop(bottom, radius, cy);
            if (right <= screenWidth / 2 || screenWidth / 2 - left > right - screenWidth / 2) {
                //left
                int bLeft = vWidth / 2 + left;
                mCanvas.drawBitmap(jtUpLeft, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft;
                    int tipTop = bTop + jtUpLeft.getHeight();
                    while (tipLeft + tipViewWidth > screenWidth) {
                        tipLeft = tipLeft - 5;
                    }
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, tipLeft + tipViewBitmap.getWidth(), tipTop + tipViewHeight);
                }
            } else if (screenWidth / 2 - left + margin >= vWidth / 2 && right - screenWidth / 2 - margin >= vWidth / 2) {
                //center
                int bLeft = vWidth / 2 + left + margin - jtUpCenter.getWidth() / 2;
                mCanvas.drawBitmap(jtUpCenter, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft - tipViewBitmap.getWidth() / 2 + jtUpCenter.getWidth() / 2;
                    int tipTop = bTop + jtUpCenter.getHeight();
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, tipLeft + tipViewBitmap.getWidth(), tipTop + tipViewHeight);
                }
            } else {
                //right
                int bLeft = 0;
                if (left + margin + vWidth / 2 >= screenWidth / 5 * 3) {
                    bLeft = left - margin;
                } else {
                    bLeft = left + margin;
                }
                mCanvas.drawBitmap(jtUpRight, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft - tipViewBitmap.getWidth() / 2;
                    while (tipLeft + tipViewWidth > screenWidth) {
                        //如果超过屏幕
                        tipLeft = tipLeft - 5;
                    }
                    int tipTop = bTop + jtUpRight.getHeight();
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, tipLeft + tipViewBitmap.getWidth(), tipTop + tipViewHeight);
                }
            }
        } else {
            //bottom
            if (right <= screenWidth / 2 || screenWidth / 2 - left > right - screenWidth / 2) {
                //left
                int bLeft = vWidth / 2 + left;
                int bTop = getJtBottomTop(top, jtDownLeft, radius, cy);
                mCanvas.drawBitmap(jtDownLeft, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft;
                    int tipTop = bTop - tipViewHeight;
                    while (tipLeft + tipViewWidth > screenWidth) {
                        tipLeft = tipLeft - 5;
                    }
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, tipLeft + tipViewBitmap.getWidth(), bTop);
                }
            } else if (screenWidth / 2 - left + margin >= vWidth / 2 && right - screenWidth / 2 - margin >= vWidth / 2) {
                //center
                int bLeft = vWidth / 2 + left + margin - jtDownCenter.getWidth() / 2;
                int bTop = getJtBottomTop(top, jtDownCenter, radius, cy);
                mCanvas.drawBitmap(jtDownCenter, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft - tipViewBitmap.getWidth() / 2 + jtUpCenter.getWidth() / 2;
                    int tipTop = bTop - tipViewHeight;
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, tipLeft + tipViewBitmap.getWidth(), bTop);
                }
            } else {
                //right
                int bLeft = 0;
                if (left + margin + vWidth / 2 >= screenWidth / 5 * 3) {
                    bLeft = left - margin;
                } else {
                    bLeft = left + margin;
                }
                int bTop = getJtBottomTop(top, jtDownRight, radius, cy);
                mCanvas.drawBitmap(jtDownRight, bLeft, bTop, null);
                if (tipViewBitmap != null) {
                    int tipLeft = bLeft - tipViewBitmap.getWidth() / 2;
                    while (tipLeft + tipViewWidth > screenWidth) {
                        //如果超过屏幕
                        tipLeft = tipLeft - 5;
                    }
                    int tipTop = bTop - tipViewHeight;
                    mCanvas.drawBitmap(tipViewBitmap, tipLeft, tipTop, null);
                    tipViewHitRect = new Rect(tipLeft, tipTop, bLeft + tipViewBitmap.getWidth(), bTop);
                }
            }
        }
    }

    private int getJtUpTop(int bottom, int radius, int cy) {
        int top = 0;
        if (targetStyle == TargetStyle.TARGETVIEWSTYLE_CIRCLE.ordinal()) {
            top = cy + radius + margin;
        } else {
            top = bottom + margin;
        }
        return top;
    }

    private int getJtBottomTop(int vTop, Bitmap bitmap, int radius, int cy) {
        int top = 0;
        if (targetStyle == TargetStyle.TARGETVIEWSTYLE_CIRCLE.ordinal()) {
            top = cy - radius - margin - bitmap.getHeight();
        } else {
            top = vTop - bitmap.getHeight();
        }
        return top;
    }

    public void setTargetView(View view) {
        if (view != null && this.targetView != view) {
            Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            mCanvas.drawPaint(mPaint);
            mCanvas.drawColor(maskColor);
        }
        targetView = view;
        invalidate();
        setVisibility(View.VISIBLE);
    }

    public void setTargetView(View... views) {
        if (views != null) {
            if (targetViews == null) {
                targetViews = new ArrayList<>();
            }
            for (int i = 0; i < views.length; i++) {
                targetViews.add(views[i]);
            }
            setTargetView(targetViews.get(0));
            targetViews.remove(0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (touchOutsideCancel) {
                //点击不是区域内就消失
                if (targetViews == null || targetViews.size() == 0) {
                    setVisibility(View.GONE);
                    if (onDissmissListener != null) {
                        onDissmissListener.onDiss(this);
                    }
                } else {
                    setTargetView(targetViews.get(0));
                    targetViews.remove(0);
                }
                return true;
            } else {
                int x = (int) event.getX();
                int y = (int) event.getY();
                if (tipViewHitRect != null && tipViewHitRect.contains(x, y)) {
                    if (targetViews == null || targetViews.size() == 0) {
                        setVisibility(View.GONE);
                        if (onDissmissListener != null) {
                            onDissmissListener.onDiss(this);
                        }
                    } else {
                        setTargetView(targetViews.get(0));
                        targetViews.remove(0);
                    }
                    return true;
                }
            }
        }
        return true;
    }

    public void setMaskStyle(MaskStyle maskStyle) {
        this.maskStyle = maskStyle.ordinal();
    }

    public void setTargetStyle(TargetStyle targetStyle) {
        this.targetStyle = targetStyle.ordinal();
    }

    public void setOnDissmissListener(OnDissmissListener onDissmissListener) {
        this.onDissmissListener = onDissmissListener;
    }

    public void setTouchOutsideCancel(boolean touchOutsideCancel) {
        this.touchOutsideCancel = touchOutsideCancel;
    }

    public interface OnDissmissListener {
        void onDiss(GuideView guideView);
    }
}
