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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static java.lang.Math.abs;
import static java.lang.Math.max;

public class ReservePriceAnalysis {
    public static final int PRECISION = 3;
    public static final int MIN_AUCTION_DAY = 10;
    public static final int FILE_COUNT = 100;
    private static final String GZIP_FILE_ENDING = ".gz";
    private static final double EPSILON = 0.00000000001;
    public static final double MAX_BID = 0.1;
    public static final double MIN_BID = 0.00000000001;
    public static final int THREAD_POOL_SIZE = 8;
    private static double BUCKET_SIZE = 0.005
            ;
    public static final String LOGS_BASE_PATH = "c:\\temp\\2016_08_20\\";
    public static final String OUTPUT_FOLDER = "t:\\";
//
//    public static final String LOGS_BASE_PATH = "/home/ubuntu/ADX/resources/";
//    public static final String OUTPUT_FOLDER = "/home/ubuntu/ADX/res/";



        public static void main(String[] args) throws IOException, InterruptedException {

            String agentGiza = "GIZA";
            String agentBob = "bob";
            String agentAdxperts = "adxperts";
            String agentLosCaparos = "LosCaparos";
            String demo = "demo";


            String[] agents = {
//                    agentAdxperts,
//                    agentBob,
//                    agentLosCaparos,
//                    agentGiza,
                    demo
            };
            for (int i = 0; i < 10;i++){
                for (String agent : agents) {
                    parseAgent(agent);
                    System.gc();
                }
            }
        }

        private static void parseAgent(String agent) throws InterruptedException, IOException {
//        Stopwatch stopwatch = Stopwatch.createStarted();
            Map<Double, List<Map<Double, AtomicLong>>> map1 = new HashMap<>();
            Map<Double, List<Map<Double, AtomicLong>>> map2 = new HashMap<>();

            Stopwatch stopwatch = new Stopwatch().start();
            parseFiles(String.format(LOGS_BASE_PATH + "%s", agent), map1, map2);
//            System.out.println(String.format("parsed files in %d seconds", stopwatch.elapsed(TimeUnit.SECONDS)));

//            calc(agent, reduceHistograms(map1, false), reduceHistograms(map2, false), new EMD());
//            calc(agent, reduceHistograms(map1, false), reduceHistograms(map2, false), new MaxDiff());
//            parseFiles(String.format(LOGS_BASE_PATH + "%s", agent), map1, map1);
//            System.out.println(String.format("parsed files in %d seconds", stopwatch.elapsed(TimeUnit.SECONDS)));
            calc_chi(reduceHistograms(map1, false));

        }

    interface HistogramFunc {
        double call(Map<Double, Double> hist1, Map<Double, Double> hist2);
    }

    static class EMD implements HistogramFunc {
        @Override
        public double call(Map<Double, Double> hist1, Map<Double, Double> hist2) {
            double EMD = 0;
            double sum = 0;

            for (double d = 0; round(d) <= MAX_BID; d += BUCKET_SIZE) {
                EMD += (hist1.get(round(d)) - hist2.get(round(d)));
                sum += abs(EMD);
            }
            return sum;
        }
    }

    static class MaxDiff implements HistogramFunc {
        @Override
        public double call(Map<Double, Double> hist1, Map<Double, Double> hist2) {
            double maxDiff = 0;

            for (double d = 0; round(d) <= MAX_BID; d += BUCKET_SIZE) {
                double diff = abs((hist1.get(round(d)) - hist2.get(round(d))));
                maxDiff = max(diff, maxDiff);
            }
            return maxDiff;
        }
    }

    private static double[][] calcMatrix(Map<Double, Map<Double, Double>> histogramMap) {
        double[][] matrix = new double[histogramMap.size()][(int)(MAX_BID/BUCKET_SIZE)+1];

        ArrayList<Double> reserves = new ArrayList<>(histogramMap.keySet());
        Collections.sort(reserves);

        for (int i = 0; i < reserves.size(); i++) {
            double reserve = reserves.get(i);
            Map<Double, Double> reserveMap = fill(histogramMap.get(reserve));
            int j = 0;
            for (double bid = 0; round(bid) <= MAX_BID; bid += BUCKET_SIZE) {
                matrix[i][j] = reserveMap.get(round(bid));
                j++;
            }
        }
        return matrix;
    }


