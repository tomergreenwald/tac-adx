package tau.tac.adx.analysis;

import com.google.common.base.Stopwatch;
import com.google.common.io.ByteStreams;
import com.google.protobuf.InvalidProtocolBufferException;
import flatbuffers.FlatBufferBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import tau.tac.adx.parser.Auctions;
import tau.tac.adx.parser.Auctions.AdxBidList.AdxBidEntry;
import tau.tac.adx.parser.Auctions.AdxQuery;
import tau.tac.adx.parser.Auctions.AuctionReport;
import tau.tac.adx.parser.Auctions.Container;
import tau.tac.adx.parser.Auctions.DataBundle;
import tau.tac.adx.parser.Auctions.DataBundle.Builder;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ReservePriceAnalysis {
    public static final int PRECISION = 3;
    public static final int MIN_AUCTION_DAY = 10;
    public static final int FILE_COUNT = 100;
    private static final String GZIP_FILE_ENDING = ".gz";
    private static final double EPSILON = 0.00000000001;
    public static final double MAX_BID = 0.5;
    public static final double MIN_BID = 0.00000000001;

    public static void main(String[] args) throws IOException, InterruptedException {

        String agentGiza = "Giza";
        String agentBob = "Bob";
        String agentAdxperts = "Adxperts";
        String agentLosCaparos = "LosCaparos";


        String[] agents = {agentAdxperts,
                agentBob,
                agentLosCaparos,
                agentGiza,
        };

        for (String agent : agents) {
            parseAgent(agent);
        }
    }

    private static void parseAgent(String agent) throws InterruptedException, IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        Map<Double, List<Map<Double, AtomicLong>>> map1 = new HashMap<>();
        Map<Double, List<Map<Double, AtomicLong>>> map2 = new HashMap<>();

        parseFiles(String.format("c:\\temp\\2016_08_20\\%s", agent), map1, map2);

        System.out.println("Reducing maps");
        calcEMD(agent, reduceHistograms(map1), reduceHistograms(map2));
        System.out.println(stopwatch.elapsed(TimeUnit.SECONDS));
    }

    private static double EMD(Map<Double, Double> histogram1, Map<Double, Double> histogram2) {
        double EMD = 0;
        double step = 1.0 / Math.pow(10, PRECISION);

        for (double d = 0; round(d) <= MAX_BID; d += step) {
            EMD += (histogram1.get(round(d)) - histogram2.get(round(d)));
        }
        return EMD;
    }

    private static double chi_squared() {
        long[][] matrix = new long[0][];
        long[] sumRows = new long[matrix[0].length];
        long[] sumCols = new long[matrix.length];
        long sum = 0;
        double res = 0;

        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                sumRows[i] += matrix[i][j];
                sum += matrix[i][j];;
            }
        }

        for (int j = 0; j < matrix.length; j++) {
            for (int i = 0; i < matrix[0].length; i++) {
                sumCols[j] += matrix[i][j];
            }
        }

        for (int i = 0; i < matrix[0].length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                Long observed = matrix[i][j];
                Long expected = sumCols[j]*sumRows[i]/sum;
                res += Math.pow(observed-expected, 2)/expected;
            }
        }
        return res;
    }

    private static void calcEMD(String agent, Map<Double, Map<Double, Double>> histogramMap1, Map<Double, Map<Double, Double>> histogramMap2) throws IOException {
        System.out.println("Extracting data");

        FileOutputStream fos = new FileOutputStream(String.format("t:\\%s.EMD..csv", agent));

        ArrayList<Double> list1 = new ArrayList<>(histogramMap1.keySet());
        ArrayList<Double> list2 = new ArrayList<>(histogramMap2.keySet());

        Collections.sort(list1);
        Collections.sort(list2);
        fos.write(',');
        for (int i = 0; i < list1.size(); i++) {                        //print col headers
            fos.write(String.format("%f,", list1.get(i)).getBytes());
        }

        for (int i = 0; i < list1.size(); i++) {
            double reserve = list1.get(i);
            fos.write(String.format("\n%f,", reserve).getBytes());        //print row headers
            for (int j = 0; j < list2.size(); j++) {
                double reserve2 = list2.get(j);

                Map<Double, Double> cumulativeHistogram1 = cumulative(histogramMap1.get(reserve));
                Map<Double, Double> cumulativeHistogram2 = cumulative(histogramMap2.get(reserve2));
                double sum = EMD(cumulativeHistogram1, cumulativeHistogram2);
                fos.write(String.format("%f,", sum).getBytes());
            }
        }
    }

    private static void parseFiles(String rootFolder, Map<Double, List<Map<Double, AtomicLong>>> map1, Map<Double, List<Map<Double, AtomicLong>>> map2) throws InterruptedException {
        System.out.println("Parsing files");
        ExecutorService service = Executors.newFixedThreadPool(8);
        List<Future> futures = new LinkedList<>();
        int fileCount = FILE_COUNT;
        for (File simulation : new File(rootFolder).listFiles()) {
            futures.add(service.submit(() -> generateHistogram(readRecords(simulation), Math.random() > 0.5 ? map1 : map2, MIN_AUCTION_DAY)));
            if (fileCount-- == 0) {
                break;
            }
        }
        service.shutdown();
        while (!service.awaitTermination(1, TimeUnit.SECONDS)) {
        }
    }

    private static Map<Double, Double> cumulative(Map<Double, Double> histogram) {
        Map<Double, Double> map = new HashMap<>();
        double cumulative = 0;
        double step = 1.0 / Math.pow(10, PRECISION);
        double d;
        for (d = 0; round(d) <= MAX_BID; d += step) {
            cumulative += histogram.getOrDefault(round(d), 0.0);
            map.put(round(d), cumulative);
        }
        return map;
    }

    private static Map<Double, Map<Double, Double>> reduceHistograms(Map<Double, List<Map<Double, AtomicLong>>> megaMap) {
        Map<Double, Map<Double, AtomicLong>> reducedMap = new HashMap<>();

        for (Double reserve1 : megaMap.keySet()) {
            double reserve = reserve1;
            reducedMap.putIfAbsent(reserve, new HashMap<>());
            Map<Double, AtomicLong> unifiedHistogram = reducedMap.get(reserve);
            for (Map<Double, AtomicLong> histogram : megaMap.get(reserve1)) {
                for (Double val : histogram.keySet()) {
                    unifiedHistogram.putIfAbsent(val, new AtomicLong(0));
                    unifiedHistogram.get(val).addAndGet(histogram.get(val).get());
                }
            }
        }

        Map<Double, Double> countMap = new HashMap<>();
        Map<Double, Map<Double, Double>> reducedMap2 = new HashMap<>();
        for (Double reserve: reducedMap.keySet()) {
            for (Double d2: reducedMap.get(reserve).keySet()) {
                reducedMap2.putIfAbsent(reserve, new HashMap<>());
                countMap.putIfAbsent(reserve, 0.0);

                countMap.put(reserve, countMap.get(reserve) + reducedMap.get(reserve).get(d2).get());
                reducedMap2.get(reserve).put(d2, (double) reducedMap.get(reserve).get(d2).get());
            }
        }

        for (Double reserve: reducedMap.keySet()) {
            for (Double d2: reducedMap.get(reserve).keySet()) {
                reducedMap2.get(reserve).put(d2, reducedMap2.get(reserve).get(d2)/countMap.get(reserve));
            }
        }
        return reducedMap2;
    }

    private static double compareDatasets(Map<Double, AtomicLong> h1, Map<Double, AtomicLong> h2) {
        double sum = 0;
        double step = 1.0 / Math.pow(10, PRECISION);

        long sum1 = 0, sum2 = 0;
        for (AtomicLong al : h1.values()) {
            sum1 += al.get();
        }
        for (AtomicLong al : h2.values()) {
            sum2 += al.get();
        }

        for (double i = 0; i < 1; i += step) {
            double p1i = (1.0 * h1.getOrDefault(ReservePriceAnalysis.round(i), new AtomicLong(0)).get()) / sum1;
            double p2i = 1.0 * h2.getOrDefault(ReservePriceAnalysis.round(i), new AtomicLong(0)).get() / sum2;
            sum += Math.abs(p1i - p2i);
        }
        return ReservePriceAnalysis.round(sum);
    }

    private static void convertContainer(Auctions.Container container) {
        if (container.hasBidList()) {
            for (AdxBidEntry entry : container.getBidList().getEntriesList()) {
                //build acd bid list
                //int bidder = builder.createString(entry.getBidder());
                //
                FlatBufferBuilder builder = new FlatBufferBuilder(0);
                AdxQuery adxQuery1 = entry.getAdxQuery();
                int publisherOffset = builder.createString(adxQuery1.getPublisher());
                byte[] marketSegments = new byte[adxQuery1.getMarketSegmentsCount()];
                for (int i = 0; i < adxQuery1.getMarketSegmentsCount(); i++) {
                    marketSegments[i] = (byte) adxQuery1.getMarketSegments(i).ordinal();
                }
                //int marketSegmentOffset = AdxQuery.createMarketSegmentsVector(builder, marketSegments);
                //AdxQuery adxQuery = AdxQuery.createAdxQuery(builder, publisherOffset, marketSegments)
                //AdxBidEntry.createAdxBidEntry(builder, entry.getBid(), bidder, )
            }
        }
        FlatBufferBuilder builder = new FlatBufferBuilder(0);
        //NewDay.createNewDay(builder);
    }

    private static void generateHistogram(List<Container> containers, Map<Double, List<Map<Double, AtomicLong>>> map, int minAuctionDay) {
        Map<Double, AtomicLong> histogram = new HashMap<>();
        List<Double> bidList = new ArrayList<>(containers.size());
        double reservePrice = Double.NaN;
        int day = 0;
        for (Container container : containers) {
            if (container.hasNewDay()) {
                day = container.getNewDay().getDay();
            }
            if (container.hasBidList() && day >= minAuctionDay) {
                for (AdxBidEntry entry : container.getBidList().getEntriesList()) {
                    double bid = round(entry.getBid());
                    if (bid <= MAX_BID && bid > MIN_BID) {
                        bidList.add(bid);
                        histogram.putIfAbsent(bid, new AtomicLong(0));
                        histogram.get(bid).incrementAndGet();
                    }
                    if (reservePrice != Double.NaN) {
                        reservePrice = entry.getReservePrice();
                    }

                }
            }
        }
//        printChart(bidList, String.format("t:\\%.02f", 1000 * reservePrice));
//        double[] bids = bidList.stream().mapToDouble(Double::doubleValue).toArray();
//        int bins = 1000;
//        HistogramDataset dataset = new HistogramDataset();
//        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
//        dataset.addSeries("Histogram", bids, bins);
        synchronized (map) {
            map.putIfAbsent(reservePrice, new LinkedList<>());
            map.get(reservePrice).add(histogram);
        }
    }

    private static void printChart(List<Double> bidList, String folderPath) {
        double[] bids = bidList.stream().mapToDouble(Double::doubleValue).toArray();
        int bins = 1000;
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        dataset.addSeries("Histogram", bids, bins);
        String plotTitle = "Histogram";
        String xaxis = "number";
        String yaxis = "value";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis,
                dataset, orientation, show, toolTips, urls);
        int width = 500;
        int height = 300;
        try {
            File reserveFolder = new File(folderPath);
            reserveFolder.mkdir();
            ChartUtilities.saveChartAsPNG(new File(String.format(reserveFolder.getAbsolutePath() + "\\histogram-%d.PNG", System.currentTimeMillis())), chart, width, height);
        } catch (IOException e) {
        }
    }

    private static double round(double value) {
        try {
            BigDecimal bd = new BigDecimal(value);
            bd = bd.setScale(PRECISION, RoundingMode.HALF_UP);
            return bd.doubleValue();
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static List<Container> readRecords(File simulation) {
        List<Container> containers = new LinkedList<>();
        try {
            InputStream is = new GZIPInputStream(new FileInputStream(simulation));


            Container container = Container.parseDelimitedFrom(is);
            while (container != null) {
                containers.add(container);
                try {

                    container = Container.parseDelimitedFrom(is);
                } catch (InvalidProtocolBufferException e) {

                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return containers;
    }

    private static void compressRecords(String originalFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(originalFile);
        FileOutputStream fileOutputStream = new FileOutputStream(originalFile + ReservePriceAnalysis.GZIP_FILE_ENDING);
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(fileOutputStream);
        ByteStreams.copy(fileInputStream, gzipOutputStream);
        gzipOutputStream.close();
        fileInputStream.close();
        File file = new File(originalFile);
        file.delete();
    }

    private static void decompressRecords(String originalFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(originalFile + ".gz");
        GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
        FileOutputStream fileOutputStream = new FileOutputStream(originalFile + ".gz.dec");
        ByteStreams.copy(gzipInputStream, fileOutputStream);
        gzipInputStream.close();
        fileOutputStream.close();
    }

    private static void printFile(String filePath) throws IOException {
        System.out.println(new String(ReservePriceAnalysis.readFile(filePath)));
    }

    private static void parseReserve(String inputFile, String outputFile, int minAuctionDay) throws IOException {

        DataBundle dataBundle = ReservePriceAnalysis.getDataBundle(inputFile);
        HashMap<Double, HashMap<ReservePriceAnalysis.UO, AtomicLong>> map = new HashMap<>();

        for (AuctionReport report : dataBundle.getReportsList()) {
            if (report.getDay() < minAuctionDay) {
                continue;
            }

            double reservePrice = report.getReservePrice() * 1000;
            map.putIfAbsent(reservePrice, new HashMap<>());
            HashMap<ReservePriceAnalysis.UO, AtomicLong> rMap = map.get(reservePrice);
            ReservePriceAnalysis.UO uo = reservePrice < report.getSecondBid() ? ReservePriceAnalysis.UO.Under : reservePrice < report.getFirstBid() ? ReservePriceAnalysis.UO.Between : ReservePriceAnalysis.UO.Over;
            if (report.getSecondBid() < ReservePriceAnalysis.EPSILON && uo == ReservePriceAnalysis.UO.Between) {
                //if the second bid is lower than epsilon we will count the reserve as UNDER both of them.
                uo = ReservePriceAnalysis.UO.Under;
            }
            if (report.getFirstBid() < ReservePriceAnalysis.EPSILON) {
                uo = ReservePriceAnalysis.UO.Irrelevant;
                continue;
            }
            rMap.putIfAbsent(uo, new AtomicLong(0));
            rMap.get(uo).incrementAndGet();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write("reserve price,\t\tirrelevant, \t\tunder,\t\tunder b,\t\tbetween,\tover\n".getBytes());
        for (double reservePrice : (Set<Double>) new TreeSet(map.keySet())) {
            HashMap<ReservePriceAnalysis.UO, AtomicLong> rMap = map.get(reservePrice);
            if (rMap == null) {
                return;
            }
            long total =
                    rMap.getOrDefault(ReservePriceAnalysis.UO.Irrelevant, new AtomicLong(0)).get() +
                            rMap.getOrDefault(ReservePriceAnalysis.UO.Under, new AtomicLong(0)).get() +
                            rMap.getOrDefault(ReservePriceAnalysis.UO.UnderBetween, new AtomicLong(0)).get() +
                            rMap.getOrDefault(ReservePriceAnalysis.UO.Between, new AtomicLong(0)).get() +
                            rMap.getOrDefault(ReservePriceAnalysis.UO.Over, new AtomicLong(0)).get();
            fos.write(String.format("%f,\t\t\t%.3f,\t\t\t\t%.3f,\t\t%.3f,\t\t\t%.3f,\t\t%.3f\n",
                    reservePrice / 1000,
                    1.0 * rMap.getOrDefault(ReservePriceAnalysis.UO.Irrelevant, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(ReservePriceAnalysis.UO.Under, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(ReservePriceAnalysis.UO.UnderBetween, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(ReservePriceAnalysis.UO.Between, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(ReservePriceAnalysis.UO.Over, new AtomicLong(0)).get() / total
            ).getBytes());
        }
        fos.close();
    }

    private static DataBundle getDataBundle(String input) throws IOException {

        byte[] data = ReservePriceAnalysis.readFile(input);
        DataBundle dataBundle = DataBundle.parseFrom(data);
        return dataBundle;
    }

    private static byte[] readFile(String inputFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        InputStream is = fis;
        if (inputFile.endsWith(ReservePriceAnalysis.GZIP_FILE_ENDING)) {
            is = new GZIPInputStream(fis);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(is, baos);
        return baos.toByteArray();
    }

    private static void reduceRecords(String input, String output, double keepRatio) throws IOException {
        DataBundle dataBundle = ReservePriceAnalysis.getDataBundle(input);

        System.out.println("Reducing data");
        Stopwatch stopwatch = Stopwatch.createStarted();


        FileOutputStream fos = new FileOutputStream(output);
        GZIPOutputStream gos = new GZIPOutputStream(fos);
        Builder builder = DataBundle.newBuilder();
        int counter = 0;
        for (int i = 0; i < dataBundle.getReportsCount(); i++) {
            if (Math.random() < keepRatio) {
                builder.addReports(dataBundle.getReports(i));
                counter++;
            }
        }
        DataBundle reducedDataBundle = builder.build();
        reducedDataBundle.writeTo(gos);
        System.out.println(String.format("Reduced %d records in %d seconds", counter, stopwatch.elapsed(TimeUnit.SECONDS)));
        gos.close();
    }

    enum UO {
        Irrelevant, Under, UnderBetween, Between, Over
    }


}
