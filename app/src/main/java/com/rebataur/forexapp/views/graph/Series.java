
package com.rebataur.forexapp.views.graph;

import android.graphics.Canvas;

import java.util.Iterator;

public interface Series<E extends DataPointInterface> {
    
    public double getLowestValueX();

    
    public double getHighestValueX();

    
    public double getLowestValueY();

    
    public double getHighestValueY();

    
    public Iterator<E> getValues(double from, double until);

    
    public void draw(GraphView graphView, Canvas canvas, boolean isSecondScale);

    
    public String getTitle();

    
    public int getColor();

    
    public void setOnDataPointTapListener(OnDataPointTapListener l);

    
    void onTap(float x, float y);

    
    void onGraphViewAttached(GraphView graphView);

    
    boolean isEmpty();

    
    void clearReference(GraphView graphView);
}
