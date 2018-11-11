
package com.rebataur.forexapp.views.graph;

import android.graphics.RectF;


public class RectD {
    public double left;
    public double right;
    public double top;
    public double bottom;

    public RectD() {
    }

    public RectD(double lLeft, double lTop, double lRight, double lBottom) {
        set(lLeft, lTop, lRight, lBottom);
    }

    public double width() {
        return right-left;
    }

    public double height() {
        return bottom-top;
    }

    public void set(double lLeft, double lTop, double lRight, double lBottom) {
        left = lLeft;
        right = lRight;
        top = lTop;
        bottom = lBottom;
    }

    public RectF toRectF() {
        return new RectF((float) left, (float) top, (float) right, (float) bottom);
    }
}
