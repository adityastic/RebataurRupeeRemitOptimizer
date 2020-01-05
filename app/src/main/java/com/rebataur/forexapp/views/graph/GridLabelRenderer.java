
package com.rebataur.forexapp.views.graph;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;

import java.util.LinkedHashMap;
import java.util.Map;


public class GridLabelRenderer {

    
    public enum VerticalLabelsVAlign {
        
        ABOVE,
        
        MID,
        
        BELOW
    }


    
    public final class Styles {
        
        public float textSize;

        
        public Paint.Align verticalLabelsAlign;

        
        public Paint.Align verticalLabelsSecondScaleAlign;

        
        public int verticalLabelsColor;

        
        public int verticalLabelsSecondScaleColor;

        
        public int horizontalLabelsColor;

        
        public int gridColor;

        
        public boolean highlightZeroLines;

        
        public int padding;

        
        public float verticalAxisTitleTextSize;

        
        public int verticalAxisTitleColor;

        
        public float horizontalAxisTitleTextSize;

        
        public int horizontalAxisTitleColor;
        
        
        public float horizontalLabelsAngle;

        
        boolean horizontalLabelsVisible;

        
        boolean verticalLabelsVisible;

        
        GridStyle gridStyle;

        
        int labelsSpace;

        
        VerticalLabelsVAlign verticalLabelsVAlign = VerticalLabelsVAlign.MID;
    }

    
    public enum GridStyle {
        
        BOTH,

        
        VERTICAL,

        
        HORIZONTAL,

        
        NONE;

