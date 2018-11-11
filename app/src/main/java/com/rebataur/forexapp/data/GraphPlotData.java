package com.rebataur.forexapp.data;

import java.util.Date;

public class GraphPlotData {
    Date date;
    double currency;
    int index;

    public GraphPlotData(Date date, double currency, int index) {
        this.date = date;
        this.currency = currency;
        this.index = index;
    }

    public Date getDate() {
        return date;
    }

    public double getCurrency() {
        return currency;
    }

    public int getIndex() {
        return index;
    }
}
