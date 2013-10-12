package tau.tac.adx.parser;

import java.util.List;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.logtool.LogReader;
import tau.tac.adx.props.PublisherCatalog;
import tau.tac.adx.props.PublisherCatalogEntry;
import edu.umich.eecs.tac.Parser;

/**
 * <code>BankStatusParser</code> is a simple example of a TAC AA parser that
 * prints out all advertiser's BankStatus received in a simulation from the
 * simulation log file.
 * <p>
 * <p/>
 * The class <code>Parser</code> is inherited to provide base functionality for
 * TAC AA log processing.
 * 
 * @author - Lee Callender
 * 
 * @see edu.umich.eecs.tac.Parser
 */
public class PublisherCatalogParser extends Parser {

	boolean first = true;

	public PublisherCatalogParser(LogReader reader) {
		super(reader);
		System.out.println("Participating publishers: \n");
	}

	@Override
	protected void dataUpdated(int type, Transportable content) {

	}

	@Override
	protected void message(int sender, int receiver, Transportable content) {
		if (first && content instanceof PublisherCatalog) {
			PublisherCatalog publisherCatalog = (PublisherCatalog) content;
			List<PublisherCatalogEntry> publishers = publisherCatalog
					.getPublishers();
			for (PublisherCatalogEntry publisherCatalogEntry : publishers) {
				System.out.println(publisherCatalogEntry.getPublisherName());
			}
			first = false;
		}
	}
}
