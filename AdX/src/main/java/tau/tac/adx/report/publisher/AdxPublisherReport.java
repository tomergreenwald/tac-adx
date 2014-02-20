/*
 * QueryReport.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.report.publisher;

import tau.tac.adx.AdxManager;
import tau.tac.adx.props.AdxQuery;
import tau.tac.adx.props.PublisherCatalogEntry;
import tau.tac.adx.publishers.AdxPublisher;
import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

/**
 * Query report contains impressions, clicks, cost, average position, and ad
 * displayed by the advertiser for each query class during the period as well as
 * the positions and displayed ads of all advertisers during the period for each
 * query class.
 * 
 * @author Ben Cassell, Patrick Jordan, Lee Callender
 * @author greenwald
 */
public class AdxPublisherReport extends
		AbstractKeyedEntryList<PublisherCatalogEntry, AdxPublisherReportEntry> {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -7957495904471250085L;

	/**
	 * Returns the {@link AdxPublisherReportEntry} class.
	 * 
	 * @return the {@link AdxPublisherReportEntry} class.
	 */
	@Override
	protected final Class<AdxPublisherReportEntry> entryClass() {
		return AdxPublisherReportEntry.class;
	}

	/**
	 * @see edu.umich.eecs.tac.props.AbstractKeyedEntryList#createEntry(java.lang.Object)
	 */
	@Override
	protected AdxPublisherReportEntry createEntry(PublisherCatalogEntry key) {
		AdxPublisherReportEntry entry = new AdxPublisherReportEntry(key);
		return entry;
	}

	/**
	 * Adds an {@link AdxPublisherReportEntry} keyed with a
	 * {@link PublisherCatalogEntry}.
	 * 
	 * @param publisher
	 *            {@link PublisherCatalogEntry}.
	 * @param publisherReportEntry
	 *            AdxPublisherReportEntry.
	 * 
	 */
	public void addPublisherReportEntry(PublisherCatalogEntry publisher,
			AdxPublisherReportEntry publisherReportEntry) {
		lockCheck();
		int index = addKey(publisher);
		AdxPublisherReportEntry entry = getEntry(index);
		entry.setPopularity(publisherReportEntry.getPopularity());
		entry.setAdTypeOrientation(publisherReportEntry.getAdTypeOrientation());
	}

	/**
	 * Retrieves an {@link AdxPublisherReportEntry} keyed with a
	 * {@link PublisherCatalogEntry}.
	 * 
	 * @param publisher
	 *            {@link PublisherCatalogEntry}.
	 * @return {@link AdxPublisherReportEntry}.
	 * 
	 */
	public AdxPublisherReportEntry getPublisherReportEntry(
			PublisherCatalogEntry publisher) {
		return getEntry(publisher);
	}

	/**
	 * Retrieves an {@link AdxPublisherReportEntry} keyed with a
	 * {@link PublisherCatalogEntry}.
	 * 
	 * @param publisher
	 *            {@link PublisherCatalogEntry}.
	 * @return {@link AdxPublisherReportEntry}.
	 * 
	 */
	public AdxPublisherReportEntry getPublisherReportEntry(String publisher) {
		PublisherCatalogEntry publisherEntry = getPublisherCatalogEntry(publisher);
		return getEntry(publisherEntry);
	}

	/**
	 * @param publisher
	 *            {@link AdxPublisher} name.
	 * @return Corresponding {@link PublisherCatalogEntry}.
	 */
	private PublisherCatalogEntry getPublisherCatalogEntry(String publisher) {
		return new PublisherCatalogEntry(AdxManager.getInstance().getPublisher(
				publisher));
	}

	public void addQuery(AdxQuery query) {
		PublisherCatalogEntry publisherCatalogEntry = getPublisherCatalogEntry(query
				.getPublisher());
		AdxPublisherReportEntry publisherReportEntry = getPublisherReportEntry(publisherCatalogEntry);
		if (publisherReportEntry == null) {
			publisherReportEntry = new AdxPublisherReportEntry(
					publisherCatalogEntry);
			addPublisherReportEntry(publisherCatalogEntry, publisherReportEntry);
			publisherReportEntry.setReservePriceBaseline(AdxManager.getInstance()
					.getPublisher(query.getPublisher()).getReservePriceManager().getDailyBaselineAverage());
		}
		publisherReportEntry.addQuery(query);
	}
}
