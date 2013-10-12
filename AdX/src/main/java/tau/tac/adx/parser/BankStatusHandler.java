package tau.tac.adx.parser;

import se.sics.isl.util.IllegalConfigurationException;
import se.sics.tasim.logtool.LogReader;
import se.sics.tasim.logtool.LogHandler;

import java.text.ParseException;
import java.io.IOException;

/**
 * <code>BankStatusHandler</code> is a simple example of a log handler that
 * uses a specific parser to extract information from log files.
 *
 * @author - Lee Callender
 */
public class BankStatusHandler extends LogHandler {

    public BankStatusHandler() {
    }

    /**
     * Invoked when a new log file should be processed.
     *
     * @param reader the log reader for the log file.
     */
    protected void start(LogReader reader) throws IllegalConfigurationException, IOException, ParseException {
        BankStatusParser parser = new BankStatusParser(reader);
        parser.start();
        parser.stop();
    }
}

