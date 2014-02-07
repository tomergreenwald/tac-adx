package tau.tac.adx.report.demand.campaign.auction;

import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignAuctionReport extends
		AbstractKeyedEntryList<CampaignAuctionReportKey, CampaignAuctionReportEntry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -383908225939942653L;

	@Override
	protected CampaignAuctionReportEntry createEntry(CampaignAuctionReportKey key) {
		return new CampaignAuctionReportEntry(key);
	}

	@Override
	protected final Class<CampaignAuctionReportEntry> entryClass() {
		return CampaignAuctionReportEntry.class;
	}

	public String toMyString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (CampaignAuctionReportEntry entry : getEntries()) {
			stringBuilder.append(entry.getKey()).append(" : ").append(entry)
					.append("\n");
		}
		return "CampaignReport: " + stringBuilder;
	}

	/**
	 * Adds an {@link campaignReportKey} to the report.
	 * 
	 * @param campaignReportKey
	 *            {@link campaignReportKey}.
	 * @return {@link CampaignAuctionReportEntry}.
	 * 
	 */
	public CampaignAuctionReportEntry addReportEntry(
			CampaignAuctionReportKey campaignReportKey) {
		lockCheck();
		int index = addKey(campaignReportKey);
		return getEntry(index);
	}

	/**
	 * Retrieves a {@link CampaignAuctionReportEntry} keyed with a
	 * {@link campaignReportKey}.
	 * 
	 * @param campaignReportKey
	 *            {@link campaignReportKey}.
	 * @return {@link CampaignAuctionReportEntry}.
	 * 
	 */
	public CampaignAuctionReportEntry getCampaignReportEntry(
			CampaignAuctionReportKey campaignReportKey) {
		return getEntry(campaignReportKey);
	}

}
