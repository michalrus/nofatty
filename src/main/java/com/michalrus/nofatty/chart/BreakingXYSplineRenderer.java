package com.michalrus.nofatty.chart;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class BreakingXYSplineRenderer extends XYSplineRenderer {
    /**
     * Draws the item (first pass). This method draws the lines
     * connecting the items. Instead of drawing separate lines,
     * a GeneralPath is constructed and drawn at the end of
     * the series painting.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param plot  the plot (can be used to obtain standard color information
     *              etc).
     * @param dataset  the dataset.
     * @param pass  the pass.
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param xAxis  the domain axis.
     * @param yAxis  the range axis.
     * @param dataArea  the area within which the data is being drawn.
     */
    @Override
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
                                         Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
                                         int series, int item, ValueAxis xAxis, ValueAxis yAxis,
                                         Rectangle2D dataArea) {

        XYSplineState s = (XYSplineState) state;
        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data points
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = xAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = yAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        // Collect points
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            Point2D p = plot.getOrientation() == PlotOrientation.HORIZONTAL
                    ? new Point2D.Float((float) transY1, (float) transX1)
                    : new Point2D.Float((float) transX1, (float) transY1);
            if (!s.points.contains(p))
                s.points.add(p);
        }

        if (item == dataset.getItemCount(series) - 1 || Double.isNaN(y1)) {     // construct path
            if (s.points.size() > 1) {
                Point2D origin;
                if (this.getFillType() == FillType.TO_ZERO) {
                    float xz = (float) xAxis.valueToJava2D(0, dataArea,
                            yAxisLocation);
                    float yz = (float) yAxis.valueToJava2D(0, dataArea,
                            yAxisLocation);
                    origin = plot.getOrientation() == PlotOrientation.HORIZONTAL
                            ? new Point2D.Float(yz, xz)
                            : new Point2D.Float(xz, yz);
                } else if (this.getFillType() == FillType.TO_LOWER_BOUND) {
                    float xlb = (float) xAxis.valueToJava2D(
                            xAxis.getLowerBound(), dataArea, xAxisLocation);
                    float ylb = (float) yAxis.valueToJava2D(
                            yAxis.getLowerBound(), dataArea, yAxisLocation);
                    origin = plot.getOrientation() == PlotOrientation.HORIZONTAL
                            ? new Point2D.Float(ylb, xlb)
                            : new Point2D.Float(xlb, ylb);
                } else {// fillType == TO_UPPER_BOUND
                    float xub = (float) xAxis.valueToJava2D(
                            xAxis.getUpperBound(), dataArea, xAxisLocation);
                    float yub = (float) yAxis.valueToJava2D(
                            yAxis.getUpperBound(), dataArea, yAxisLocation);
                    origin = plot.getOrientation() == PlotOrientation.HORIZONTAL
                            ? new Point2D.Float(yub, xub)
                            : new Point2D.Float(xub, yub);
                }

                // we need at least two points to draw something
                Point2D cp0 = s.points.get(0);
                s.seriesPath.moveTo(cp0.getX(), cp0.getY());
                if (this.getFillType() != FillType.NONE) {
                    if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                        s.fillArea.moveTo(origin.getX(), cp0.getY());
                    } else {
                        s.fillArea.moveTo(cp0.getX(), origin.getY());
                    }
                    s.fillArea.lineTo(cp0.getX(), cp0.getY());
                }
                if (s.points.size() == 2) {
                    // we need at least 3 points to spline. Draw simple line
                    // for two points
                    Point2D cp1 = s.points.get(1);
                    if (this.getFillType() != FillType.NONE) {
                        s.fillArea.lineTo(cp1.getX(), cp1.getY());
                        s.fillArea.lineTo(cp1.getX(), origin.getY());
                        s.fillArea.closePath();
                    }
                    s.seriesPath.lineTo(cp1.getX(), cp1.getY());
                } else {
                    // construct spline
                    int np = s.points.size(); // number of points
                    float[] d = new float[np]; // Newton form coefficients
                    float[] x = new float[np]; // x-coordinates of nodes
                    float y, oldy;
                    float t, oldt;

                    float[] a = new float[np];
                    float t1;
                    float t2;
                    float[] h = new float[np];

                    for (int i = 0; i < np; i++) {
                        Point2D.Float cpi = (Point2D.Float) s.points.get(i);
                        x[i] = cpi.x;
                        d[i] = cpi.y;
                    }

                    for (int i = 1; i <= np - 1; i++)
                        h[i] = x[i] - x[i - 1];

                    float[] sub = new float[np - 1];
                    float[] diag = new float[np - 1];
                    float[] sup = new float[np - 1];

                    for (int i = 1; i <= np - 2; i++) {
                        diag[i] = (h[i] + h[i + 1]) / 3;
                        sup[i] = h[i + 1] / 6;
                        sub[i] = h[i] / 6;
                        a[i] = (d[i + 1] - d[i]) / h[i + 1]
                                - (d[i] - d[i - 1]) / h[i];
                    }
                    solveTridiag(sub, diag, sup, a, np - 2);

                    // note that a[0]=a[np-1]=0
                    oldt = x[0];
                    oldy = d[0];
                    for (int i = 1; i <= np - 1; i++) {
                        // loop over intervals between nodes
                        for (int j = 1; j <= this.getPrecision(); j++) {
                            t1 = (h[i] * j) / this.getPrecision();
                            t2 = h[i] - t1;
                            y = ((-a[i - 1] / 6 * (t2 + h[i]) * t1 + d[i - 1])
                                    * t2 + (-a[i] / 6 * (t1 + h[i]) * t2
                                    + d[i]) * t1) / h[i];
                            t = x[i - 1] + t1;
                            s.seriesPath.lineTo(t, y);
                            if (this.getFillType() != FillType.NONE) {
                                s.fillArea.lineTo(t, y);
                            }
                        }
                    }
                }
                // Add last point @ y=0 for fillPath and close path
                if (this.getFillType() != FillType.NONE) {
                    if (plot.getOrientation() == PlotOrientation.HORIZONTAL) {
                        s.fillArea.lineTo(origin.getX(), s.points.get(
                                s.points.size() - 1).getY());
                    } else {
                        s.fillArea.lineTo(s.points.get(
                                s.points.size() - 1).getX(), origin.getY());
                    }
                    s.fillArea.closePath();
                }

                // fill under the curve...
                if (this.getFillType() != FillType.NONE) {
                    Paint fp = getSeriesFillPaint(series);
                    if (this.getGradientPaintTransformer() != null
                            && fp instanceof GradientPaint) {
                        GradientPaint gp = this.getGradientPaintTransformer()
                                .transform((GradientPaint) fp, s.fillArea);
                        g2.setPaint(gp);
                    } else {
                        g2.setPaint(fp);
                    }
                    g2.fill(s.fillArea);
                    s.fillArea.reset();
                }
                // then draw the line...
                drawFirstPassShape(g2, pass, series, item, s.seriesPath);
            }
            // reset points vector
            s.points = new ArrayList<Point2D>();
        }
    }

    private void solveTridiag(float[] sub, float[] diag, float[] sup,
                              float[] b, int n) {
/*      solve linear system with tridiagonal n by n matrix a
        using Gaussian elimination *without* pivoting
        where   a(i,i-1) = sub[i]  for 2<=i<=n
        a(i,i)   = diag[i] for 1<=i<=n
        a(i,i+1) = sup[i]  for 1<=i<=n-1
        (the values sub[1], sup[n] are ignored)
        right hand side vector b[1:n] is overwritten with solution
        NOTE: 1...n is used in all arrays, 0 is unused */
        int i;
/*      factorization and forward substitution */
        for (i = 2; i <= n; i++) {
            sub[i] /= diag[i - 1];
            diag[i] -= sub[i] * sup[i - 1];
            b[i] -= sub[i] * b[i - 1];
        }
        b[n] /= diag[n];
        for (i = n - 1; i >= 1; i--)
            b[i] = (b[i] - sup[i] * b[i + 1]) / diag[i];
    }
}