        public boolean drawVertical() { return this == BOTH || this == VERTICAL && this != NONE; }
        public boolean drawHorizontal() { return this == BOTH || this == HORIZONTAL && this != NONE; }
    }

    
    protected Styles mStyles;

    
    private final GraphView mGraphView;

    
    private Map<Integer, Double> mStepsVertical;

    
    private Map<Integer, Double> mStepsVerticalSecondScale;

    
    private Map<Integer, Double> mStepsHorizontal;

    
    private Paint mPaintLine;

    
    private Paint mPaintLabel;

    
    private Paint mPaintAxisTitle;

    
    protected boolean mIsAdjusted;

    
    private Integer mLabelVerticalWidth;

    
    private boolean mLabelVerticalWidthFixed;

    
    private Integer mLabelVerticalHeight;

    
    private boolean mLabelHorizontalHeightFixed;

    
    private Integer mLabelVerticalSecondScaleWidth;

    
    private Integer mLabelVerticalSecondScaleHeight;

    
    private Integer mLabelHorizontalWidth;

    
    private Integer mLabelHorizontalHeight;

    
    private LabelFormatter mLabelFormatter;

    
    private String mHorizontalAxisTitle;

    
    private String mVerticalAxisTitle;

    
    private int mNumVerticalLabels;

    
    private int mNumHorizontalLabels;

    
    public void setSecondScaleLabelVerticalWidth(Integer newWidth) {
        mLabelVerticalSecondScaleWidth = newWidth;
    }

    
    private boolean mHumanRoundingY;

    
    private boolean mHumanRoundingX;

    
    public GridLabelRenderer(GraphView graphView) {
        mGraphView = graphView;
        setLabelFormatter(new DefaultLabelFormatter());
        mStyles = new Styles();
        resetStyles();
        mNumVerticalLabels = 5;
        mNumHorizontalLabels = 5;
        mHumanRoundingX = true;
        mHumanRoundingY = true;
    }

    
    @SuppressLint("ResourceType")
    public void resetStyles() {
        // get matching styles from theme
        TypedValue typedValue = new TypedValue();
        mGraphView.getContext().getTheme().resolveAttribute(android.R.attr.textAppearanceSmall, typedValue, true);

        int color1;
        int color2;
        int size;
        int size2;

        TypedArray array = null;
        try {
            array = mGraphView.getContext().obtainStyledAttributes(typedValue.data, new int[]{
                    android.R.attr.textColorPrimary
                    , android.R.attr.textColorSecondary
                    , android.R.attr.textSize
                    , android.R.attr.horizontalGap});
            color1 = array.getColor(0, Color.BLACK);
            color2 = array.getColor(1, Color.GRAY);
            size = array.getDimensionPixelSize(2, 20);
            size2 = array.getDimensionPixelSize(3, 20);
            array.recycle();
        } catch (Exception e) {
            color1 = Color.BLACK;
            color2 = Color.GRAY;
            size = 20;
            size2 = 20;
        }

        mStyles.verticalLabelsColor = color1;
        mStyles.verticalLabelsSecondScaleColor = color1;
        mStyles.horizontalLabelsColor = color1;
        mStyles.gridColor = color2;
        mStyles.textSize = size;
        mStyles.padding = size2;
        mStyles.labelsSpace = (int) mStyles.textSize/5;

        mStyles.verticalLabelsAlign = Paint.Align.RIGHT;
        mStyles.verticalLabelsSecondScaleAlign = Paint.Align.LEFT;
        mStyles.highlightZeroLines = true;

        mStyles.verticalAxisTitleColor = mStyles.verticalLabelsColor;
        mStyles.horizontalAxisTitleColor = mStyles.horizontalLabelsColor;
        mStyles.verticalAxisTitleTextSize = mStyles.textSize;
        mStyles.horizontalAxisTitleTextSize = mStyles.textSize;

        mStyles.horizontalLabelsVisible = true;
        mStyles.verticalLabelsVisible = true;
        
        mStyles.horizontalLabelsAngle = 0f;

        mStyles.gridStyle = GridStyle.BOTH;
        reloadStyles();
    }

    
    public void reloadStyles() {
        mPaintLine = new Paint();
        mPaintLine.setColor(mStyles.gridColor);
        mPaintLine.setStrokeWidth(0);

        mPaintLabel = new Paint();
        mPaintLabel.setTextSize(getTextSize());
        mPaintLabel.setAntiAlias(true);

        mPaintAxisTitle = new Paint();
        mPaintAxisTitle.setTextSize(getTextSize());
        mPaintAxisTitle.setTextAlign(Paint.Align.CENTER);
    }

    
    public boolean isHumanRoundingX() {
        return mHumanRoundingX;
    }

    
    public boolean isHumanRoundingY() {
        return mHumanRoundingY;
    }

    
    public void setHumanRounding(boolean humanRoundingX, boolean humanRoundingY) {
        this.mHumanRoundingX = humanRoundingX;
        this.mHumanRoundingY = humanRoundingY;
    }

    
    public void setHumanRounding(boolean humanRoundingBoth) {
        this.mHumanRoundingX = humanRoundingBoth;
        this.mHumanRoundingY = humanRoundingBoth;
    }

    
    public float getTextSize() {
        return mStyles.textSize;
    }

    
    public int getVerticalLabelsColor() {
        return mStyles.verticalLabelsColor;
    }

    
    public Paint.Align getVerticalLabelsAlign() {
        return mStyles.verticalLabelsAlign;
    }

    
    public int getHorizontalLabelsColor() {
        return mStyles.horizontalLabelsColor;
    }
    
    
    public float getHorizontalLabelsAngle() {
        return mStyles.horizontalLabelsAngle;
    }

    
    public void invalidate(boolean keepLabelsSize, boolean keepViewport) {
        if (!keepViewport) {
            mIsAdjusted = false;
        }
        if (!keepLabelsSize) {
            if (!mLabelVerticalWidthFixed) {
                mLabelVerticalWidth = null;
            }
            mLabelVerticalHeight = null;
            mLabelVerticalSecondScaleWidth = null;
            mLabelVerticalSecondScaleHeight = null;
        }
        //reloadStyles();
    }

    
    protected boolean adjustVerticalSecondScale() {
        if (mLabelHorizontalHeight == null) {
            return false;
        }
        if (mGraphView.mSecondScale == null) {
            return true;
        }

        double minY = mGraphView.mSecondScale.getMinY(false);
        double maxY = mGraphView.mSecondScale.getMaxY(false);

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        if (mGraphView.mSecondScale.isYAxisBoundsManual()) {
            // split range into equal steps
            exactSteps = (maxY - minY) / (numVerticalLabels - 1);

            // round because of floating error
            exactSteps = Math.round(exactSteps * 1000000d) / 1000000d;
        } else {
            // TODO auto adjusting
            throw new IllegalStateException("Not yet implemented");
        }

        if (mStepsVerticalSecondScale != null && mStepsVerticalSecondScale.size() > 1) {
            // else choose other nice steps that previous
            // steps are included (divide to have more, or multiplicate to have less)

            double d1 = 0, d2 = 0;
            int i = 0;
            for (Double v : mStepsVerticalSecondScale.values()) {
                if (i == 0) {
                    d1 = v;
                } else {
                    d2 = v;
                    break;
                }
                i++;
            }
            double oldSteps = d2 - d1;
            if (oldSteps > 0) {
                double newSteps = Double.NaN;

                if (oldSteps > exactSteps) {
                    newSteps = oldSteps / 2;
                } else if (oldSteps < exactSteps) {
                    newSteps = oldSteps * 2;
                }

                // only if there wont be more than numLabels
                // and newSteps will be better than oldSteps
                int numStepsOld = (int) ((maxY - minY) / oldSteps);
                int numStepsNew = (int) ((maxY - minY) / newSteps);

                boolean shouldChange;

                // avoid switching between 2 steps
                if (numStepsOld <= numVerticalLabels && numStepsNew <= numVerticalLabels) {
                    // both are possible
                    // only the new if it hows more labels
                    shouldChange = numStepsNew > numStepsOld;
                } else {
                    shouldChange = true;
                }

                if (newSteps != Double.NaN && shouldChange && numStepsNew <= numVerticalLabels) {
                    exactSteps = newSteps;
                } else {
                    // try to stay to the old steps
                    exactSteps = oldSteps;
                }
            }
        } else {
            // first time
        }

        // find the first data point that is relevant to display
        // starting from 1st datapoint so that the steps have nice numbers
        // goal is to start with the minY or 1 step before
        newMinY = mGraphView.getSecondScale().mReferenceY;
        // must be down-rounded
        double count = Math.floor((minY-newMinY)/exactSteps);
        newMinY = count*exactSteps + newMinY;

        // it can happen that we need to add some more labels to fill the complete screen
        numVerticalLabels = (int) ((mGraphView.getSecondScale().mCurrentViewport.height()*-1 / exactSteps)) + 2;

        // ensure that the value is valid (minimum 2)
        // see https://github.com/appsthatmatter/GraphView/issues/520
        numVerticalLabels = Math.max(numVerticalLabels, 2);

        if (mStepsVerticalSecondScale != null) {
            mStepsVerticalSecondScale.clear();
        } else {
            mStepsVerticalSecondScale = new LinkedHashMap<>(numVerticalLabels);
        }

        int height = mGraphView.getGraphContentHeight();
        // convert data-y to pixel-y in current viewport
        double pixelPerData = height / mGraphView.getSecondScale().mCurrentViewport.height()*-1;

        for (int i = 0; i < numVerticalLabels; i++) {
            // dont draw if it is top of visible screen
            if (newMinY + (i * exactSteps) > mGraphView.getSecondScale().mCurrentViewport.top) {
                continue;
            }
            // dont draw if it is below of visible screen
            if (newMinY + (i * exactSteps) < mGraphView.getSecondScale().mCurrentViewport.bottom) {
                continue;
            }


            // where is the data point on the current screen
            double dataPointPos = newMinY + (i * exactSteps);
            double relativeToCurrentViewport = dataPointPos - mGraphView.getSecondScale().mCurrentViewport.bottom;

            double pixelPos = relativeToCurrentViewport * pixelPerData;
            mStepsVerticalSecondScale.put((int) pixelPos, dataPointPos);
        }

        return true;
    }

    
    protected boolean adjustVertical(boolean changeBounds) {
        if (mLabelHorizontalHeight == null) {
            return false;
        }

        double minY = mGraphView.getViewport().getMinY(false);
        double maxY = mGraphView.getViewport().getMaxY(false);

        if (minY == maxY) {
            return false;
        }

        // TODO find the number of labels
        int numVerticalLabels = mNumVerticalLabels;

        double newMinY;
        double exactSteps;

        // split range into equal steps
        exactSteps = (maxY - minY) / (numVerticalLabels - 1);

        // round because of floating error
        exactSteps = Math.round(exactSteps * 1000000d) / 1000000d;

        // smallest viewport
        if (exactSteps == 0d) {
            exactSteps = 0.0000001d;
            maxY = minY + exactSteps * (numVerticalLabels - 1);
        }

        // human rounding to have nice numbers (1, 2, 5, ...)
        if (isHumanRoundingY()) {
            exactSteps = humanRound(exactSteps, changeBounds);
        } else if (mStepsVertical != null && mStepsVertical.size() > 1) {
            // else choose other nice steps that previous
            // steps are included (divide to have more, or multiplicate to have less)

            double d1 = 0, d2 = 0;
            int i = 0;
            for (Double v : mStepsVertical.values()) {
                if (i == 0) {
                    d1 = v;
                } else {
                    d2 = v;
                    break;
                }
                i++;
            }
            double oldSteps = d2 - d1;
            if (oldSteps > 0) {
                double newSteps = Double.NaN;

                if (oldSteps > exactSteps) {
                    newSteps = oldSteps / 2;
                } else if (oldSteps < exactSteps) {
                    newSteps = oldSteps * 2;
                }

                // only if there wont be more than numLabels
                // and newSteps will be better than oldSteps
                int numStepsOld = (int) ((maxY - minY) / oldSteps);
                int numStepsNew = (int) ((maxY - minY) / newSteps);

                boolean shouldChange;

                // avoid switching between 2 steps
                if (numStepsOld <= numVerticalLabels && numStepsNew <= numVerticalLabels) {
                    // both are possible
                    // only the new if it hows more labels
                    shouldChange = numStepsNew > numStepsOld;
                } else {
                    shouldChange = true;
                }

                if (newSteps != Double.NaN && shouldChange && numStepsNew <= numVerticalLabels) {
                    exactSteps = newSteps;
                } else {
                    // try to stay to the old steps
                    exactSteps = oldSteps;
                }
            }
        } else {
            // first time
        }

        // find the first data point that is relevant to display
        // starting from 1st datapoint so that the steps have nice numbers
        // goal is to start with the minX or 1 step before
        newMinY = mGraphView.getViewport().getReferenceY();
        // must be down-rounded
        double count = Math.floor((minY-newMinY)/exactSteps);
        newMinY = count*exactSteps + newMinY;

        // now we have our labels bounds
        if (changeBounds) {
            mGraphView.getViewport().setMinY(newMinY);
            mGraphView.getViewport().setMaxY(Math.max(maxY, newMinY + (numVerticalLabels - 1) * exactSteps));
            mGraphView.getViewport().mYAxisBoundsStatus = Viewport.AxisBoundsStatus.AUTO_ADJUSTED;
        }

        // it can happen that we need to add some more labels to fill the complete screen
        numVerticalLabels = (int) ((mGraphView.getViewport().mCurrentViewport.height()*-1 / exactSteps)) + 2;

        if (mStepsVertical != null) {
            mStepsVertical.clear();
        } else {
            mStepsVertical = new LinkedHashMap<>((int) numVerticalLabels);
        }

        int height = mGraphView.getGraphContentHeight();
        // convert data-y to pixel-y in current viewport
        double pixelPerData = height / mGraphView.getViewport().mCurrentViewport.height()*-1;

        for (int i = 0; i < numVerticalLabels; i++) {
            // dont draw if it is top of visible screen
            if (newMinY + (i * exactSteps) > mGraphView.getViewport().mCurrentViewport.top) {
                continue;
            }
            // dont draw if it is below of visible screen
            if (newMinY + (i * exactSteps) < mGraphView.getViewport().mCurrentViewport.bottom) {
                continue;
            }


            // where is the data point on the current screen
            double dataPointPos = newMinY + (i * exactSteps);
            double relativeToCurrentViewport = dataPointPos - mGraphView.getViewport().mCurrentViewport.bottom;

            double pixelPos = relativeToCurrentViewport * pixelPerData;
            mStepsVertical.put((int) pixelPos, dataPointPos);
        }

        return true;
    }

    
    protected boolean adjustHorizontal(boolean changeBounds) {
        if (mLabelVerticalWidth == null) {
            return false;
        }

        double minX = mGraphView.getViewport().getMinX(false);
        double maxX = mGraphView.getViewport().getMaxX(false);
        if (minX == maxX) return false;

        // TODO find the number of labels
        int numHorizontalLabels = mNumHorizontalLabels;

        double newMinX;
        double exactSteps;

        // split range into equal steps
        exactSteps = (maxX - minX) / (numHorizontalLabels - 1);

        // round because of floating error
        exactSteps = Math.round(exactSteps * 1000000d) / 1000000d;

        // smallest viewport
        if (exactSteps == 0d) {
            exactSteps = 0.0000001d;
            maxX = minX + exactSteps * (numHorizontalLabels - 1);
        }

        // human rounding to have nice numbers (1, 2, 5, ...)
        if (isHumanRoundingX()) {
            exactSteps = humanRound(exactSteps, false);
        } else if (mStepsHorizontal != null && mStepsHorizontal.size() > 1) {
            // else choose other nice steps that previous
            // steps are included (divide to have more, or multiplicate to have less)

            double d1 = 0, d2 = 0;
            int i = 0;
            for (Double v : mStepsHorizontal.values()) {
                if (i == 0) {
                    d1 = v;
                } else {
                    d2 = v;
                    break;
                }
                i++;
            }
            double oldSteps = d2 - d1;
            if (oldSteps > 0) {
                double newSteps = Double.NaN;

                if (oldSteps > exactSteps) {
                    newSteps = oldSteps / 2;
                } else if (oldSteps < exactSteps) {
                    newSteps = oldSteps * 2;
                }

                // only if there wont be more than numLabels
                // and newSteps will be better than oldSteps
                int numStepsOld = (int) ((maxX - minX) / oldSteps);
                int numStepsNew = (int) ((maxX - minX) / newSteps);

                boolean shouldChange;

                // avoid switching between 2 steps
                if (numStepsOld <= numHorizontalLabels && numStepsNew <= numHorizontalLabels) {
                    // both are possible
                    // only the new if it hows more labels
                    shouldChange = numStepsNew > numStepsOld;
                } else {
                    shouldChange = true;
                }

                if (newSteps != Double.NaN && shouldChange && numStepsNew <= numHorizontalLabels) {
                    exactSteps = newSteps;
                } else {
                    // try to stay to the old steps
                    exactSteps = oldSteps;
                }
            }
        } else {
            // first time
        }


        // starting from 1st datapoint
        // goal is to start with the minX or 1 step before
        newMinX = mGraphView.getViewport().getReferenceX();
        // must be down-rounded
        double count = Math.floor((minX-newMinX)/exactSteps);
        newMinX = count*exactSteps + newMinX;

        // now we have our labels bounds
        if (changeBounds) {
            mGraphView.getViewport().setMinX(newMinX);
            mGraphView.getViewport().setMaxX(newMinX + (numHorizontalLabels - 1) * exactSteps);
            mGraphView.getViewport().mXAxisBoundsStatus = Viewport.AxisBoundsStatus.AUTO_ADJUSTED;
        }

        // it can happen that we need to add some more labels to fill the complete screen
        numHorizontalLabels = (int) ((mGraphView.getViewport().mCurrentViewport.width() / exactSteps)) + 1;

        if (mStepsHorizontal != null) {
            mStepsHorizontal.clear();
        } else {
            mStepsHorizontal = new LinkedHashMap<>((int) numHorizontalLabels);
        }

        int width = mGraphView.getGraphContentWidth();
        // convert data-x to pixel-x in current viewport
        double pixelPerData = width / mGraphView.getViewport().mCurrentViewport.width();

        for (int i = 0; i < numHorizontalLabels; i++) {
            // dont draw if it is left of visible screen
            if (newMinX + (i * exactSteps) < mGraphView.getViewport().mCurrentViewport.left) {
                continue;
            }

            // where is the data point on the current screen
            double dataPointPos = newMinX + (i * exactSteps);
            double relativeToCurrentViewport = dataPointPos - mGraphView.getViewport().mCurrentViewport.left;

            double pixelPos = relativeToCurrentViewport * pixelPerData;
            mStepsHorizontal.put((int) pixelPos, dataPointPos);
        }

        return true;
    }

    
    protected void adjustSteps() {
        mIsAdjusted = adjustVertical(! Viewport.AxisBoundsStatus.FIX.equals(mGraphView.getViewport().mYAxisBoundsStatus));
        mIsAdjusted &= adjustVerticalSecondScale();
        mIsAdjusted &= adjustHorizontal(! Viewport.AxisBoundsStatus.FIX.equals(mGraphView.getViewport().mXAxisBoundsStatus));
    }

    
    protected void calcLabelVerticalSize(Canvas canvas) {
        // test label with first and last label
        String testLabel = mLabelFormatter.formatLabel(mGraphView.getViewport().getMaxY(false), false);
        if (testLabel == null) testLabel = "";

        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = textBounds.width();
        mLabelVerticalHeight = textBounds.height();

        testLabel = mLabelFormatter.formatLabel(mGraphView.getViewport().getMinY(false), false);
        if (testLabel == null) testLabel = "";

        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalWidth = Math.max(mLabelVerticalWidth, textBounds.width());

        // add some pixel to get a margin
        mLabelVerticalWidth += 6;

        // space between text and graph content
        mLabelVerticalWidth += mStyles.labelsSpace;

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalHeight *= lines;
    }

    
    protected void calcLabelVerticalSecondScaleSize(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            mLabelVerticalSecondScaleWidth = 0;
            mLabelVerticalSecondScaleHeight = 0;
            return;
        }

