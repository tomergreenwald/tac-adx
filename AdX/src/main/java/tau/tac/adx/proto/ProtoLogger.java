package tau.tac.adx.proto;

import com.google.protobuf.Message;
import tau.tac.adx.parser.Auctions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Tomer on 13/08/2016.
 */
public class ProtoLogger {
    static OutputStream outputStream;
    static String LOG_PATH = "logs/proto";
    static String LOG_FILE_NAME = "sim_%d.proto.gz";

    public static void initOutputStream(int simulationId) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            File logFolder = new File(LOG_PATH);
            if (!logFolder.exists()) {
                logFolder.mkdir();
            }
            outputStream = new GZIPOutputStream(new FileOutputStream(String.format(LOG_PATH +"/" + LOG_FILE_NAME, simulationId)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void log(Message message) {
        try {
            Auctions.Container.Builder containerBuilder = Auctions.Container.newBuilder();
            if (message instanceof Auctions.NewDay) {
                containerBuilder.setNewDay((Auctions.NewDay) message);
            } else if (message instanceof Auctions.AdxBidList) {
                containerBuilder.setBidList((Auctions.AdxBidList) message);
            }
            containerBuilder.build().writeDelimitedTo(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeOutputStream() {
        try {
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);//replace throw runtime with catch
        }
    }
}