    static void calc_chi(Map<Double, Map<Double, Double>> histogramMap) {

        double[][] matrix = calcMatrix(histogramMap);

        double[][] res = new double[matrix.length][matrix[0].length];
        double[] sumRows = new double[matrix.length];
        double[] sumCols = new double[matrix[0].length];
        double globalSum = 0;
        double sum = 0;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                sumRows[i] += matrix[i][j];
                sumCols[j] += matrix[i][j];
                globalSum += matrix[i][j];
            }
        }

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                double observed = matrix[i][j];
                double expected = sumCols[j] * sumRows[i] / globalSum;
                if (expected != 0)
                    sum += Math.pow(observed - expected, 2) / expected;
            }
        }
        System.out.println(String.format("Test statistic: %f, degrees of freedom: %d, (col) %d, (row) %d", sum, (matrix.length-1)*(matrix[0].length-1), matrix.length-1, matrix[0].length-1));
    }

        private static void calc(String agent, Map<Double, Map<Double, Double>> histogramMap1, Map<Double, Map<Double, Double>> histogramMap2, HistogramFunc func) throws IOException {
            FileOutputStream fos = new FileOutputStream(String.format(OUTPUT_FOLDER + "%s.%s.csv", func.getClass().getSimpleName(), agent));

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
                Map<Double, Double> cumulative1 = cumulative(histogramMap1.get(reserve));

                fos.write(String.format("\n%f,", reserve).getBytes());        //print row headers
                for (int j = 0; j < list2.size(); j++) {
                    double reserve2 = list2.get(j);
                    Map<Double, Double> cumulative2 = cumulative(histogramMap2.get(reserve2));
                    double sum = func.call(cumulative1, cumulative2);
                    fos.write(String.format("%f,", sum).getBytes());
                }
            }
        }


        private static Map<Double, Double> fill(Map<Double, Double> histogram) {
            Map<Double, Double> map = new HashMap<>();
            for (double d = 0; round(d) <= MAX_BID; d += BUCKET_SIZE) {
                map.put(round(d), histogram.getOrDefault(round(d), 0.0));
            }
            return map;
        }

        private static void calcCHI(String agent, Map<Double, Map<Double, Double>> histogramMap) throws IOException {
//            System.out.println("Extracting data");
//            d
//            double sum = chi_squared(matrix);
//            System.out.println(String.format("%s chi_squared sum: %f", agent, sum));
//        double[][] res = chi_squared(matrix);
//
//        FileOutputStream fos = new FileOutputStream(String.format("t:\\%s.CHI..csv", agent));
//
//        fos.write(',');
//        for (int i = 0; i < bids.size(); i++) {                        //print col headers
//            fos.write(String.format("%f,", bids.get(i)).getBytes());
//        }
//
//        for (int i = 0; i < reserves.size(); i++) {
//            double reserve = reserves.get(i);
//            fos.write(String.format("\n%f,", reserve).getBytes());        //print row headers
//            for (int j = 0; j < bidCount; j++) {
////                double bid = bids.get(j);
//                fos.write(String.format("%f,", res[i][j]).getBytes());
//            }
//        }
        }

        private static void parseFiles(String rootFolder, Map<Double, List<Map<Double, AtomicLong>>> map1, Map<Double, List<Map<Double, AtomicLong>>> map2) throws InterruptedException {
//            System.out.println("Parsing files");

//            System.out.println(rootFolder);
            if (rootFolder.contains("demo")){
                for (int i = 0; i < FILE_COUNT; i++) {
                    generateDemoHistogram(map1);
                    generateDemoHistogram(map2);
                }
            } else {
                ExecutorService service = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                List<Future> futures = new LinkedList<>();
                int fileCount = FILE_COUNT;
                for (File simulation : new File(rootFolder).listFiles()) {
                    futures.add(service.submit(() -> generateHistogram(simulation, Math.random() >= 0.5 ? map1 : map2, MIN_AUCTION_DAY)));
                    if (fileCount-- == 0) {
                        break;
                    }
                }
                service.shutdown();
                while (!service.awaitTermination(1, TimeUnit.SECONDS)) {
                }
            }

        }

        private static Map<Double, Double> cumulative(Map<Double, Double> histogram) {
            Map<Double, Double> map = new HashMap<>();
            double cumulative = 0;
            double d;
            for (d = 0; round(d) <= MAX_BID; d += BUCKET_SIZE) {
                cumulative += histogram.getOrDefault(round(d), 0.0);
                map.put(round(d), cumulative);
            }
            for (d = 0; round(d) <= MAX_BID; d += BUCKET_SIZE) {
                map.put(round(d), map.get(round(d))/cumulative);
            }
            return map;
        }

        private static Map<Double, Map<Double, Double>> reduceHistograms(Map<Double, List<Map<Double, AtomicLong>>> megaMap, boolean shuoldNormalize) {
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
            for (Double reserve : reducedMap.keySet()) {
                for (Double d2 : reducedMap.get(reserve).keySet()) {
                    reducedMap2.putIfAbsent(reserve, new HashMap<>());
                    countMap.putIfAbsent(reserve, 0.0);

                    countMap.put(reserve, countMap.get(reserve) + reducedMap.get(reserve).get(d2).get());
                    reducedMap2.get(reserve).put(d2, (double) reducedMap.get(reserve).get(d2).get());
                }
            }

            if (shuoldNormalize) {
                for (Double reserve : reducedMap.keySet()) {
                    for (Double d2 : reducedMap.get(reserve).keySet()) {
                        reducedMap2.get(reserve).put(d2, reducedMap2.get(reserve).get(d2) / countMap.get(reserve));
                    }
                }
            }
//            List<Double> bidList = new ArrayList<>(10000);
//            printChart(bidList, String.format("t:\\%.02f", 1000 * reservePrice));
            return reducedMap2;
        }

