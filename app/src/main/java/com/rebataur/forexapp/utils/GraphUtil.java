package com.rebataur.forexapp.utils;

import android.util.Log;
import com.rebataur.forexapp.data.GraphPlotData;

import java.util.ArrayList;
import java.util.Date;

public class GraphUtil {

    public ArrayList<GraphPlotData> graphMultiply(ArrayList<GraphPlotData> list) {
        int points;

        if (list.size() >= 88) {
            return list;
        } else
            points = (int) Math.ceil((double) 90 / (list.size() - 1));

        Log.e("Ceiling", points + "");

        ArrayList<GraphPlotData> listNew = new ArrayList<>();

        for (int i = 0; i < list.size() - 1; i++) {
            Line temp = new Line(
                    list.get(i).getIndex(),
                    list.get(i).getCurrency(),
                    list.get(i + 1).getIndex(),
                    list.get(i + 1).getCurrency()
            );
            temp.calculateSlope();
            temp.calculateIntercept();
            listNew.addAll(temp.createPoints(points, list.get(i).getDate(), (i * points)+1));
        }

//        for (int i = 0; i < listNew.size(); i++) {
//            Log.e("Data "+i,listNew.get(i).getCurrency()+"");
//        }

        return listNew;
    }

    class Line {
        double x1, y1, x2, y2, slope, intercept;

        Line(double x1, double y1, double x2, double y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        private void calculateSlope() {
            slope = (y2 - y1) / (x2 - x1);
        }

        private void calculateIntercept() {
            intercept = y1 - (slope * x1);
        }

        private ArrayList<GraphPlotData> createPoints(int points, Date d, int iStart) {
            ArrayList<GraphPlotData> list = new ArrayList<>();
            double unit = (x2 - x1) / points;
            for (int i = 0; i < points; i++) {
                double valX = x1 + (unit * i);
                double valY = (slope * valX) + intercept;

                list.add(new GraphPlotData(d, valY, i + iStart));
            }
            return list;
        }
    }
}
