package com.yandex.divkit.demo.div.drawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class PaintView extends View {

    //region variable
    public static int BRUSH_SIZE = 5;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static int DEFAULT_BG_COLOR = Color.WHITE;
    private static final float TOUCH_TOLERANCE = 4;
    public static float mX, mY, cX, cY;
    private Path mPath;
    private Paint mPaint, cPaint;
    private ArrayList<FingerPath> paths = new ArrayList<>();
    private ArrayList<FingerPath> pathsUndo = new ArrayList<>();
    private int currentColor;
    private int backgroundColor = DEFAULT_BG_COLOR;
    private int strokeWidth;
    private boolean emboss;
    private boolean blur;
    private MaskFilter mEmboss;
    private MaskFilter mBlur;
    private Bitmap mBitmap;
    private Bitmap mBitmapBackup;
    private Canvas mCanvas;
//    private Canvas mCanvasBackup;
    private Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    //endregion

    public boolean boolDrawCircle = false, greatThen = false;
    private int height;
    private int width;

    public PaintView(Context context) {
        this(context, null);
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);

        cPaint = new Paint();
        cPaint.setAntiAlias(true);
        cPaint.setDither(true);
        cPaint.setColor(Color.TRANSPARENT);
        cPaint.setStyle(Paint.Style.STROKE);
        cPaint.setStrokeJoin(Paint.Join.ROUND);
        cPaint.setStrokeCap(Paint.Cap.BUTT);
        cPaint.setXfermode(null);
        cPaint.setAlpha(0xff);

        mEmboss = new EmbossMaskFilter(new float[]{1, 1, 1}, 0.4f, 6, 3.5f);
        mBlur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }


    public void eraser() {
        currentColor = -1;
    }

    public void paint() {
        currentColor = Color.BLACK;
    }

    public void clear() {
        backgroundColor = DEFAULT_BG_COLOR;
        paths.clear();
        normal();
        mX = 0;
        cX = 0;
        mY = 0;
        cY = 0;
        cPaint.setColor(Color.TRANSPARENT);
        boolDrawCircle = false;
        greatThen = false;
        invalidate();
    }

    public void unDo() {

        if (paths.size() > 0) {
            pathsUndo.add(paths.remove(paths.size() - 1));
            invalidate();
        } else {

        }
        //toast the user

    }

    public void reDo() {
        if (pathsUndo.size() > 0) {
            paths.add(pathsUndo.remove(pathsUndo.size() - 1));
            invalidate();
        } else {

        }
    }

    public void setBackground(int background) {
        DEFAULT_BG_COLOR = backgroundColor;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(backgroundColor);

        for (FingerPath fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);
            mPaint.setMaskFilter(null);
            if (fp.emboss)
                mPaint.setMaskFilter(mEmboss);
            else if (fp.blur)
                mPaint.setMaskFilter(mBlur);
            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        final float radius = 100;
        if (boolDrawCircle) {
            cPaint.setColor(Color.RED);
        } else
            cPaint.setColor(Color.TRANSPARENT);

        if (greatThen) {
            cPaint.setColor(Color.TRANSPARENT);
        }

        cPaint.setStrokeWidth(0);
        canvas.drawCircle(cX, cY, radius, cPaint);

        if (Math.abs(cX - mX) > 90 || Math.abs(cY - mY) > 90) {
            greatThen = true;
        }
        canvas.restore();
    }

    public void setBrushSize(int size) {
        strokeWidth = size;
    }

    private void touchStart(float x, float y) {
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, emboss, blur, strokeWidth, mPath);
        paths.add(fp);
        pathsUndo.add(fp);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
        cX = x;
        cY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    public void init(DisplayMetrics metrics, int defaultBrushSize, int intBackground) {
        strokeWidth = defaultBrushSize;
        backgroundColor = DEFAULT_BG_COLOR = intBackground;
//        backgroundColor = intBackground;
        height = metrics.heightPixels;
        width = metrics.widthPixels;

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        currentColor = DEFAULT_COLOR;
    }

    public void normal() {
        emboss = false;
        blur = false;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }


}