        // test label
        double testY = ((mGraphView.mSecondScale.getMaxY(false) - mGraphView.mSecondScale.getMinY(false)) * 0.783) + mGraphView.mSecondScale.getMinY(false);
        String testLabel = mGraphView.mSecondScale.getLabelFormatter().formatLabel(testY, false);
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelVerticalSecondScaleWidth = textBounds.width();
        mLabelVerticalSecondScaleHeight = textBounds.height();

        // multiline
        int lines = 1;
        for (byte c : testLabel.getBytes()) {
            if (c == '\n') lines++;
        }
        mLabelVerticalSecondScaleHeight *= lines;
    }

    
    protected void calcLabelHorizontalSize(Canvas canvas) {
        // test label
        double testX = ((mGraphView.getViewport().getMaxX(false) - mGraphView.getViewport().getMinX(false)) * 0.783) + mGraphView.getViewport().getMinX(false);
        String testLabel = mLabelFormatter.formatLabel(testX, true);
        if (testLabel == null) {
            testLabel = "";
        }
        Rect textBounds = new Rect();
        mPaintLabel.getTextBounds(testLabel, 0, testLabel.length(), textBounds);
        mLabelHorizontalWidth = textBounds.width();

        if (!mLabelHorizontalHeightFixed) {
            mLabelHorizontalHeight = textBounds.height();

            // multiline
            int lines = 1;
            for (byte c : testLabel.getBytes()) {
                if (c == '\n') lines++;
            }
            mLabelHorizontalHeight *= lines;

            mLabelHorizontalHeight = (int) Math.max(mLabelHorizontalHeight, mStyles.textSize);
        }

        if (mStyles.horizontalLabelsAngle > 0f && mStyles.horizontalLabelsAngle <= 180f) {
            int adjHorizontalHeightH = (int) Math.round(Math.abs(mLabelHorizontalHeight*Math.cos(Math.toRadians(mStyles.horizontalLabelsAngle))));
            int adjHorizontalHeightW = (int) Math.round(Math.abs(mLabelHorizontalWidth*Math.sin(Math.toRadians(mStyles.horizontalLabelsAngle))));
            int adjHorizontalWidthH = (int) Math.round(Math.abs(mLabelHorizontalHeight*Math.sin(Math.toRadians(mStyles.horizontalLabelsAngle))));
            int adjHorizontalWidthW = (int) Math.round(Math.abs(mLabelHorizontalWidth*Math.cos(Math.toRadians(mStyles.horizontalLabelsAngle))));

            mLabelHorizontalHeight = adjHorizontalHeightH + adjHorizontalHeightW;
            mLabelHorizontalWidth = adjHorizontalWidthH + adjHorizontalWidthW;
        }

        // space between text and graph content
        mLabelHorizontalHeight += mStyles.labelsSpace;
    }

    
    public void draw(Canvas canvas) {
        boolean labelSizeChanged = false;
        if (mLabelHorizontalWidth == null) {
            calcLabelHorizontalSize(canvas);
            labelSizeChanged = true;
        }
        if (mLabelVerticalWidth == null) {
            calcLabelVerticalSize(canvas);
            labelSizeChanged = true;
        }
        if (mLabelVerticalSecondScaleWidth == null) {
            calcLabelVerticalSecondScaleSize(canvas);
            labelSizeChanged = true;
        }
        if (labelSizeChanged) {
            // redraw directly
            mGraphView.drawGraphElements(canvas);
            return;
        }

        if (!mIsAdjusted) {
            adjustSteps();
        }

        if (mIsAdjusted) {
            drawVerticalSteps(canvas);
            drawVerticalStepsSecondScale(canvas);
            drawHorizontalSteps(canvas);
        } else {
            // we can not draw anything
            return;
        }

        drawHorizontalAxisTitle(canvas);
        drawVerticalAxisTitle(canvas);

        // draw second scale axis title if it exists
        if (mGraphView.mSecondScale != null) {
            mGraphView.mSecondScale.drawVerticalAxisTitle(canvas);
        }
    }

    
    protected void drawHorizontalAxisTitle(Canvas canvas) {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getHorizontalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getHorizontalAxisTitleTextSize());
            float x = canvas.getWidth() / 2;
            float y = canvas.getHeight() - mStyles.padding;
            canvas.drawText(mHorizontalAxisTitle, x, y, mPaintAxisTitle);
        }
    }

    
    protected void drawVerticalAxisTitle(Canvas canvas) {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            mPaintAxisTitle.setColor(getVerticalAxisTitleColor());
            mPaintAxisTitle.setTextSize(getVerticalAxisTitleTextSize());
            float x = getVerticalAxisTitleWidth();
            float y = canvas.getHeight() / 2;
            canvas.save();
            canvas.rotate(-90, x, y);
            canvas.drawText(mVerticalAxisTitle, x, y, mPaintAxisTitle);
            canvas.restore();
        }
    }

    
    public int getHorizontalAxisTitleHeight() {
        if (mHorizontalAxisTitle != null && mHorizontalAxisTitle.length() > 0) {
            return (int) getHorizontalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    
    public int getVerticalAxisTitleWidth() {
        if (mVerticalAxisTitle != null && mVerticalAxisTitle.length() > 0) {
            return (int) getVerticalAxisTitleTextSize();
        } else {
            return 0;
        }
    }

    
    protected void drawHorizontalSteps(Canvas canvas) {
        // draw horizontal steps (vertical lines and horizontal labels)
        mPaintLabel.setColor(getHorizontalLabelsColor());
        int i = 0;
        for (Map.Entry<Integer, Double> e : mStepsHorizontal.entrySet()) {
            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            if (mStyles.gridStyle.drawVertical()) {
                // dont draw if it is right of visible screen
                if (e.getKey() <= mGraphView.getGraphContentWidth()) {
                    canvas.drawLine(mGraphView.getGraphContentLeft()+e.getKey(), mGraphView.getGraphContentTop(), mGraphView.getGraphContentLeft()+e.getKey(), mGraphView.getGraphContentTop() + mGraphView.getGraphContentHeight(), mPaintLine);
                }
            }

            // draw label
            if (isHorizontalLabelsVisible()) {
                if (mStyles.horizontalLabelsAngle > 0f && mStyles.horizontalLabelsAngle <= 180f) {
                    if (mStyles.horizontalLabelsAngle < 90f) {
                        mPaintLabel.setTextAlign((Paint.Align.RIGHT));
                    } else if (mStyles.horizontalLabelsAngle <= 180f) {
                        mPaintLabel.setTextAlign((Paint.Align.LEFT));
                    }
                } else {
                    mPaintLabel.setTextAlign(Paint.Align.CENTER);
                    if (i == mStepsHorizontal.size() - 1)
                        mPaintLabel.setTextAlign(Paint.Align.RIGHT);
                    if (i == 0)
                        mPaintLabel.setTextAlign(Paint.Align.LEFT);
                }

                // multiline labels
                String label = mLabelFormatter.formatLabel(e.getValue(), true);
                if (label == null) {
                    label = "";
                }
                String[] lines = label.split("\n");
                
                // If labels are angled, calculate adjustment to line them up with the grid
                int labelWidthAdj = 0;
                if (mStyles.horizontalLabelsAngle > 0f && mStyles.horizontalLabelsAngle <= 180f) {
                    Rect textBounds = new Rect();
                    mPaintLabel.getTextBounds(lines[0], 0, lines[0].length(), textBounds);
                    labelWidthAdj = (int) Math.abs(textBounds.width()*Math.cos(Math.toRadians(mStyles.horizontalLabelsAngle)));
                }
                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y = (canvas.getHeight() - mStyles.padding - getHorizontalAxisTitleHeight()) - (lines.length - li - 1) * getTextSize() * 1.1f + mStyles.labelsSpace;
                    float x = mGraphView.getGraphContentLeft()+e.getKey();
                    if (mStyles.horizontalLabelsAngle > 0 && mStyles.horizontalLabelsAngle < 90f) {
                        canvas.save();
                        canvas.rotate(mStyles.horizontalLabelsAngle, x + labelWidthAdj, y);
                        canvas.drawText(lines[li], x + labelWidthAdj, y, mPaintLabel);
                        canvas.restore();
                    } else if (mStyles.horizontalLabelsAngle > 0 && mStyles.horizontalLabelsAngle <= 180f) {
                        canvas.save();
                        canvas.rotate(mStyles.horizontalLabelsAngle - 180f, x - labelWidthAdj, y);
                        canvas.drawText(lines[li], x - labelWidthAdj, y, mPaintLabel);
                        canvas.restore();
                    } else {
                        canvas.drawText(lines[li], x, y, mPaintLabel);
                    }
                }
            }
            i++;
        }
    }

    
    protected void drawVerticalStepsSecondScale(Canvas canvas) {
        if (mGraphView.mSecondScale == null) {
            return;
        }

        // draw only the vertical labels on the right
        float startLeft = mGraphView.getGraphContentLeft() + mGraphView.getGraphContentWidth();
        mPaintLabel.setColor(getVerticalLabelsSecondScaleColor());
        mPaintLabel.setTextAlign(getVerticalLabelsSecondScaleAlign());
        for (Map.Entry<Integer, Double> e : mStepsVerticalSecondScale.entrySet()) {
            float posY = mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight()-e.getKey();

            // draw label
            int labelsWidth = mLabelVerticalSecondScaleWidth;
            int labelsOffset = (int) startLeft;
            if (getVerticalLabelsSecondScaleAlign() == Paint.Align.RIGHT) {
                labelsOffset += labelsWidth;
            } else if (getVerticalLabelsSecondScaleAlign() == Paint.Align.CENTER) {
                labelsOffset += labelsWidth / 2;
            }

            float y = posY;

            String[] lines = mGraphView.mSecondScale.mLabelFormatter.formatLabel(e.getValue(), false).split("\n");
            y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
            for (int li = 0; li < lines.length; li++) {
                // for the last line y = height
                float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
            }
        }
    }

    
    protected void drawVerticalSteps(Canvas canvas) {
        // draw vertical steps (horizontal lines and vertical labels)
        float startLeft = mGraphView.getGraphContentLeft();
        mPaintLabel.setColor(getVerticalLabelsColor());
        mPaintLabel.setTextAlign(getVerticalLabelsAlign());

        int numberOfLine = mStepsVertical.size();
        int currentLine = 1;

        for (Map.Entry<Integer, Double> e : mStepsVertical.entrySet()) {
            float posY = mGraphView.getGraphContentTop()+mGraphView.getGraphContentHeight()-e.getKey();

            // draw line
            if (mStyles.highlightZeroLines) {
                if (e.getValue() == 0d) {
                    mPaintLine.setStrokeWidth(5);
                } else {
                    mPaintLine.setStrokeWidth(0);
                }
            }
            if (mStyles.gridStyle.drawHorizontal()) {
                canvas.drawLine(startLeft, posY, startLeft + mGraphView.getGraphContentWidth(), posY, mPaintLine);
            }

            //if draw the label above or below the line, we mustn't draw the first for last label, for beautiful design.
            boolean isDrawLabel = true;
            if ((mStyles.verticalLabelsVAlign == VerticalLabelsVAlign.ABOVE && currentLine == 1)
                    || (mStyles.verticalLabelsVAlign == VerticalLabelsVAlign.BELOW && currentLine == numberOfLine)){
                isDrawLabel = false;
            }

            // draw label
            if (isVerticalLabelsVisible() && isDrawLabel) {
                int labelsWidth = mLabelVerticalWidth;
                int labelsOffset = 0;
                if (getVerticalLabelsAlign() == Paint.Align.RIGHT) {
                    labelsOffset = labelsWidth;
                    labelsOffset -= mStyles.labelsSpace;
                } else if (getVerticalLabelsAlign() == Paint.Align.CENTER) {
                    labelsOffset = labelsWidth / 2;
                }
                labelsOffset += mStyles.padding + getVerticalAxisTitleWidth();

                float y = posY;

                String label = mLabelFormatter.formatLabel(e.getValue(), false);
                if (label == null) {
                    label = "";
                }
                String[] lines = label.split("\n");
                switch (mStyles.verticalLabelsVAlign){
                    case MID:
                        y += (lines.length * getTextSize() * 1.1f) / 2; // center text vertically
                        break;
                    case ABOVE:
                        y -= 5;
                        break;
                    case BELOW:
                        y += (lines.length * getTextSize() * 1.1f) + 5;
                        break;
                }

                for (int li = 0; li < lines.length; li++) {
                    // for the last line y = height
                    float y2 = y - (lines.length - li - 1) * getTextSize() * 1.1f;
                    canvas.drawText(lines[li], labelsOffset, y2, mPaintLabel);
                }
            }

            currentLine ++;
        }
    }

    
    protected double humanRound(double in, boolean roundAlwaysUp) {
        // round-up to 1-steps, 2-steps or 5-steps
        int ten = 0;
        while (Math.abs(in) >= 10d) {
            in /= 10d;
            ten++;
        }
        while (Math.abs(in) < 1d) {
            in *= 10d;
            ten--;
        }
        if (roundAlwaysUp) {
            if (in == 1d) {
            } else if (in <= 2d) {
                in = 2d;
            } else if (in <= 5d) {
                in = 5d;
            } else if (in < 10d) {
                in = 10d;
            }
        } else { // always round down
            if (in == 1d) {
            } else if (in <= 4.9d) {
                in = 2d;
            } else if (in <= 9.9d) {
                in = 5d;
            } else if (in < 15d) {
                in = 10d;
            }
        }
        return in * Math.pow(10d, ten);
    }

    
    public Styles getStyles() {
        return mStyles;
    }

    
    public int getLabelVerticalWidth() {
        if (mStyles.verticalLabelsVAlign == VerticalLabelsVAlign.ABOVE
                || mStyles.verticalLabelsVAlign == VerticalLabelsVAlign.BELOW) {
            return 0;
        }
        return mLabelVerticalWidth == null || !isVerticalLabelsVisible() ? 0 : mLabelVerticalWidth;
    }

    
    public void setLabelVerticalWidth(Integer width) {
        mLabelVerticalWidth = width;
        mLabelVerticalWidthFixed = mLabelVerticalWidth != null;
    }

    
    public int getLabelHorizontalHeight() {
        return mLabelHorizontalHeight == null || !isHorizontalLabelsVisible() ? 0 : mLabelHorizontalHeight;
    }

    
    public void setLabelHorizontalHeight(Integer height) {
        mLabelHorizontalHeight = height;
        mLabelHorizontalHeightFixed = mLabelHorizontalHeight != null;
    }

    
    public int getGridColor() {
        return mStyles.gridColor;
    }

    
    public boolean isHighlightZeroLines() {
        return mStyles.highlightZeroLines;
    }

    
    public int getPadding() {
        return mStyles.padding;
    }

    
    public void setTextSize(float textSize) {
        mStyles.textSize = textSize;
        reloadStyles();
    }

    
    public void setVerticalLabelsAlign(Paint.Align verticalLabelsAlign) {
        mStyles.verticalLabelsAlign = verticalLabelsAlign;
    }

    
    public void setVerticalLabelsColor(int verticalLabelsColor) {
        mStyles.verticalLabelsColor = verticalLabelsColor;
    }

    
    public void setHorizontalLabelsColor(int horizontalLabelsColor) {
        mStyles.horizontalLabelsColor = horizontalLabelsColor;
    }
    
    
    public void setHorizontalLabelsAngle(int horizontalLabelsAngle) {
        mStyles.horizontalLabelsAngle = horizontalLabelsAngle;
    }

    
    public void setGridColor(int gridColor) {
        mStyles.gridColor = gridColor;
        reloadStyles();
    }

    
    public void setHighlightZeroLines(boolean highlightZeroLines) {
        mStyles.highlightZeroLines = highlightZeroLines;
    }

    
    public void setPadding(int padding) {
        mStyles.padding = padding;
    }

    
    public LabelFormatter getLabelFormatter() {
        return mLabelFormatter;
    }

    
    public void setLabelFormatter(LabelFormatter mLabelFormatter) {
        this.mLabelFormatter = mLabelFormatter;
        mLabelFormatter.setViewport(mGraphView.getViewport());
    }

    
    public String getHorizontalAxisTitle() {
        return mHorizontalAxisTitle;
    }

    
    public void setHorizontalAxisTitle(String mHorizontalAxisTitle) {
        this.mHorizontalAxisTitle = mHorizontalAxisTitle;
    }

    
    public String getVerticalAxisTitle() {
        return mVerticalAxisTitle;
    }

    
    public void setVerticalAxisTitle(String mVerticalAxisTitle) {
        this.mVerticalAxisTitle = mVerticalAxisTitle;
    }

    
    public float getVerticalAxisTitleTextSize() {
        return mStyles.verticalAxisTitleTextSize;
    }

    
    public void setVerticalAxisTitleTextSize(float verticalAxisTitleTextSize) {
        mStyles.verticalAxisTitleTextSize = verticalAxisTitleTextSize;
    }

    
    public int getVerticalAxisTitleColor() {
        return mStyles.verticalAxisTitleColor;
    }

    
    public void setVerticalAxisTitleColor(int verticalAxisTitleColor) {
        mStyles.verticalAxisTitleColor = verticalAxisTitleColor;
    }

    
    public float getHorizontalAxisTitleTextSize() {
        return mStyles.horizontalAxisTitleTextSize;
    }

    
    public void setHorizontalAxisTitleTextSize(float horizontalAxisTitleTextSize) {
        mStyles.horizontalAxisTitleTextSize = horizontalAxisTitleTextSize;
    }

    
    public int getHorizontalAxisTitleColor() {
        return mStyles.horizontalAxisTitleColor;
    }

    
    public void setHorizontalAxisTitleColor(int horizontalAxisTitleColor) {
        mStyles.horizontalAxisTitleColor = horizontalAxisTitleColor;
    }

    
    public Paint.Align getVerticalLabelsSecondScaleAlign() {
        return mStyles.verticalLabelsSecondScaleAlign;
    }

    
    public void setVerticalLabelsSecondScaleAlign(Paint.Align verticalLabelsSecondScaleAlign) {
        mStyles.verticalLabelsSecondScaleAlign = verticalLabelsSecondScaleAlign;
    }

    
    public int getVerticalLabelsSecondScaleColor() {
        return mStyles.verticalLabelsSecondScaleColor;
    }

    
    public void setVerticalLabelsSecondScaleColor(int verticalLabelsSecondScaleColor) {
        mStyles.verticalLabelsSecondScaleColor = verticalLabelsSecondScaleColor;
    }

    
    public int getLabelVerticalSecondScaleWidth() {
        return mLabelVerticalSecondScaleWidth==null?0:mLabelVerticalSecondScaleWidth;
    }

    
    public boolean isHorizontalLabelsVisible() {
        return mStyles.horizontalLabelsVisible;
    }

    
    public void setHorizontalLabelsVisible(boolean horizontalTitleVisible) {
        mStyles.horizontalLabelsVisible = horizontalTitleVisible;
    }

    
    public boolean isVerticalLabelsVisible() {
        return mStyles.verticalLabelsVisible;
    }

    
    public void setVerticalLabelsVisible(boolean verticalTitleVisible) {
        mStyles.verticalLabelsVisible = verticalTitleVisible;
    }

    
    public int getNumVerticalLabels() {
        return mNumVerticalLabels;
    }

    
    public void setNumVerticalLabels(int mNumVerticalLabels) {
        this.mNumVerticalLabels = mNumVerticalLabels;
    }

    
    public int getNumHorizontalLabels() {
        return mNumHorizontalLabels;
    }

    
    public void setNumHorizontalLabels(int mNumHorizontalLabels) {
        this.mNumHorizontalLabels = mNumHorizontalLabels;
    }

    
    public GridStyle getGridStyle() {
        return mStyles.gridStyle;
    }

    
    public void setGridStyle(GridStyle gridStyle) {
        mStyles.gridStyle = gridStyle;
    }

    
    public int getLabelsSpace() {
        return mStyles.labelsSpace;
    }

    
    public void setLabelsSpace(int labelsSpace) {
        mStyles.labelsSpace = labelsSpace;
    }


    
    public void setVerticalLabelsVAlign(VerticalLabelsVAlign align){
        mStyles.verticalLabelsVAlign = align;
    }

    
    public VerticalLabelsVAlign getVerticalLabelsVAlign(){
        return mStyles.verticalLabelsVAlign;
    }
}
