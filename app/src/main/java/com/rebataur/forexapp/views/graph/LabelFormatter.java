
package com.rebataur.forexapp.views.graph;

public interface LabelFormatter {
    
    public String formatLabel(double value, boolean isValueX);

    
    public void setViewport(Viewport viewport);
}
