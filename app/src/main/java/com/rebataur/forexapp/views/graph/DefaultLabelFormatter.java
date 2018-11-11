
package com.rebataur.forexapp.views.graph;

import java.text.NumberFormat;


public class DefaultLabelFormatter implements LabelFormatter {
    
    protected NumberFormat[] mNumberFormatter = new NumberFormat[2];

    
    protected Viewport mViewport;

    
    public DefaultLabelFormatter() {
    }

    
    public DefaultLabelFormatter(NumberFormat xFormat, NumberFormat yFormat) {
        mNumberFormatter[0] = yFormat;
        mNumberFormatter[1] = xFormat;
    }

    
    @Override
    public void setViewport(Viewport viewport) {
        mViewport = viewport;
    }

    
    public String formatLabel(double value, boolean isValueX) {
        int i = isValueX ? 1 : 0;
        if (mNumberFormatter[i] == null) {
            mNumberFormatter[i] = NumberFormat.getNumberInstance();
            double highestvalue = isValueX ? mViewport.getMaxX(false) : mViewport.getMaxY(false);
            double lowestvalue = isValueX ? mViewport.getMinX(false) : mViewport.getMinY(false);
            if (highestvalue - lowestvalue < 0.1) {
                mNumberFormatter[i].setMaximumFractionDigits(6);
            } else if (highestvalue - lowestvalue < 1) {
                mNumberFormatter[i].setMaximumFractionDigits(4);
            } else if (highestvalue - lowestvalue < 20) {
                mNumberFormatter[i].setMaximumFractionDigits(3);
            } else if (highestvalue - lowestvalue < 100) {
                mNumberFormatter[i].setMaximumFractionDigits(1);
            } else {
                mNumberFormatter[i].setMaximumFractionDigits(0);
            }
        }
        return mNumberFormatter[i].format(value);
    }
}
