package tau.tac.adx.analysis;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class Histogram {
    public static void main(String[] args) {
        double[] value = new double[100];
        Random generator = new Random();
        for (int i = 1; i < 100; i++) {
            value[i] = generator.nextDouble();
        }

    }
}