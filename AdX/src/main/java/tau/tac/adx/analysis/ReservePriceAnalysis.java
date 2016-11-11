package tau.tac.adx.analysis;

import com.google.common.io.ByteStreams;
import com.google.protobuf.InvalidProtocolBufferException;
import flatbuffers.FlatBufferBuilder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.io.CSV;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.mortbay.util.Credential;
import tau.tac.adx.parser.Auctions;
//import tau.tac.adx.parser.flatbuffers.AdxBidEntry;
//import tau.tac.adx.parser.flatbuffers.AdxQuery;
//import tau.tac.adx.parser.flatbuffers.NewDay;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Tomer on 07/08/2016.
 */
public class ReservePriceAnalysis {
    private static final String GZIP_FILE_ENDING = ".gz";
    private static final double EPSILON = 0.00000000001;

    public static void main(String[] args) throws IOException, InterruptedException {

        String agentGiza = "Giza";
        String agentBob = "Bob";
        String agentAdxperts = "Adxperts";
        String agentLosCaparos = "LosCaparos";


        String[] agents = {agentAdxperts, agentBob, agentLosCaparos};

//        for (String agent : agents) {
//            String rootFolder = "c:\\temp\\2016_08_13";
//
//            String originalFile = String.format("%s\\%s.protobuf%s", rootFolder, agent, GZIP_FILE_ENDING);
//            String reducedFile = String.format("%s\\%s.reduced.protobuf%s", rootFolder, agent, GZIP_FILE_ENDING);
//            String csvOutputFile = String.format("%s\\%s.csv", rootFolder, agent);
//            int minAuctionDay = 10;
//            double keepRatio = 0.01;
//
//            System.out.println(agent);
//            //compressRecords(originalFile);
//            //decompressRecords(originalFile);
//            //reduceRecords(originalFile, reducedFile, keepRatio);
//            parseReserve(reducedFile, csvOutputFile, minAuctionDay);
//            printFile(csvOutputFile);
//        }
        for (String agent : agents) {
            String rootFolder = String.format("c:\\temp\\2016_08_20\\%s", agent);
            Map<Double, List<Map<Double, AtomicLong>>> map1 = new HashMap<>();
            Map<Double, List<Map<Double, AtomicLong>>> map2 = new HashMap<>();
            int precision = 3;
            long pre = System.currentTimeMillis();
            ExecutorService service = Executors.newFixedThreadPool(8);
            List<Future> futures = new LinkedList<>();
            int fileCount = 900;
            for (File simulation : new File(rootFolder).listFiles()) {
//            int minAuctionDay = 10;
//            double keepRatio = 0.01;
//            convertContainer(containers.get(0));
                futures.add(service.submit(new Runnable() {
                    @Override
                    public void run() {
                        List<Auctions.Container> containers = null;
                        try {
                            containers = readRecords(simulation);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        generateHistogram(containers, Math.random()>0.5?map1:map2, precision);
                    }
                }));
                if (fileCount-- == 0) {
                    break;
                }
            }

            service.shutdown();
            while (!service.awaitTermination(1, TimeUnit.SECONDS)) {
            }
            System.out.println("Reducing maps");
            Map<Double, Map<Double, AtomicLong>> histogramMap1 = reduceHistograms(map1);
            Map<Double, Map<Double, AtomicLong>> histogramMap2 = reduceHistograms(map2);
//        for (Double reserve : map.keySet()) {
//            System.out.println(String.format("Parsing reserve price %f", reserve));
//            List<Map<Double, AtomicLong>> histogramList = map.get(reserve);
//            for (int i = 0; i < histogramList.size(); i++) {
//                for (int j = i + 1; j < histogramList.size(); j++) {
//                    double sum = compareDatasets(histogramList.get(i), histogramList.get(j), precision);
//                    System.out.println(String.format("\tComparing %d - %d : %f", i, j, sum));
//                }
//            }
//        }
            FileOutputStream fos = new FileOutputStream(String.format("t:\\%s.csv", agent));
            fos.write(',');
            ArrayList<Double> list1 = new ArrayList<>(histogramMap1.keySet());
            ArrayList<Double> list2 = new ArrayList<>(histogramMap2.keySet());
            Collections.sort(list1);
            for (Double reserve : list1) {
                fos.write(String.format("%f,", reserve).getBytes());
            }
            fos.write('\n');

            for (int i = 0; i < list1.size(); i++) {
                double reserve = list1.get(i);
                fos.write(String.format("%f,", reserve).getBytes());
                for (int j = 0; j < list2.size(); j++) {
                    double reserve2 = list2.get(j);
//                    if (j <= i) {
//                        fos.write(',');
//                        continue;
//                    }

                    Map<Double, AtomicLong> histogram1 = cumulative(histogramMap1.get(reserve), precision);
                    Map<Double, AtomicLong> histogram2 = cumulative(histogramMap2.get(reserve2), precision);
                    double sum = compareDatasets(histogram1, histogram2, precision);
                    fos.write(String.format("%f,", sum).getBytes());
//                    sum = compareDatasets(histogram1, histogram2, precision);
                }
                fos.write('\n');
            }
            long post = System.currentTimeMillis();
            System.out.println((post - pre) / 1000);
        }
    }

    private static Map<Double, AtomicLong> cumulative(Map<Double, AtomicLong> histogram, int precision) {
        Map<Double, AtomicLong> map = new HashMap<>();
        AtomicLong cumulative = new AtomicLong(0);
        double step = 1.0/Math.pow(10, precision);

        for (double i = 0; i < 1; i+=step) {
            cumulative.addAndGet(histogram.getOrDefault(round(i, precision), new AtomicLong(0)).get());
            map.put(round(i, precision), new AtomicLong(cumulative.longValue()));
        }
        return map;
    }

    private static Map<Double, Map<Double, AtomicLong>> reduceHistograms(Map<Double, List<Map<Double, AtomicLong>>> megaMap) {
        Map<Double, Map<Double, AtomicLong>> reducedMap = new HashMap<>();
        for (Double reserve : megaMap.keySet()) {
            reducedMap.putIfAbsent(reserve, new HashMap<>());
            Map<Double, AtomicLong> unifiedHistogram = reducedMap.get(reserve);
            for (Map<Double, AtomicLong> histogram : megaMap.get(reserve)) {
                for (Double val : histogram.keySet()) {
                    unifiedHistogram.putIfAbsent(val, new AtomicLong(0));
                    unifiedHistogram.get(val).addAndGet(histogram.get(val).get());
                }
            }
        }
        return reducedMap;
    }

    private static double compareDatasets(Map<Double, AtomicLong> h1, Map<Double, AtomicLong> h2, int precision) {
        double sum = 0;
        double step = 1.0/Math.pow(10, precision);

        long sum1 = 0, sum2 = 0;
        for (AtomicLong al : h1.values()) {
            sum1 += al.get();
        }
        for (AtomicLong al : h2.values()) {
            sum2 += al.get();
        }

        for (double i = 0; i < 1; i+=step) {
            double p1i = 1.0*h1.getOrDefault(round(i, precision), new AtomicLong(0)).get()/sum1;
            double p2i = 1.0*h2.getOrDefault(round(i, precision), new AtomicLong(0)).get()/sum2;
            sum += Math.abs(p1i - p2i);
        }
        return round(sum, 4);
    }

    private static void convertContainer(Auctions.Container container) {
        if (container.hasBidList()) {
            for (Auctions.AdxBidList.AdxBidEntry entry : container.getBidList().getEntriesList()) {
                //build acd bid list
                //int bidder = builder.createString(entry.getBidder());
                //
                FlatBufferBuilder builder = new FlatBufferBuilder(0);
                Auctions.AdxQuery adxQuery1 = entry.getAdxQuery();
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

    private static void generateHistogram(List<Auctions.Container> containers, Map<Double, List<Map<Double, AtomicLong>>> map, int precision) {
        Map<Double, AtomicLong> histogram = new HashMap<>();
        double maxBid = 0.5;
        double minBid = 0.00000000001;
        List<Double> bidList = new ArrayList<>(containers.size());
        double reservePrice = Double.NaN;
        for (Auctions.Container container : containers) {
            if (container.hasBidList()) {
                for (Auctions.AdxBidList.AdxBidEntry entry : container.getBidList().getEntriesList()) {
                    try {
                        double bid = round(entry.getBid(), precision);
                        if (bid < maxBid && bid > minBid) {
                            bidList.add(bid);
                            histogram.putIfAbsent(bid, new AtomicLong(0));
                            histogram.get(bid).incrementAndGet();
                        }
                        if (reservePrice != Double.NaN) {
                            reservePrice = entry.getReservePrice();
//                        if (map.size() !=0 && !map.containsKey(reservePrice)) {
//                            return;
//                        }
                        }
                    } catch (NumberFormatException e) {

                    }
                }
            }
        }
        printChart(bidList, reservePrice);
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

    private static void printChart(List<Double> bidList, double reservePrice) {
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
            File reserveFolder = new File(String.format("t:\\%.02f", 1000 * reservePrice));
            reserveFolder.mkdir();
            ChartUtilities.saveChartAsPNG(new File(String.format(reserveFolder.getAbsolutePath() + "\\histogram-%d.PNG", System.currentTimeMillis())), chart, width, height);
        } catch (IOException e) {
        }
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static List<Auctions.Container> readRecords(File simulation) throws IOException {
        InputStream is = new GZIPInputStream(new FileInputStream(simulation));
        List<Auctions.Container> containers = new LinkedList<>();
        Auctions.Container container = Auctions.Container.parseDelimitedFrom(is);
        while (container != null) {
            containers.add(container);
            try {

                container = Auctions.Container.parseDelimitedFrom(is);
            } catch (InvalidProtocolBufferException e) {

            }
        }
        return containers;
    }

    private static void compressRecords(String originalFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(originalFile);
        FileOutputStream fileOutputStream = new FileOutputStream(originalFile + GZIP_FILE_ENDING);
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
        System.out.println(new String(readFile(filePath)));
    }

    private static void parseReserve(String inputFile, String outputFile, int minAuctionDay) throws IOException {

        Auctions.DataBundle dataBundle = getDataBundle(inputFile);
        HashMap<Double, HashMap<UO, AtomicLong>> map = new HashMap<>();

        for (Auctions.AuctionReport report : dataBundle.getReportsList()) {
            if (report.getDay() < minAuctionDay) {
                continue;
            }

            double reservePrice = report.getReservePrice() * 1000;
            map.putIfAbsent(reservePrice, new HashMap<>());
            HashMap<UO, AtomicLong> rMap = map.get(reservePrice);
            UO uo = reservePrice < report.getSecondBid() ? UO.Under : (reservePrice < report.getFirstBid() ? UO.Between : UO.Over);
            if (report.getSecondBid() < EPSILON && uo == UO.Between) {
                //if the second bid is lower than epsilon we will count the reserve as UNDER both of them.
                uo = UO.Under;
            }
            if (report.getFirstBid() < EPSILON) {
                uo = UO.Irrelevant;
                continue;
            }
            rMap.putIfAbsent(uo, new AtomicLong(0));
            rMap.get(uo).incrementAndGet();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write("reserve price,\t\tirrelevant, \t\tunder,\t\tunder b,\t\tbetween,\tover\n".getBytes());
        for (double reservePrice : (Set<Double>) new TreeSet(map.keySet())) {
            HashMap<UO, AtomicLong> rMap = map.get(reservePrice);
            if (rMap == null) {
                return;
            }
            long total =
                    rMap.getOrDefault(UO.Irrelevant, new AtomicLong(0)).get() +
                            rMap.getOrDefault(UO.Under, new AtomicLong(0)).get() +
                            rMap.getOrDefault(UO.UnderBetween, new AtomicLong(0)).get() +
                            rMap.getOrDefault(UO.Between, new AtomicLong(0)).get() +
                            rMap.getOrDefault(UO.Over, new AtomicLong(0)).get();
            fos.write(String.format("%f,\t\t\t%.3f,\t\t\t\t%.3f,\t\t%.3f,\t\t\t%.3f,\t\t%.3f\n",
                    reservePrice / 1000,
                    1.0 * rMap.getOrDefault(UO.Irrelevant, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(UO.Under, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(UO.UnderBetween, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(UO.Between, new AtomicLong(0)).get() / total,
                    1.0 * rMap.getOrDefault(UO.Over, new AtomicLong(0)).get() / total
            ).getBytes());
        }
        fos.close();
    }

    private static Auctions.DataBundle getDataBundle(String input) throws IOException {

        byte[] data = readFile(input);
        long pre = System.currentTimeMillis();
        Auctions.DataBundle dataBundle = Auctions.DataBundle.parseFrom(data);
        long post = System.currentTimeMillis();
        return dataBundle;
    }

    private static byte[] readFile(String inputFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        InputStream is = fis;
        if (inputFile.endsWith(GZIP_FILE_ENDING)) {
            is = new GZIPInputStream(fis);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteStreams.copy(is, baos);
        return baos.toByteArray();
    }

    private static void reduceRecords(String input, String output, double keepRatio) throws IOException {
        Auctions.DataBundle dataBundle = getDataBundle(input);

        System.out.println("Reducing data");
        long pre = System.currentTimeMillis();


        FileOutputStream fos = new FileOutputStream(output);
        GZIPOutputStream gos = new GZIPOutputStream(fos);
        Auctions.DataBundle.Builder builder = Auctions.DataBundle.newBuilder();
        int counter = 0;
        for (int i = 0; i < dataBundle.getReportsCount(); i++) {
            if (Math.random() < keepRatio) {
                builder.addReports(dataBundle.getReports(i));
                counter++;
            }
        }
        Auctions.DataBundle reducedDataBundle = builder.build();
        reducedDataBundle.writeTo(gos);
        long post = System.currentTimeMillis();
        System.out.println(String.format("Reduced %d records in %d seconds", counter, (post - pre) / 1000));
        gos.close();
    }

    enum UO {
        Irrelevant, Under, UnderBetween, Between, Over;
    }


}
