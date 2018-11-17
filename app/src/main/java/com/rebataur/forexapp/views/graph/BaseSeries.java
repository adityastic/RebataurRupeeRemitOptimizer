
package com.rebataur.forexapp.views.graph;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;


public abstract class BaseSeries<E extends DataPointInterface> implements Series<E> {

    final private List<E> mData = new ArrayList<E>();


    private Map<PointF, E> mDataPoints = new HashMap<PointF, E>();


    private String mTitle;


    private int mColor = 0xff0077cc;


    private double mLowestYCache = Double.NaN;


    private double mHighestYCache = Double.NaN;


    protected OnDataPointTapListener mOnDataPointTapListener;


    private List<WeakReference<GraphView>> mGraphViews;


    public BaseSeries() {
        mGraphViews = new ArrayList<>();
    }


    public BaseSeries(E[] data) {
        mGraphViews = new ArrayList<>();
        for (E d : data) {
            mData.add(d);
        }
        checkValueOrder(null);
    }


    public double getLowestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(0).getX();
    }


    public double getHighestValueX() {
        if (mData.isEmpty()) return 0d;
        return mData.get(mData.size() - 1).getX();
    }


    public double getLowestValueY() {
        if (mData.isEmpty()) return 0d;
        if (!Double.isNaN(mLowestYCache)) {
            return mLowestYCache;
        }
        double l = mData.get(0).getY();
        for (int i = 1; i < mData.size(); i++) {
            double c = mData.get(i).getY();
            if (l > c) {
                l = c;
            }
        }
        return mLowestYCache = l;
    }


    public double getHighestValueY() {
        if (mData.isEmpty()) return 0d;
        if (!Double.isNaN(mHighestYCache)) {
            return mHighestYCache;
        }
        double h = mData.get(0).getY();
        for (int i = 1; i < mData.size(); i++) {
            double c = mData.get(i).getY();
            if (h < c) {
                h = c;
            }
        }
        return mHighestYCache = h;
    }


    @Override
    public Iterator<E> getValues(final double from, final double until) {
        if (from <= getLowestValueX() && until >= getHighestValueX()) {
            Log.e("Default", "YES");
            return mData.iterator();
        } else {
            return new Iterator<E>() {
                Iterator<E> org = mData.iterator();
                E nextValue = null;
                E nextNextValue = null;
                boolean plusOne = true;

                {
                    // go to first
                    boolean found = false;
                    E prevValue = null;
                    if (org.hasNext()) {
                        prevValue = org.next();
                    }
                    if (prevValue != null) {
                        if (prevValue.getX() >= from) {
                            nextValue = prevValue;
                            found = true;
                        } else {
                            while (org.hasNext()) {
                                nextValue = org.next();
                                if (nextValue.getX() >= from) {
                                    found = true;
                                    nextNextValue = nextValue;
                                    nextValue = prevValue;
                                    break;
                                }
                                prevValue = nextValue;
                            }
                        }
                    }
                    if (!found) {
                        nextValue = null;
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public E next() {
                    if (hasNext()) {
                        E r = nextValue;
                        if (r.getX() > until) {
                            plusOne = false;
                        }
                        if (nextNextValue != null) {
                            nextValue = nextNextValue;
                            nextNextValue = null;
                        } else if (org.hasNext()) nextValue = org.next();
                        else nextValue = null;
                        return r;
                    } else {
                        throw new NoSuchElementException();
                    }
                }

                @Override
                public boolean hasNext() {
                    return nextValue != null && (nextValue.getX() <= until || plusOne);
                }
            };
        }
    }


    public String getTitle() {
        return mTitle;
    }


    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }


    public int getColor() {
        return mColor;
    }


    public void setColor(int mColor) {
        this.mColor = mColor;
    }


    public void setOnDataPointTapListener(OnDataPointTapListener l) {
        this.mOnDataPointTapListener = l;
    }


    @Override
    public void onTap(float x, float y) {
        if (mOnDataPointTapListener != null) {
            E p = findDataPoint(x, y);
            if (p != null) {
                mOnDataPointTapListener.onTap(this, p);
            }
        }
    }


    protected E findDataPoint(float x, float y) {
        float shortestDistance = Float.NaN;
        E shortest = null;
        for (Map.Entry<PointF, E> entry : mDataPoints.entrySet()) {
            float x1 = entry.getKey().x;
            float y1 = entry.getKey().y;
            float x2 = x;
            float y2 = y;

            float distance = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
            if (shortest == null || distance < shortestDistance) {
                shortestDistance = distance;
                shortest = entry.getValue();
            }
        }
        if (shortest != null) {
            if (shortestDistance < 120) {
                return shortest;
            }
        }
        return null;
    }

    public E findDataPointAtX(float x) {
        float shortestDistance = Float.NaN;
        E shortest = null;
        for (Map.Entry<PointF, E> entry : mDataPoints.entrySet()) {
            float x1 = entry.getKey().x;
            float x2 = x;

            float distance = Math.abs(x1 - x2);
            if (shortest == null || distance < shortestDistance) {
                shortestDistance = distance;
                shortest = entry.getValue();
            }
        }
        if (shortest != null) {
            if (shortestDistance < 200) {
                return shortest;
            }
        }
        return null;
    }


    protected void registerDataPoint(float x, float y, E dp) {
        // performance
        // TODO maybe invalidate after setting the listener
        if (mOnDataPointTapListener != null) {
            mDataPoints.put(new PointF(x, y), dp);
        }
    }

    protected void resetDataPoints() {
        mDataPoints.clear();
    }


    public void resetData(E[] data) {
        mData.clear();
        for (E d : data) {
            mData.add(d);
        }
        checkValueOrder(null);

        mHighestYCache = mLowestYCache = Double.NaN;

        // update graphview
        for (WeakReference<GraphView> gv : mGraphViews) {
            if (gv != null && gv.get() != null) {
                gv.get().onDataChanged(true, false);
            }
        }
    }


    @Override
    public void onGraphViewAttached(GraphView graphView) {
        mGraphViews.add(new WeakReference<>(graphView));
    }


    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints, boolean silent) {
        checkValueOrder(dataPoint);

        if (!mData.isEmpty() && dataPoint.getX() < mData.get(mData.size() - 1).getX()) {
            throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
        }
        synchronized (mData) {
            int curDataCount = mData.size();
            if (curDataCount < maxDataPoints) {
                // enough space
                mData.add(dataPoint);
            } else {
                // we have to trim one data
                mData.remove(0);
                mData.add(dataPoint);
            }

            // update lowest/highest cache
            double dataPointY = dataPoint.getY();
            if (!Double.isNaN(mHighestYCache)) {
                if (dataPointY > mHighestYCache) {
                    mHighestYCache = dataPointY;
                }
            }
            if (!Double.isNaN(mLowestYCache)) {
                if (dataPointY < mLowestYCache) {
                    mLowestYCache = dataPointY;
                }
            }

        }

        if (!silent) {
            // recalc the labels when it was the first data
            boolean keepLabels = mData.size() != 1;

            // update linked graph views
            // update graphview
            for (WeakReference<GraphView> gv : mGraphViews) {
                if (gv != null && gv.get() != null) {
                    if (scrollToEnd) {
                        gv.get().getViewport().scrollToEnd();
                    } else {
                        gv.get().onDataChanged(keepLabels, scrollToEnd);
                    }
                }
            }
        }
    }


    public void appendData(E dataPoint, boolean scrollToEnd, int maxDataPoints) {
        appendData(dataPoint, scrollToEnd, maxDataPoints, false);
    }


    @Override
    public boolean isEmpty() {
        return mData.isEmpty();
    }


    protected void checkValueOrder(DataPointInterface onlyLast) {
        if (mData.size() > 1) {
            if (onlyLast != null) {
                // only check last
                if (onlyLast.getX() < mData.get(mData.size() - 1).getX()) {
                    throw new IllegalArgumentException("new x-value must be greater then the last value. x-values has to be ordered in ASC.");
                }
            } else {
                double lx = mData.get(0).getX();

                for (int i = 1; i < mData.size(); i++) {
                    if (mData.get(i).getX() != Double.NaN) {
                        if (lx > mData.get(i).getX()) {
                            throw new IllegalArgumentException("The order of the values is not correct. X-Values have to be ordered ASC. First the lowest x value and at least the highest x value.");
                        }
                        lx = mData.get(i).getX();
                    }
                }
            }
        }
    }

    public abstract void drawSelection(GraphView mGraphView, Canvas canvas, boolean b, DataPointInterface value);

    @Override
    public void clearReference(GraphView graphView) {
        // find and remove
        for (WeakReference<GraphView> view : mGraphViews) {
            if (view != null && view.get() != null && view.get() == graphView) {
                mGraphViews.remove(view);
                break;
            }
        }
    }
}
