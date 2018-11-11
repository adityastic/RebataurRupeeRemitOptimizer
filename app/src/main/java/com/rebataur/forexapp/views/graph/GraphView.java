
package com.rebataur.forexapp.views.graph;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class GraphView extends View {
    
    private static final class Styles {
        
        float titleTextSize;
        int titleColor;
    }

    
    private class TapDetector {
        
        private long lastDown;

        
        private PointF lastPoint;

        
        public boolean onTouchEvent(MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastDown = System.currentTimeMillis();
                lastPoint = new PointF(event.getX(), event.getY());
            } else if (lastDown > 0 && event.getAction() == MotionEvent.ACTION_MOVE) {
                if (Math.abs(event.getX() - lastPoint.x) > 60
                        || Math.abs(event.getY() - lastPoint.y) > 60) {
                    lastDown = 0;
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (System.currentTimeMillis() - lastDown < 400) {
                    return true;
                }
            }
            return false;
        }
    }

    
    private List<Series> mSeries;

    
    private GridLabelRenderer mGridLabelRenderer;


    public GridLabelRenderer getmGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    public void setmGridLabelRenderer(GridLabelRenderer mGridLabelRenderer) {
        this.mGridLabelRenderer = mGridLabelRenderer;
    }

    private Viewport mViewport;

    
    private String mTitle;

    
    private Styles mStyles;

    
    protected SecondScale mSecondScale;

    
    private TapDetector mTapDetector;

    
    private LegendRenderer mLegendRenderer;

    
    private Paint mPaintTitle;

    private Paint mPreviewPaint;

    
    public GraphView(Context context) {
        super(context);
        init();
    }

    
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    
    protected void init() {
        mPreviewPaint = new Paint();
        mPreviewPaint.setTextAlign(Paint.Align.CENTER);
        mPreviewPaint.setColor(Color.BLACK);
        mPreviewPaint.setTextSize(50);

        mStyles = new Styles();
        mViewport = new Viewport(this);
        mGridLabelRenderer = new GridLabelRenderer(this);
        mLegendRenderer = new LegendRenderer(this);

        mSeries = new ArrayList<Series>();
        mPaintTitle = new Paint();

        mTapDetector = new TapDetector();

        loadStyles();
    }

    
    protected void loadStyles() {
        mStyles.titleColor = mGridLabelRenderer.getHorizontalLabelsColor();
        mStyles.titleTextSize = mGridLabelRenderer.getTextSize();
    }

    
    public GridLabelRenderer getGridLabelRenderer() {
        return mGridLabelRenderer;
    }

    
    public void addSeries(Series s) {
        s.onGraphViewAttached(this);
        mSeries.add(s);
        onDataChanged(false, false);
    }

    public void reset(){
        mSeries = new ArrayList<>();
        onDataChanged(false, false);
    }

    
    public List<Series> getSeries() {
        // TODO immutable array
        return mSeries;
    }

    
    public void onDataChanged(boolean keepLabelsSize, boolean keepViewport) {
        // adjustSteps grid system
        mViewport.calcCompleteRange();
        if (mSecondScale != null) {
            mSecondScale.calcCompleteRange();
        }
        mGridLabelRenderer.invalidate(keepLabelsSize, keepViewport);
        postInvalidate();
    }

    
    protected void drawGraphElements(Canvas canvas) {
        // must be in hardware accelerated mode
        if (!canvas.isHardwareAccelerated()) {
            // just warn about it, because it is ok when making a snapshot
            Log.w("GraphView", "GraphView should be used in hardware accelerated mode." +
                    "You can use android:hardwareAccelerated=\"true\" on your activity. Read this for more info:" +
                    "https://developer.android.com/guide/topics/graphics/hardware-accel.html");
        }

        drawTitle(canvas);
        mViewport.drawFirst(canvas);
        mGridLabelRenderer.draw(canvas);
        for (Series s : mSeries) {
            s.draw(this, canvas, false);
        }
        if (mSecondScale != null) {
            for (Series s : mSecondScale.getSeries()) {
                s.draw(this, canvas, true);
            }
        }

        mViewport.draw(canvas);
        mLegendRenderer.draw(canvas);
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            canvas.drawColor(Color.rgb(200, 200, 200));
            canvas.drawText("GraphView: No Preview available", canvas.getWidth()/2, canvas.getHeight()/2, mPreviewPaint);
        } else {
            drawGraphElements(canvas);
        }
    }

    
    protected void drawTitle(Canvas canvas) {
        if (mTitle != null && mTitle.length()>0) {
            mPaintTitle.setColor(mStyles.titleColor);
            mPaintTitle.setTextSize(mStyles.titleTextSize);
            mPaintTitle.setTextAlign(Paint.Align.CENTER);
            float x = canvas.getWidth()/2;
            float y = mPaintTitle.getTextSize();
            canvas.drawText(mTitle, x, y, mPaintTitle);
        }
    }

    
    protected int getTitleHeight() {
        if (mTitle != null && mTitle.length()>0) {
            return (int) mPaintTitle.getTextSize();
        } else {
            return 0;
        }
    }

    
    public Viewport getViewport() {
        return mViewport;
    }

    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onDataChanged(false, false);
    }

    
    public int getGraphContentLeft() {
        int border = getGridLabelRenderer().getStyles().padding;
        return border + getGridLabelRenderer().getLabelVerticalWidth() + getGridLabelRenderer().getVerticalAxisTitleWidth();
    }

    
    public int getGraphContentTop() {
        int border = getGridLabelRenderer().getStyles().padding + getTitleHeight();
        return border;
    }

    
    public int getGraphContentHeight() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphheight = getHeight() - (2 * border) - getGridLabelRenderer().getLabelHorizontalHeight() - getTitleHeight();
        graphheight -= getGridLabelRenderer().getHorizontalAxisTitleHeight();
        return graphheight;
    }

    
    public int getGraphContentWidth() {
        int border = getGridLabelRenderer().getStyles().padding;
        int graphwidth = getWidth() - (2 * border) - getGridLabelRenderer().getLabelVerticalWidth();
        if (mSecondScale != null) {
            graphwidth -= getGridLabelRenderer().getLabelVerticalSecondScaleWidth();
            graphwidth -= mSecondScale.getVerticalAxisTitleTextSize();
        }
        return graphwidth;
    }

    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mViewport.onTouchEvent(event);
        boolean a = super.onTouchEvent(event);

        // is it a click?
        if (mTapDetector.onTouchEvent(event)) {
            for (Series s : mSeries) {
                s.onTap(event.getX(), event.getY());
            }
            if (mSecondScale != null) {
                for (Series s : mSecondScale.getSeries()) {
                    s.onTap(event.getX(), event.getY());
                }
            }
        }

        return b || a;
    }

    
    @Override
    public void computeScroll() {
        super.computeScroll();
        mViewport.computeScroll();
    }

    public String getTitle() {
        return mTitle;
    }

    
    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    
    public SecondScale getSecondScale() {
        if (mSecondScale == null) {
            // this creates the second scale
            mSecondScale = new SecondScale(this);
            mSecondScale.setVerticalAxisTitleTextSize(mGridLabelRenderer.mStyles.textSize);
        }
        return mSecondScale;
    }
}
