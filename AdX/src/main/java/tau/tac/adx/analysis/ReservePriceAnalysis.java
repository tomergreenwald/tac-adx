package tau.tac.adx.analysis;

import tau.tac.adx.parser.Auctions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Tomer on 07/08/2016.
 */
public class ReservePriceAnalysis {
    public static void main(String[] args) throws IOException {

        String originalFile = "T:\\log.protobuf";
        String reducedFile = "T:\\log.reduced.protobuf";
        String csvOutputFile = "T:\\log.reduced.csv";
        int minAuctionDay = 10;

        reduceRecords(originalFile, reducedFile);
        parseReserve(reducedFile, csvOutputFile, minAuctionDay);
        printFile(csvOutputFile);
    }

    private static void printFile(String filePath) throws IOException {
        byte[] data = readFile(filePath);
        System.out.println(new String(data));
    }

    private static void parseReserve(String inputFile, String outputFile, int minAuctionDay) throws IOException {

        Auctions.DataBundle dataBundle = getDataBundle(inputFile);
        HashMap<Double, HashMap<UO, AtomicLong>> map = new HashMap<>();

        for (Auctions.AuctionReport report : dataBundle.getReportsList()) {
            if (report.getDay() < minAuctionDay) {
                continue;
            }
            double reservePrice = report.getReservePrice();
            map.putIfAbsent(reservePrice, new HashMap<>());
            HashMap<UO, AtomicLong> rMap = map.get(reservePrice);
            UO uo = reservePrice < report.getSecondBid() ? UO.Under : (reservePrice < report.getFirstBid() ? UO.Between : UO.Over);
            rMap.putIfAbsent(uo, new AtomicLong(0));
            rMap.get(uo).incrementAndGet();
        }
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write("reserve price,\t\tunder,\t\tbetween,\t\tover\n".getBytes());
        for (double reservePrice : map.keySet()) {
            HashMap<UO, AtomicLong> rMap = map.get(reservePrice);
            if (rMap == null) {
                return;
            }
            long total = rMap.getOrDefault(UO.Under, new AtomicLong(0)).get() +
                    rMap.getOrDefault(UO.Between, new AtomicLong(0)).get() +
                    rMap.getOrDefault(UO.Over, new AtomicLong(0)).get();
            fos.write(String.format("%f,\t\t%f,\t\t%f,\t\t%f\n",
                    reservePrice,
                    1.0 * rMap.get(UO.Under).get() / total,
                    1.0 * rMap.get(UO.Between).get() / total,
                    1.0 * rMap.get(UO.Over).get() / total
            ).getBytes());
        }
        fos.close();
    }

    private static Auctions.DataBundle getDataBundle(String input) throws IOException {

        byte[] data = readFile(input);
        System.out.println("De-serializing data");
        long pre = System.currentTimeMillis();
        Auctions.DataBundle dataBundle = Auctions.DataBundle.parseFrom(data);
        long post = System.currentTimeMillis();
        System.out.println(String.format("Serialized in %d seconds", (post - pre) / 1000));
        System.out.println(String.format("Parsed %d records", dataBundle.getReportsCount()));
        return dataBundle;
    }

    private static byte[] readFile(String inputFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        File file = new File(inputFile);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }

    private static void reduceRecords(String input, String output) throws IOException {
        Auctions.DataBundle dataBundle = getDataBundle(input);

        System.out.println("Reducing data");
        long pre = System.currentTimeMillis();

        double keepRatio = 0.01;
        FileOutputStream fos = new FileOutputStream(output);
        Auctions.DataBundle.Builder builder = Auctions.DataBundle.newBuilder();
        int counter = 0;
        for (int i = 0; i < dataBundle.getReportsCount(); i++) {
            if (Math.random() < keepRatio) {
                builder.addReports(dataBundle.getReports(i));
                counter++;
            }
        }
        Auctions.DataBundle reducedDataBundle = builder.build();
        reducedDataBundle.writeTo(fos);
        long post = System.currentTimeMillis();
        System.out.println(String.format("Reduced %d records in %d seconds", counter, (post - pre) / 1000));
        fos.close();
    }

    enum UO {
        Under, Between, Over;
    }

}
