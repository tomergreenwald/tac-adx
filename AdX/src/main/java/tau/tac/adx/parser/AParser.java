package tau.tac.adx.parser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.parser.Auctions.ABMessage;
import tau.tac.adx.parser.Auctions.AMessage;
import tau.tac.adx.parser.Auctions.AMessage.Builder;
import tau.tac.adx.parser.Auctions.AdType;
import tau.tac.adx.parser.Auctions.Age;
import tau.tac.adx.parser.Auctions.BMessage;
import tau.tac.adx.parser.Auctions.Device;
import tau.tac.adx.parser.Auctions.Gender;
import tau.tac.adx.parser.Auctions.Income;
import tau.tac.adx.report.adn.AdNetworkKey;
import tau.tac.adx.report.adn.AdNetworkReport;
import tau.tac.adx.report.adn.AdNetworkReportEntry;
import edu.umich.eecs.tac.Parser;

/**
 * <code>GeneralParser</code> is a simple example of a TAC Adx parser that
 * prints out a variety of messages received in a simulation from the simulation
 * log file.
 * <p>
 * <p/>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC Adx log processing.
 * 
 * @author Mariano Schain
 * @author - greenwald
 * 
 * @see edu.umich.eecs.tac.Parser
 */
public class AParser extends Parser {

//	Map<Integer, Map<AdNetworkKey, AdNetworkReportEntry>> map = new HashMap<Integer, Map<AdNetworkKey, AdNetworkReportEntry>>();
	FileOutputStream fileOutputStream;

	public AParser(LogReader reader, ConfigManager configManager) {
		super(reader);
		try {
			fileOutputStream = new FileOutputStream("C:\\temp\\2015_03_07\\rab_messages.buf", true);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {

		if (content instanceof AdNetworkReport) {
			AdNetworkReport adNetworkReport = (AdNetworkReport) content;
			for (AdNetworkKey adNetworkKey : adNetworkReport) {
				AdNetworkReportEntry entry = adNetworkReport
						.getAdNetworkReportEntry(adNetworkKey);
//				if (entry != null) {
//					if (!map.containsKey(sender)) {
//						map.put(sender,
//								new HashMap<AdNetworkKey, AdNetworkReportEntry>());
//					}
//					map.get(sender).put(adNetworkKey, entry);
//				}
				
				Builder builder = AMessage.newBuilder();
				if(adNetworkKey.getAdType() != tau.tac.adx.ads.properties.AdType.text) {
					int i = 0;
				}
				builder.setAdType(AdType.valueOf(adNetworkKey.getAdType().ordinal()));
				builder.setDevice(Device.valueOf(adNetworkKey.getDevice().ordinal()));
				builder.setAge(Age.valueOf(adNetworkKey.getAge().ordinal()));
				builder.setGender(Gender.valueOf(adNetworkKey.getGender().ordinal()));
				builder.setIncome(Income.valueOf(adNetworkKey.getIncome().ordinal()));
				AMessage aMessage = builder.build();
				
				tau.tac.adx.parser.Auctions.BMessage.Builder builder2 = BMessage.newBuilder();
				builder2.setImpressions(entry.getWinCount());
				builder2.setCost((float) entry.getCost());
				BMessage bMessage = builder2.build();
				
				tau.tac.adx.parser.Auctions.ABMessage.Builder builder3 = ABMessage.newBuilder();
				builder3.setAMessage(aMessage);
				builder3.setBMessage(bMessage);
				ABMessage abMessage = builder3.build();
				try {
					abMessage.writeTo(fileOutputStream);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