//    private static double compareDatasets(Map<Double, AtomicLong> h1, Map<Double, AtomicLong> h2) {
//        double sum = 0;
//        double step = 1.0 / Math.pow(10, PRECISION);
//
//        long sum1 = 0, sum2 = 0;
//        for (AtomicLong al : h1.values()) {
//            sum1 += al.get();
//        }
//        for (AtomicLong al : h2.values()) {
//            sum2 += al.get();
//        }
//
//        for (double i = 0; i < 1; i += step) {
//            double p1i = (1.0 * h1.getOrDefault(ReservePriceAnalysis.round(i), new AtomicLong(0)).get()) / sum1;
//            double p2i = 1.0 * h2.getOrDefault(ReservePriceAnalysis.round(i), new AtomicLong(0)).get() / sum2;
//            sum += abs(p1i - p2i);
//        }
//        return ReservePriceAnalysis.round(sum);
//    }

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

    private static void generateDemoHistogram(Map<Double, List<Map<Double, AtomicLong>>> map) {
        Map<Double, AtomicLong> histogram = new HashMap<>();
        Random r = new Random(System.currentTimeMillis());
        double reservePrice = round(0.00002 + (0.0002 - 0.00002) * r.nextDouble(), 0.00002);
        List<Double> bidList = new ArrayList<>(10000);

        int day = 0;
        for (int i = 0; i < 1000000; i++) {
            double bid = round(MIN_BID + (MAX_BID - MIN_BID) * r.nextDouble(), BUCKET_SIZE);
            bidList.add(bid);
            histogram.putIfAbsent(bid, new AtomicLong(0));
            histogram.get(bid).incrementAndGet();
        }

//        printChart(bidList, String.format("t:\\%.02f", 1000 * reservePrice));
        synchronized (map) {
            map.putIfAbsent(reservePrice, new LinkedList<>());
            map.get(reservePrice).add(histogram);
        }
        System.gc();
    }

        private static void generateHistogram(File simulation, Map<Double, List<Map<Double, AtomicLong>>> map, int minAuctionDay) {
            List<Container> containers = readRecords(simulation);


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
                        double bid = round(entry.getBid(), BUCKET_SIZE);
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
            System.gc();
        }

        private static double round(double bid, double bucketSize) {
            return (int) (bid * 1 / bucketSize) / (1.0 / bucketSize);
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
            int scale = (int) Math.pow(10, PRECISION);
            return (double) Math.round(value * scale) / scale;
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

            Stopwatch stopwatch = new Stopwatch().start();


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
