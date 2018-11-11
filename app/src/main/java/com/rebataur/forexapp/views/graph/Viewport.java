
package com.rebataur.forexapp.views.graph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;
import androidx.core.view.ViewCompat;
import androidx.core.widget.EdgeEffectCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Viewport {
    
    protected double referenceY = Double.NaN;

    
    protected double referenceX = Double.NaN;

    
    protected boolean scalableY;

    
    private RectD mMinimalViewport = new RectD(Double.NaN, Double.NaN, Double.NaN, Double.NaN);

    
    protected double getReferenceX() {
        // if the bounds is manual then we take the
        // original manual min y value as reference
        if (isXAxisBoundsManual() && !mGraphView.getGridLabelRenderer().isHumanRoundingX()) {
            if (Double.isNaN(referenceX)) {
                referenceX = getMinX(false);
            }
            return referenceX;
        } else {
            // starting from 0 so that the steps have nice numbers
            return 0;
        }
    }

    
    public interface OnXAxisBoundsChangedListener {
        
        void onXAxisBoundsChanged(double minX, double maxX, Reason reason);

        public enum Reason {
            SCROLL, SCALE
        }
    }

    
    private final ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener
            = new ScaleGestureDetector.OnScaleGestureListener() {
        
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // --- horizontal scaling ---
            double viewportWidth = mCurrentViewport.width();

            if (mMaxXAxisSize != 0) {
                if (viewportWidth > mMaxXAxisSize) {
                    viewportWidth = mMaxXAxisSize;
                }
            }

            double center = mCurrentViewport.left + viewportWidth / 2;

            float scaleSpanX;
            if (android.os.Build.VERSION.SDK_INT >= 11 && scalableY) {
                scaleSpanX = detector.getCurrentSpanX()/detector.getPreviousSpanX();
            } else {
                scaleSpanX = detector.getScaleFactor();
            }

            viewportWidth /= scaleSpanX;
            mCurrentViewport.left = center - viewportWidth / 2;
            mCurrentViewport.right = mCurrentViewport.left+viewportWidth;

            // viewportStart must not be < minX
            double minX = getMinX(true);
            if (!Double.isNaN(mMinimalViewport.left)) {
                minX = Math.min(minX, mMinimalViewport.left);
            }
            if (mCurrentViewport.left < minX) {
                mCurrentViewport.left = minX;
                mCurrentViewport.right = mCurrentViewport.left+viewportWidth;
            }

            // viewportStart + viewportSize must not be > maxX
            double maxX = getMaxX(true);
            if (!Double.isNaN(mMinimalViewport.right)) {
                maxX = Math.max(maxX, mMinimalViewport.right);
            }
            if (viewportWidth == 0) {
                mCurrentViewport.right = maxX;
            }
            double overlap = mCurrentViewport.left + viewportWidth - maxX;
            if (overlap > 0) {
                // scroll left
                if (mCurrentViewport.left-overlap > minX) {
                    mCurrentViewport.left -= overlap;
                    mCurrentViewport.right = mCurrentViewport.left+viewportWidth;
                } else {
                    // maximal scale
                    mCurrentViewport.left = minX;
                    mCurrentViewport.right = maxX;
                }
            }


            // --- vertical scaling ---
            if (scalableY && android.os.Build.VERSION.SDK_INT >= 11 && detector.getCurrentSpanY() != 0f && detector.getPreviousSpanY() != 0f) {
                boolean hasSecondScale = mGraphView.mSecondScale != null;

                double viewportHeight = mCurrentViewport.height()*-1;

                if (mMaxYAxisSize != 0) {
                    if (viewportHeight > mMaxYAxisSize) {
                        viewportHeight = mMaxYAxisSize;
                    }
                }

                center = mCurrentViewport.bottom + viewportHeight / 2;

                viewportHeight /= detector.getCurrentSpanY()/detector.getPreviousSpanY();
                mCurrentViewport.bottom = center - viewportHeight / 2;
                mCurrentViewport.top = mCurrentViewport.bottom+viewportHeight;

                // ignore bounds when second scale
                if (!hasSecondScale) {
                    // viewportStart must not be < minY
                    double minY = getMinY(true);
                    if (!Double.isNaN(mMinimalViewport.bottom)) {
                        minY = Math.min(minY, mMinimalViewport.bottom);
                    }
                    if (mCurrentViewport.bottom < minY) {
                        mCurrentViewport.bottom = minY;
                        mCurrentViewport.top = mCurrentViewport.bottom+viewportHeight;
                    }

                    // viewportStart + viewportSize must not be > maxY
                    double maxY = getMaxY(true);
                    if (!Double.isNaN(mMinimalViewport.top)) {
                        maxY = Math.max(maxY, mMinimalViewport.top);
                    }
                    if (viewportHeight == 0) {
                        mCurrentViewport.top = maxY;
                    }
                    overlap = mCurrentViewport.bottom + viewportHeight - maxY;
                    if (overlap > 0) {
                        // scroll left
                        if (mCurrentViewport.bottom-overlap > minY) {
                            mCurrentViewport.bottom -= overlap;
                            mCurrentViewport.top = mCurrentViewport.bottom+viewportHeight;
                        } else {
                            // maximal scale
                            mCurrentViewport.bottom = minY;
                            mCurrentViewport.top = maxY;
                        }
                    }
                } else {
                    // ---- second scale ---
                    viewportHeight = mGraphView.mSecondScale.mCurrentViewport.height()*-1;
                    center = mGraphView.mSecondScale.mCurrentViewport.bottom + viewportHeight / 2;
                    viewportHeight /= detector.getCurrentSpanY()/detector.getPreviousSpanY();
                    mGraphView.mSecondScale.mCurrentViewport.bottom = center - viewportHeight / 2;
                    mGraphView.mSecondScale.mCurrentViewport.top = mGraphView.mSecondScale.mCurrentViewport.bottom+viewportHeight;
                }
            }

            // adjustSteps viewport, labels, etc.
            mGraphView.onDataChanged(true, false);

            ViewCompat.postInvalidateOnAnimation(mGraphView);

            return true;
        }

        
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            if (mIsScalable) {
                mScalingActive = true;
                return true;
            } else {
                return false;
            }
        }

        
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mScalingActive = false;

            // notify
            if (mOnXAxisBoundsChangedListener != null) {
                mOnXAxisBoundsChangedListener.onXAxisBoundsChanged(getMinX(false), getMaxX(false), OnXAxisBoundsChangedListener.Reason.SCALE);
            }

            ViewCompat.postInvalidateOnAnimation(mGraphView);
        }
    };

    
    private final GestureDetector.SimpleOnGestureListener mGestureListener
            = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onDown(MotionEvent e) {

            if (!mIsScrollable || mScalingActive) return false;

            // Initiates the decay phase of any active edge effects.
            releaseEdgeEffects();
            // Aborts any active scroll animations and invalidates.
            mScroller.forceFinished(true);
            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!mIsScrollable || mScalingActive) return false;

            // Scrolling uses math based on the viewport (as opposed to math using pixels).
            
            double viewportOffsetX = distanceX * mCurrentViewport.width() / mGraphView.getGraphContentWidth();
            double viewportOffsetY = distanceY * mCurrentViewport.height() / mGraphView.getGraphContentHeight();

            // respect minimal viewport
            double completeRangeLeft = mCompleteRange.left;
            if (!Double.isNaN(mMinimalViewport.left)) {
                completeRangeLeft = Math.min(completeRangeLeft, mMinimalViewport.left);
            }
            double completeRangeRight = mCompleteRange.right;
            if (!Double.isNaN(mMinimalViewport.right)) {
                completeRangeRight = Math.max(completeRangeRight, mMinimalViewport.right);
            }
            double completeRangeWidth = completeRangeRight - completeRangeLeft;

            double completeRangeBottom = mCompleteRange.bottom;
            if (!Double.isNaN(mMinimalViewport.bottom)) {
                completeRangeBottom = Math.min(completeRangeBottom, mMinimalViewport.bottom);
            }
            double completeRangeTop = mCompleteRange.top;
            if (!Double.isNaN(mMinimalViewport.top)) {
                completeRangeTop = Math.max(completeRangeTop, mMinimalViewport.top);
            }
            double completeRangeHeight = completeRangeTop - completeRangeBottom;

            int completeWidth = (int)((completeRangeWidth/mCurrentViewport.width()) * (double) mGraphView.getGraphContentWidth());
            int completeHeight = (int)((completeRangeHeight/mCurrentViewport.height()) * (double) mGraphView.getGraphContentHeight());

            int scrolledX = (int) (completeWidth
                    * (mCurrentViewport.left + viewportOffsetX - completeRangeLeft)
                    / completeRangeWidth);

            int scrolledY = (int) (completeHeight
                    * (mCurrentViewport.bottom + viewportOffsetY - completeRangeBottom)
                    / completeRangeHeight*-1);
            boolean canScrollX = mCurrentViewport.left > completeRangeLeft
                    || mCurrentViewport.right < completeRangeRight;
            boolean canScrollY = mCurrentViewport.bottom > completeRangeBottom
                    || mCurrentViewport.top < completeRangeTop;

            boolean hasSecondScale = mGraphView.mSecondScale != null;

            // second scale
            double viewportOffsetY2 = 0d;
            if (hasSecondScale) {
                viewportOffsetY2 = distanceY * mGraphView.mSecondScale.mCurrentViewport.height() / mGraphView.getGraphContentHeight();
                canScrollY |= mGraphView.mSecondScale.mCurrentViewport.bottom > mGraphView.mSecondScale.mCompleteRange.bottom
                        || mGraphView.mSecondScale.mCurrentViewport.top < mGraphView.mSecondScale.mCompleteRange.top;
            }

            canScrollY &= scrollableY;

            if (canScrollX) {
                if (viewportOffsetX < 0) {
                    double tooMuch = mCurrentViewport.left+viewportOffsetX - completeRangeLeft;
                    if (tooMuch < 0) {
                        viewportOffsetX -= tooMuch;
                    }
                } else {
                    double tooMuch = mCurrentViewport.right+viewportOffsetX - completeRangeRight;
                    if (tooMuch > 0) {
                        viewportOffsetX -= tooMuch;
                    }
                }

                mCurrentViewport.left += viewportOffsetX;
                mCurrentViewport.right += viewportOffsetX;

                // notify
                if (mOnXAxisBoundsChangedListener != null) {
                    mOnXAxisBoundsChangedListener.onXAxisBoundsChanged(getMinX(false), getMaxX(false), OnXAxisBoundsChangedListener.Reason.SCROLL);
                }
            }
            if (canScrollY) {
                // if we have the second axis we ignore the max/min range
                if (!hasSecondScale) {
                    if (viewportOffsetY < 0) {
                        double tooMuch = mCurrentViewport.bottom+viewportOffsetY - completeRangeBottom;
                        if (tooMuch < 0) {
                            viewportOffsetY -= tooMuch;
                        }
                    } else {
                        double tooMuch = mCurrentViewport.top+viewportOffsetY - completeRangeTop;
                        if (tooMuch > 0) {
                            viewportOffsetY -= tooMuch;
                        }
                    }
                }

                mCurrentViewport.top += viewportOffsetY;
                mCurrentViewport.bottom += viewportOffsetY;

                // second scale
                if (hasSecondScale) {
                    mGraphView.mSecondScale.mCurrentViewport.top += viewportOffsetY2;
                    mGraphView.mSecondScale.mCurrentViewport.bottom += viewportOffsetY2;
                }
            }

            if (canScrollX && scrolledX < 0) {
                mEdgeEffectLeft.onPull(scrolledX / (float) mGraphView.getGraphContentWidth());
            }
            if (!hasSecondScale && canScrollY && scrolledY < 0) {
                mEdgeEffectBottom.onPull(scrolledY / (float) mGraphView.getGraphContentHeight());
            }
            if (canScrollX && scrolledX > completeWidth - mGraphView.getGraphContentWidth()) {
                mEdgeEffectRight.onPull((scrolledX - completeWidth + mGraphView.getGraphContentWidth())
                        / (float) mGraphView.getGraphContentWidth());
            }
            if (!hasSecondScale && canScrollY && scrolledY > completeHeight - mGraphView.getGraphContentHeight()) {
                mEdgeEffectTop.onPull((scrolledY - completeHeight + mGraphView.getGraphContentHeight())
                        / (float) mGraphView.getGraphContentHeight());
            }

            // adjustSteps viewport, labels, etc.
            mGraphView.onDataChanged(true, false);

            ViewCompat.postInvalidateOnAnimation(mGraphView);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                               float velocityX, float velocityY) {
            //fling((int) -velocityX, (int) -velocityY);
            return true;
        }
    };

    
    public enum AxisBoundsStatus {
        
        INITIAL,

        
        AUTO_ADJUSTED,

        
        FIX
    }

    
    private Paint mPaint;

    
    private final GraphView mGraphView;

    
    protected RectD mCurrentViewport = new RectD();

    
    protected double mMaxXAxisSize = 0;

    
    protected double mMaxYAxisSize = 0;

    
    protected RectD mCompleteRange = new RectD();

    
    protected boolean mScalingActive;

    
    private boolean mIsScrollable;

    
    private boolean mIsScalable;

    
    private boolean scrollableY;

    
    protected GestureDetector mGestureDetector;

    
    protected ScaleGestureDetector mScaleGestureDetector;

    
    protected OverScroller mScroller;

    
    private EdgeEffectCompat mEdgeEffectTop;

    
    private EdgeEffectCompat mEdgeEffectBottom;

    
    private EdgeEffectCompat mEdgeEffectLeft;

    
    private EdgeEffectCompat mEdgeEffectRight;

    
    protected AxisBoundsStatus mXAxisBoundsStatus;

    
    protected AxisBoundsStatus mYAxisBoundsStatus;

    
    private boolean mXAxisBoundsManual;

    
    private boolean mYAxisBoundsManual;

    
    private int mBackgroundColor;

    
    protected OnXAxisBoundsChangedListener mOnXAxisBoundsChangedListener;

    
    private boolean mDrawBorder;

    
    private Integer mBorderColor;

    
    private Paint mBorderPaint;

    
    Viewport(GraphView graphView) {
        mScroller = new OverScroller(graphView.getContext());
        mEdgeEffectTop = new EdgeEffectCompat(graphView.getContext());
        mEdgeEffectBottom = new EdgeEffectCompat(graphView.getContext());
        mEdgeEffectLeft = new EdgeEffectCompat(graphView.getContext());
        mEdgeEffectRight = new EdgeEffectCompat(graphView.getContext());
        mGestureDetector = new GestureDetector(graphView.getContext(), mGestureListener);
        mScaleGestureDetector = new ScaleGestureDetector(graphView.getContext(), mScaleGestureListener);

        mGraphView = graphView;
        mXAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        mYAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        mBackgroundColor = Color.TRANSPARENT;
        mPaint = new Paint();
    }

    
    public boolean onTouchEvent(MotionEvent event) {
        boolean b = mScaleGestureDetector.onTouchEvent(event);
        b |= mGestureDetector.onTouchEvent(event);
        return b;
    }

    
    public void setXAxisBoundsStatus(AxisBoundsStatus s) {
        mXAxisBoundsStatus = s;
    }

    
    public void setYAxisBoundsStatus(AxisBoundsStatus s) {
        mYAxisBoundsStatus = s;
    }

    
    public boolean isScrollable() {
        return mIsScrollable;
    }

    
    public void setScrollable(boolean mIsScrollable) {
        this.mIsScrollable = mIsScrollable;
    }

    
    public AxisBoundsStatus getXAxisBoundsStatus() {
        return mXAxisBoundsStatus;
    }

    
    public AxisBoundsStatus getYAxisBoundsStatus() {
        return mYAxisBoundsStatus;
    }

    
    public void calcCompleteRange() {
        List<Series> series = mGraphView.getSeries();
        List<Series> seriesInclusiveSecondScale = new ArrayList<>(mGraphView.getSeries());
        if (mGraphView.mSecondScale != null) {
            seriesInclusiveSecondScale.addAll(mGraphView.mSecondScale.getSeries());
        }
        mCompleteRange.set(0d, 0d, 0d, 0d);
        if (!seriesInclusiveSecondScale.isEmpty() && !seriesInclusiveSecondScale.get(0).isEmpty()) {
            double d = seriesInclusiveSecondScale.get(0).getLowestValueX();
            for (Series s : seriesInclusiveSecondScale) {
                if (!s.isEmpty() && d > s.getLowestValueX()) {
                    d = s.getLowestValueX();
                }
            }
            mCompleteRange.left = d;

            d = seriesInclusiveSecondScale.get(0).getHighestValueX();
            for (Series s : seriesInclusiveSecondScale) {
                if (!s.isEmpty() && d < s.getHighestValueX()) {
                    d = s.getHighestValueX();
                }
            }
            mCompleteRange.right = d;

            if (!series.isEmpty() && !series.get(0).isEmpty()) {
                d = series.get(0).getLowestValueY();
                for (Series s : series) {
                    if (!s.isEmpty() && d > s.getLowestValueY()) {
                        d = s.getLowestValueY();
                    }
                }
                mCompleteRange.bottom = d;

                d = series.get(0).getHighestValueY();
                for (Series s : series) {
                    if (!s.isEmpty() && d < s.getHighestValueY()) {
                        d = s.getHighestValueY();
                    }
                }
                mCompleteRange.top = d;
            }
        }

        // calc current viewport bounds
        if (mYAxisBoundsStatus == AxisBoundsStatus.AUTO_ADJUSTED) {
            mYAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        }
        if (mYAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            mCurrentViewport.top = mCompleteRange.top;
            mCurrentViewport.bottom = mCompleteRange.bottom;
        }

        if (mXAxisBoundsStatus == AxisBoundsStatus.AUTO_ADJUSTED) {
            mXAxisBoundsStatus = AxisBoundsStatus.INITIAL;
        }
        if (mXAxisBoundsStatus == AxisBoundsStatus.INITIAL) {
            mCurrentViewport.left = mCompleteRange.left;
            mCurrentViewport.right = mCompleteRange.right;
        } else if (mXAxisBoundsManual && !mYAxisBoundsManual && mCompleteRange.width() != 0) {
            // get highest/lowest of current viewport
            // lowest
            double d = Double.MAX_VALUE;
            for (Series s : series) {
                Iterator<DataPointInterface> values = s.getValues(mCurrentViewport.left, mCurrentViewport.right);
                while (values.hasNext()) {
                    double v = values.next().getY();
                    if (d > v) {
                        d = v;
                    }
                }
            }

            if (d != Double.MAX_VALUE) {
                mCurrentViewport.bottom = d;
            }

            // highest
            d = Double.MIN_VALUE;
            for (Series s : series) {
                Iterator<DataPointInterface> values = s.getValues(mCurrentViewport.left, mCurrentViewport.right);
                while (values.hasNext()) {
                    double v = values.next().getY();
                    if (d < v) {
                        d = v;
                    }
                }
            }

            if (d != Double.MIN_VALUE) {
                mCurrentViewport.top = d;
            }
        }

        // fixes blank screen when range is zero
        if (mCurrentViewport.left == mCurrentViewport.right) mCurrentViewport.right++;
        if (mCurrentViewport.top == mCurrentViewport.bottom) mCurrentViewport.top++;
    }

    
    public double getMinX(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.left;
        } else {
            return mCurrentViewport.left;
        }
    }

    
    public double getMaxX(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.right;
        } else {
            return mCurrentViewport.right;
        }
    }

    
    public double getMinY(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.bottom;
        } else {
            return mCurrentViewport.bottom;
        }
    }

    
    public double getMaxY(boolean completeRange) {
        if (completeRange) {
            return mCompleteRange.top;
        } else {
            return mCurrentViewport.top;
        }
    }

    
    public void setMaxY(double y) {
        mCurrentViewport.top = y;
    }

    
    public void setMinY(double y) {
        mCurrentViewport.bottom = y;
    }

    
    public void setMaxX(double x) {
        mCurrentViewport.right = x;
    }

    
    public void setMinX(double x) {
        mCurrentViewport.left = x;
    }

    
    private void releaseEdgeEffects() {
        mEdgeEffectLeft.onRelease();
        mEdgeEffectRight.onRelease();
        mEdgeEffectTop.onRelease();
        mEdgeEffectBottom.onRelease();
    }

    
    private void fling(int velocityX, int velocityY) {
        velocityY = 0;
        releaseEdgeEffects();
        // Flings use math in pixels (as opposed to math based on the viewport).
        int maxX = (int)((mCurrentViewport.width()/mCompleteRange.width())*(float)mGraphView.getGraphContentWidth()) - mGraphView.getGraphContentWidth();
        int maxY = (int)((mCurrentViewport.height()/mCompleteRange.height())*(float)mGraphView.getGraphContentHeight()) - mGraphView.getGraphContentHeight();
        int startX = (int)((mCurrentViewport.left - mCompleteRange.left)/mCompleteRange.width())*maxX;
        int startY = (int)((mCurrentViewport.top - mCompleteRange.top)/mCompleteRange.height())*maxY;
        mScroller.forceFinished(true);
        mScroller.fling(
                startX,
                startY,
                velocityX,
                velocityY,
                0, maxX,
                0, maxY,
                mGraphView.getGraphContentWidth() / 2,
                mGraphView.getGraphContentHeight() / 2);
        ViewCompat.postInvalidateOnAnimation(mGraphView);
    }

    
    public void computeScroll() {
    }

    
    private void drawEdgeEffectsUnclipped(Canvas canvas) {
        // The methods below rotate and translate the canvas as needed before drawing the glow,
        // since EdgeEffectCompat always draws a top-glow at 0,0.

        boolean needsInvalidate = false;

        if (!mEdgeEffectTop.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop());
            mEdgeEffectTop.setSize(mGraphView.getGraphContentWidth(), mGraphView.getGraphContentHeight());
            if (mEdgeEffectTop.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectBottom.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight());
            canvas.rotate(180, mGraphView.getGraphContentWidth()/2, 0);
            mEdgeEffectBottom.setSize(mGraphView.getGraphContentWidth(), mGraphView.getGraphContentHeight());
            if (mEdgeEffectBottom.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectLeft.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft(), mGraphView.getGraphContentTop()+ mGraphView.getGraphContentHeight());
            canvas.rotate(-90, 0, 0);
            mEdgeEffectLeft.setSize(mGraphView.getGraphContentHeight(), mGraphView.getGraphContentWidth());
            if (mEdgeEffectLeft.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (!mEdgeEffectRight.isFinished()) {
            final int restoreCount = canvas.save();
            canvas.translate(mGraphView.getGraphContentLeft()+ mGraphView.getGraphContentWidth(), mGraphView.getGraphContentTop());
            canvas.rotate(90, 0, 0);
            mEdgeEffectRight.setSize(mGraphView.getGraphContentHeight(), mGraphView.getGraphContentWidth());
            if (mEdgeEffectRight.draw(canvas)) {
                needsInvalidate = true;
            }
            canvas.restoreToCount(restoreCount);
        }

        if (needsInvalidate) {
            ViewCompat.postInvalidateOnAnimation(mGraphView);
        }
    }

    
    public void drawFirst(Canvas c) {
        // draw background
        if (mBackgroundColor != Color.TRANSPARENT) {
            mPaint.setColor(mBackgroundColor);
            c.drawRect(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop(),
                    mGraphView.getGraphContentLeft()+mGraphView.getGraphContentWidth(),
                    mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(),
                    mPaint
            );
        }
        if (mDrawBorder) {
            Paint p;
            if (mBorderPaint != null) {
                p = mBorderPaint;
            } else {
                p = mPaint;
                p.setColor(getBorderColor());
            }
            c.drawLine(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop(),
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(),
                    p
            );
            c.drawLine(
                    mGraphView.getGraphContentLeft(),
                    mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(),
                    mGraphView.getGraphContentLeft()+mGraphView.getGraphContentWidth(),
                    mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(),
                    p
            );
            // on the right side if we have second scale
            if (mGraphView.mSecondScale != null) {
                c.drawLine(
                        mGraphView.getGraphContentLeft()+mGraphView.getGraphContentWidth(),
                        mGraphView.getGraphContentTop(),
                        mGraphView.getGraphContentLeft()+mGraphView.getGraphContentWidth(),
                        mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight(),
                        p
                );
            }
        }
    }

    
    public void draw(Canvas c) {
        drawEdgeEffectsUnclipped(c);
    }

    
    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    
    public void setBackgroundColor(int mBackgroundColor) {
        this.mBackgroundColor = mBackgroundColor;
    }

    
    public boolean isScalable() {
        return mIsScalable;
    }

    
    public void setScalable(boolean mIsScalable) {
        this.mIsScalable = mIsScalable;
        if (mIsScalable) {
            mIsScrollable = true;

            // set viewport to manual
            setXAxisBoundsManual(true);
        }

    }

    
    public boolean isXAxisBoundsManual() {
        return mXAxisBoundsManual;
    }

    
    public void setXAxisBoundsManual(boolean mXAxisBoundsManual) {
        this.mXAxisBoundsManual = mXAxisBoundsManual;
        if (mXAxisBoundsManual) {
            mXAxisBoundsStatus = AxisBoundsStatus.FIX;
        }
    }

    
    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }

    
    public void setYAxisBoundsManual(boolean mYAxisBoundsManual) {
        this.mYAxisBoundsManual = mYAxisBoundsManual;
        if (mYAxisBoundsManual) {
            mYAxisBoundsStatus = AxisBoundsStatus.FIX;
        }
    }

    
    public void scrollToEnd() {
        if (mXAxisBoundsManual) {
            double size = mCurrentViewport.width();
            mCurrentViewport.right = mCompleteRange.right;
            mCurrentViewport.left = mCompleteRange.right - size;
            mGraphView.onDataChanged(true, false);
        } else {
            Log.w("GraphView", "scrollToEnd works only with manual x axis bounds");
        }
    }

    
    public OnXAxisBoundsChangedListener getOnXAxisBoundsChangedListener() {
        return mOnXAxisBoundsChangedListener;
    }

    
    public void setOnXAxisBoundsChangedListener(OnXAxisBoundsChangedListener l) {
        mOnXAxisBoundsChangedListener = l;
    }

    
    public void setDrawBorder(boolean drawBorder) {
        this.mDrawBorder = drawBorder;
    }

    
    public int getBorderColor() {
        if (mBorderColor != null) {
            return mBorderColor;
        }
        return mGraphView.getGridLabelRenderer().getGridColor();
    }

    
    public void setBorderColor(Integer borderColor) {
        this.mBorderColor = borderColor;
    }

    
    public void setBorderPaint(Paint borderPaint) {
        this.mBorderPaint = borderPaint;
    }

    
    public void setScrollableY(boolean scrollableY) {
        this.scrollableY = scrollableY;
    }

    
    protected double getReferenceY() {
        // if the bounds is manual then we take the
        // original manual min y value as reference
        if (isYAxisBoundsManual() && !mGraphView.getGridLabelRenderer().isHumanRoundingY()) {
            if (Double.isNaN(referenceY)) {
                referenceY = getMinY(false);
            }
            return referenceY;
        } else {
            // starting from 0 so that the steps have nice numbers
            return 0;
        }
    }

    
    public void setScalableY(boolean scalableY) {
        if (scalableY) {
            this.scrollableY = true;
            setScalable(true);

            if (android.os.Build.VERSION.SDK_INT < 11) {
                Log.w("GraphView", "Vertical scaling requires minimum Android 3.0 (API Level 11)");
            }
        }
        this.scalableY = scalableY;
    }

    
    public double getMaxXAxisSize() {
        return mMaxXAxisSize;
    }

    
    public double getMaxYAxisSize() {
        return mMaxYAxisSize;
    }

    
    public void setMaxXAxisSize(double mMaxXAxisViewportSize) {
        this.mMaxXAxisSize = mMaxXAxisViewportSize;
    }

    
    public void setMaxYAxisSize(double mMaxYAxisViewportSize) {
        this.mMaxYAxisSize = mMaxYAxisViewportSize;
    }

    
    public void setMinimalViewport(double minX, double maxX, double minY, double maxY) {
       mMinimalViewport.set(minX, maxY, maxX, minY);
    }
}
