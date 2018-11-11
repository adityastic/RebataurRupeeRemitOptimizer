package com.rebataur.forexapp.views.graph;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;


public class SecondScale {
    
    protected final GraphView mGraph;

    
    protected List<Series> mSeries;

    
    private boolean mYAxisBoundsManual = true;

    
    protected RectD mCompleteRange = new RectD();

    protected RectD mCurrentViewport = new RectD();

    
    protected LabelFormatter mLabelFormatter;

    protected double mReferenceY = Double.NaN;

    
    private Paint mPaintAxisTitle;

    
    private String mVerticalAxisTitle;

    
    public float mVerticalAxisTitleTextSize;

    
    public int mVerticalAxisTitleColor;

    
    SecondScale(GraphView graph) {
        mGraph = graph;
        mSeries = new ArrayList<Series>();
        mLabelFormatter = new DefaultLabelFormatter();
        mLabelFormatter.setViewport(mGraph.getViewport());
    }

    
    public void addSeries(Series s) {
        s.onGraphViewAttached(mGraph);
        mSeries.add(s);
        mGraph.onDataChanged(false, false);
    }

    
    public void setMinY(double d) {
        mReferenceY = d;
        mCurrentViewport.bottom = d;
    }

    
    public void setMaxY(double d) {
        mCurrentViewport.top = d;
    }

    
    public List<Series> getSeries() {
        return mSeries;
    }

    
    public double getMinY(boolean completeRange) {
        return completeRange ? mCompleteRange.bottom : mCurrentViewport.bottom;
    }

    
    public double getMaxY(boolean completeRange) {
        return completeRange ? mCompleteRange.top : mCurrentViewport.top;
    }

    
    public boolean isYAxisBoundsManual() {
        return mYAxisBoundsManual;
    }

    
    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
    }

    
    public void setLabelFormatter(LabelFormatter formatter) {
        mLabelFormatter = formatter;
        mLabelFormatter.setViewport(mGraph.getViewport());
    }

    
    public void removeAllSeries() {
        mSeries.clear();
        mGraph.onDataChanged(false, false);
    }

    
    public void removeSeries(Series series) {
        mSeries.remove(series);
        mGraph.onDataChanged(false, false);
    }

    
    public void calcCompleteRange() {
        List<Series> series = getSeries();
        mCompleteRange.set(0d, 0d, 0d, 0d);
        if (!series.isEmpty() && !series.get(0).isEmpty()) {
            double d = series.get(0).getLowestValueX();
            for (Series s : series) {
                if (!s.isEmpty() && d > s.getLowestValueX()) {
                    d = s.getLowestValueX();
                }
            }
            mCompleteRange.left = d;

            d = series.get(0).getHighestValueX();
            for (Series s : series) {
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
    }

    
    public String getVerticalAxisTitle() {
        return mVerticalAxisTitle;
    }

    
    public void setVerticalAxisTitle(String mVerticalAxisTitle) {
        if(mPaintAxisTitle==null) {
            mPaintAxisTitle = new Paint();
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            mPaintAxisTitle.setTextAlign(Paint.Align.CENTER);
        }
        this.mVerticalAxisTitle = mVerticalAxisTitle;
    }

    
    public float getVerticalAxisTitleTextSize() {
        if (getVerticalAxisTitle() == null || getVerticalAxisTitle().length() == 0) {
            return 0;
        }
        return mVerticalAxisTitleTextSize;
    }

    
    public void setVerticalAxisTitleTextSize(float verticalAxisTitleTextSize) {
        mVerticalAxisTitleTextSize = verticalAxisTitleTextSize;
    }

    
    public int getVerticalAxisTitleColor() {
        return mVerticalAxisTitleColor;
    }

    
    public void setVerticalAxisTitleColor(int verticalAxisTitleColor) {
        mVerticalAxisTitleColor = verticalAxisTitleColor;
    }

    
    protected void drawVerticalAxisTitle(Canvas canvas) {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getVerticalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            float x = canvas.getWidth() - getVerticalAxisTitleTextSize()/2;
            float y = canvas.getHeight() / 2;
            canvas.save();
            canvas.rotate(-90, x, y);
            canvas.drawText(mVerticalAxisTitle, x, y, mPaintAxisTitle);
            canvas.restore();
        }
    }
}
