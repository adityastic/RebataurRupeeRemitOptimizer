
package com.rebataur.forexapp.views.graph;

import android.content.Context;
import android.graphics.*;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import androidx.core.view.ViewCompat;
import com.rebataur.forexapp.R;


import java.util.Iterator;


public class LineGraphSeries<E extends DataPointInterface> extends BaseSeries<E> {
    private static final long ANIMATION_DURATION = 333;

    
    private final class Styles {
        
        private int thickness = 5;

        
        private boolean drawBackground = false;

        
        private boolean drawDataPoints = false;

        
        private float dataPointsRadius = 10f;
    }

    private Context context;
    
    private Styles mStyles;

    private Paint mSelectionPaint;

    
    private Paint mPaint;

    
    private Paint mPaintBackground;

    
    private Path mPathBackground;

    
    private Path mPath;

    
    private Paint mCustomPaint;

    
    private boolean mAnimated;

    
    private double mLastAnimatedValue = Double.NaN;

    
    private long mAnimationStart;

    
    private AccelerateInterpolator mAnimationInterpolator;

    
    private int mAnimationStartFrameNo;

    
    private boolean mDrawAsPath = false;

    
    public LineGraphSeries(Context context) {
        init();
        this.context = context;
    }

    
    public LineGraphSeries(E[] data, Context context) {
        super(data);
        this.context = context;
        init();
    }

    
    protected void init() {
        mStyles = new Styles();
        mPaint = new Paint();
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaintBackground = new Paint();

        mSelectionPaint = new Paint();
        mSelectionPaint.setColor(Color.argb(80, 0, 0, 0));
        mSelectionPaint.setStyle(Paint.Style.FILL);

        mPathBackground = new Path();
        mPath = new Path();

        mAnimationInterpolator = new AccelerateInterpolator(2f);
    }

    
    @Override
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale) {
        resetDataPoints();

        // get data
        double maxX = graphView.getViewport().getMaxX(false);
        double minX = graphView.getViewport().getMinX(false);

        double maxY;
        double minY;
        if (isSecondScale) {
            maxY = graphView.getSecondScale().getMaxY(false);
            minY = graphView.getSecondScale().getMinY(false);
        } else {
            maxY = graphView.getViewport().getMaxY(false);
            minY = graphView.getViewport().getMinY(false);
        }

        Iterator<E> values = getValues(minX, maxX);

        // draw background
        double lastEndY = 0;
        double lastEndX = 0;

        // draw data
        mPaint.setStrokeWidth(mStyles.thickness);
        mPaint.setColor(getColor());


        mPaintBackground.setShader(new LinearGradient(0, 0, 0, graphView.getGraphContentHeight(), getContext().getResources().getColor(R.color.colorAccentAlpha) , Color.TRANSPARENT, Shader.TileMode.MIRROR));

        Paint paint;
        if (mCustomPaint != null) {
            paint = mCustomPaint;
        } else {
            paint = mPaint;
        }

        mPath.reset();

        if (mStyles.drawBackground) {
            mPathBackground.reset();
        }

        double diffY = maxY - minY;
        double diffX = maxX - minX;

        float graphHeight = graphView.getGraphContentHeight();
        float graphWidth = graphView.getGraphContentWidth();
        float graphLeft = graphView.getGraphContentLeft();
        float graphTop = graphView.getGraphContentTop();

        lastEndY = 0;
        lastEndX = 0;

        // needed to end the path for background
        double lastUsedEndX = 0;
        double lastUsedEndY = 0;
        float firstX = -1;
        float firstY = -1;
        float lastRenderedX = Float.NaN;
        int i = 0;
        float lastAnimationReferenceX = graphLeft;

        boolean sameXSkip = false;
        float minYOnSameX = 0f;
        float maxYOnSameX = 0f;

        while (values.hasNext()) {
            E value = values.next();


            double valY = value.getY() - minY;
            double ratY = valY / diffY;
            double y = graphHeight * ratY;

            double valueX = value.getX();
            double valX = valueX - minX;
            double ratX = valX / diffX;
            double x = graphWidth * ratX;

            double orgX = x;
            double orgY = y;

            if (i > 0) {
                // overdraw
                boolean isOverdrawY = false;
                boolean isOverdrawEndPoint = false;
                boolean skipDraw = false;

                if (x > graphWidth) { // end right
                    double b = ((graphWidth - lastEndX) * (y - lastEndY) / (x - lastEndX));
                    y = lastEndY + b;
                    x = graphWidth;
                    isOverdrawEndPoint = true;
                }
                if (y < 0) { // end bottom
                    // skip when previous and this point is out of bound
                    if (lastEndY < 0) {
                        skipDraw = true;
                    } else {
                        double b = ((0 - lastEndY) * (x - lastEndX) / (y - lastEndY));
                        x = lastEndX + b;
                    }
                    y = 0;
                    isOverdrawY = isOverdrawEndPoint = true;
                }
                if (y > graphHeight) { // end top
                    // skip when previous and this point is out of bound
                    if (lastEndY > graphHeight) {
                        skipDraw = true;
                    } else {
                        double b = ((graphHeight - lastEndY) * (x - lastEndX) / (y - lastEndY));
                        x = lastEndX + b;
                    }
                    y = graphHeight;
                    isOverdrawY = isOverdrawEndPoint = true;
                }
                if (lastEndX < 0) { // start left
                    double b = ((0 - x) * (y - lastEndY) / (lastEndX - x));
                    lastEndY = y - b;
                    lastEndX = 0;
                }

                // we need to save the X before it will be corrected when overdraw y
                float orgStartX = (float) lastEndX + (graphLeft + 1);

                if (lastEndY < 0) { // start bottom
                    if (!skipDraw) {
                        double b = ((0 - y) * (x - lastEndX) / (lastEndY - y));
                        lastEndX = x - b;
                    }
                    lastEndY = 0;
                    isOverdrawY = true;
                }
                if (lastEndY > graphHeight) { // start top
                    // skip when previous and this point is out of bound
                    if (!skipDraw) {
                        double b = ((graphHeight - y) * (x - lastEndX) / (lastEndY - y));
                        lastEndX = x - b;
                    }
                    lastEndY = graphHeight;
                    isOverdrawY = true;
                }

                float startX = (float) lastEndX + (graphLeft + 1);
                float startY = (float) (graphTop - lastEndY) + graphHeight;
                float endX = (float) x + (graphLeft + 1);
                float endY = (float) (graphTop - y) + graphHeight;
                float startXAnimated = startX;
                float endXAnimated = endX;

                if (endX < startX) {
                    // dont draw from right to left
                    skipDraw = true;
                }

                // NaN can happen when previous and current value is out of y bounds
                if (!skipDraw && !Float.isNaN(startY) && !Float.isNaN(endY)) {
                    // animation
                    if (mAnimated) {
                        if ((Double.isNaN(mLastAnimatedValue) || mLastAnimatedValue < valueX)) {
                            long currentTime = System.currentTimeMillis();
                            if (mAnimationStart == 0) {
                                // start animation
                                mAnimationStart = currentTime;
                                mAnimationStartFrameNo = 0;
                            } else {
                                // anti-lag: wait a few frames
                                if (mAnimationStartFrameNo < 15) {
                                    // second time
                                    mAnimationStart = currentTime;
                                    mAnimationStartFrameNo++;
                                }
                            }
                            float timeFactor = (float) (currentTime - mAnimationStart) / ANIMATION_DURATION;
                            float factor = mAnimationInterpolator.getInterpolation(timeFactor);
                            if (timeFactor <= 1.0) {
                                startXAnimated = (startX - lastAnimationReferenceX) * factor + lastAnimationReferenceX;
                                startXAnimated = Math.max(startXAnimated, lastAnimationReferenceX);
                                endXAnimated = (endX - lastAnimationReferenceX) * factor + lastAnimationReferenceX;
                                ViewCompat.postInvalidateOnAnimation(graphView);
                            } else {
                                // animation finished
                                mLastAnimatedValue = valueX;
                            }
                        } else {
                            lastAnimationReferenceX = endX;
                        }
                    }

                    // draw data point
                    if (!isOverdrawEndPoint) {
                        if (mStyles.drawDataPoints) {
                            // draw first datapoint
                            Paint.Style prevStyle = paint.getStyle();
                            paint.setStyle(Paint.Style.FILL);
                            canvas.drawCircle(endXAnimated, endY, mStyles.dataPointsRadius, paint);
                            paint.setStyle(prevStyle);
                        }
                        registerDataPoint(endX, endY, value);
                    }

                    if (mDrawAsPath) {
                        mPath.moveTo(startXAnimated, startY);
                    }
                    // performance opt.
                    if (Float.isNaN(lastRenderedX) || Math.abs(endX - lastRenderedX) > .3f) {
                        if (mDrawAsPath) {
                            mPath.lineTo(endXAnimated, endY);
                        } else {
                            // draw vertical lines that were skipped
                            if (sameXSkip) {
                                sameXSkip = false;
                                renderLine(canvas, new float[]{lastRenderedX, minYOnSameX, lastRenderedX, maxYOnSameX}, paint);
                            }
                            renderLine(canvas, new float[]{startXAnimated, startY, endXAnimated, endY}, paint);
                        }
                        lastRenderedX = endX;
                    } else {
                        // rendering on same x position
                        // save min+max y position and draw it as line
                        if (sameXSkip) {
                            minYOnSameX = Math.min(minYOnSameX, endY);
                            maxYOnSameX = Math.max(maxYOnSameX, endY);
                        } else {
                            // first
                            sameXSkip = true;
                            minYOnSameX = Math.min(startY, endY);
                            maxYOnSameX = Math.max(startY, endY);
                        }
                    }

                }

                if (mStyles.drawBackground) {
                    if (isOverdrawY) {
                        // start draw original x
                        if (firstX == -1) {
                            firstX = orgStartX;
                            firstY = startY;
                            mPathBackground.moveTo(orgStartX, startY);
                        }
                        // from original start to new start
                        mPathBackground.lineTo(startXAnimated, startY);
                    }
                    if (firstX == -1) {
                        firstX = startXAnimated;
                        firstY = startY;
                        mPathBackground.moveTo(startXAnimated, startY);
                    }
                    mPathBackground.lineTo(startXAnimated, startY);
                    mPathBackground.lineTo(endXAnimated, endY);
                }

                lastUsedEndX = endXAnimated;
                lastUsedEndY = endY;
            } else if (mStyles.drawDataPoints) {
                //fix: last value not drawn as datapoint. Draw first point here, and then on every step the end values (above)
                float first_X = (float) x + (graphLeft + 1);
                float first_Y = (float) (graphTop - y) + graphHeight;

                if (first_X >= graphLeft && first_Y <= (graphTop + graphHeight)) {
                    if (mAnimated && (Double.isNaN(mLastAnimatedValue) || mLastAnimatedValue < valueX)) {
                        long currentTime = System.currentTimeMillis();
                        if (mAnimationStart == 0) {
                            // start animation
                            mAnimationStart = currentTime;
                        }
                        float timeFactor = (float) (currentTime - mAnimationStart) / ANIMATION_DURATION;
                        float factor = mAnimationInterpolator.getInterpolation(timeFactor);
                        if (timeFactor <= 1.0) {
                            first_X = (first_X - lastAnimationReferenceX) * factor + lastAnimationReferenceX;
                            ViewCompat.postInvalidateOnAnimation(graphView);
                        } else {
                            // animation finished
                            mLastAnimatedValue = valueX;
                        }
                    }


                    Paint.Style prevStyle = paint.getStyle();
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(first_X, first_Y, mStyles.dataPointsRadius, paint);
                    paint.setStyle(prevStyle);
                    registerDataPoint(first_X, first_Y, value);
                }
            }
            lastEndY = orgY;
            lastEndX = orgX;
            i++;
        }

        if (mDrawAsPath) {
            // draw at the end
            canvas.drawPath(mPath, paint);
        }

        if (mStyles.drawBackground && firstX != -1) {
            // end / close path
            if (lastUsedEndY != graphHeight + graphTop) {
                // dont draw line to same point, otherwise the path is completely broken
                mPathBackground.lineTo((float) lastUsedEndX, graphHeight + graphTop);
            }
            mPathBackground.lineTo(firstX, graphHeight + graphTop);
            if (firstY != graphHeight + graphTop) {
                // dont draw line to same point, otherwise the path is completely broken
                mPathBackground.lineTo(firstX, firstY);
            }
            //mPathBackground.close();
            canvas.drawPath(mPathBackground, mPaintBackground);
        }
    }

    
    private void renderLine(Canvas canvas, float[] pts, Paint paint) {
        if (pts.length == 4 && pts[0] == pts[2] && pts[1] == pts[3]) {
            // avoid zero length lines, to makes troubles on some devices
            // see https://github.com/appsthatmatter/GraphView/issues/499
            return;
        }
        canvas.drawLines(pts, paint);
    }

    private Context getContext() {
        return context;
    }

    public int getThickness() {
        return mStyles.thickness;
    }

    
    public void setThickness(int thickness) {
        mStyles.thickness = thickness;
    }

    
    public boolean isDrawBackground() {
        return mStyles.drawBackground;
    }

    
    public void setDrawBackground(boolean drawBackground) {
        mStyles.drawBackground = drawBackground;
    }

    
    public boolean isDrawDataPoints() {
        return mStyles.drawDataPoints;
    }

    
    public void setDrawDataPoints(boolean drawDataPoints) {
        mStyles.drawDataPoints = drawDataPoints;
    }

    
    public float getDataPointsRadius() {
        return mStyles.dataPointsRadius;
    }

    
    public void setDataPointsRadius(float dataPointsRadius) {
        mStyles.dataPointsRadius = dataPointsRadius;
    }
    
    public void setCustomPaint(Paint customPaint) {
        this.mCustomPaint = customPaint;
    }

    
    public void setAnimated(boolean animated) {
        this.mAnimated = animated;
    }
    
    public boolean isDrawAsPath() {
        return mDrawAsPath;
    }

    
    public void setDrawAsPath(boolean mDrawAsPath) {
        this.mDrawAsPath = mDrawAsPath;
    }

    
    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints, boolean silent) {
        if (!isAnimationActive()) {
            mAnimationStart = 0;
        }
        super.appendData(dataPoint, scrollToEnd, maxDataPoints, silent);
    }

    
    private boolean isAnimationActive() {
        if (mAnimated) {
            long curr = System.currentTimeMillis();
            return curr - mAnimationStart <= ANIMATION_DURATION;
        }
        return false;
    }

    @Override
    public void drawSelection(GraphView graphView, Canvas canvas, boolean b, DataPointInterface value) {
        double spanX = graphView.getViewport().getMaxX(false) - graphView.getViewport().getMinX(false);
        double spanXPixel = graphView.getGraphContentWidth();

        double spanY = graphView.getViewport().getMaxY(false) - graphView.getViewport().getMinY(false);
        double spanYPixel = graphView.getGraphContentHeight();

        double pointX = (value.getX() - graphView.getViewport().getMinX(false)) * spanXPixel / spanX;
        pointX += graphView.getGraphContentLeft();

        double pointY = (value.getY() - graphView.getViewport().getMinY(false)) * spanYPixel / spanY;
        pointY = graphView.getGraphContentTop() + spanYPixel - pointY;

        // border
        canvas.drawCircle((float) pointX, (float) pointY, 30f, mSelectionPaint);

        // fill
        Paint.Style prevStyle = mPaint.getStyle();
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawCircle((float) pointX, (float) pointY, 23f, mPaint);
        mPaint.setStyle(prevStyle);
    }
}
