package tau.tac.adx.report.demand;

import tau.tac.adx.demand.CampaignStats;
import edu.umich.eecs.tac.props.AbstractKeyedEntryList;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class CampaignReport extends
		AbstractKeyedEntryList<CampaignReportKey, CampaignReportEntry> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -383908225939942652L;

	@Override
	protected CampaignReportEntry createEntry(CampaignReportKey key) {
		return new CampaignReportEntry(key);
	}

	@Override
	protected final Class<CampaignReportEntry> entryClass() {
		return CampaignReportEntry.class;
	}

	public String toMyString() {
		StringBuilder stringBuilder = new StringBuilder();
		for (CampaignReportEntry entry : getEntries()) {
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
	 * @return {@link CampaignReportEntry}.
	 * 
	 */
	public CampaignReportEntry addReportEntry(
			CampaignReportKey campaignReportKey) {
		lockCheck();
		int index = addKey(campaignReportKey);
		return getEntry(index);
	}

	/**
	 * Retrieves a {@link CampaignReportEntry} keyed with a
	 * {@link campaignReportKey}.
	 * 
	 * @param campaignReportKey
	 *            {@link campaignReportKey}.
	 * @return {@link CampaignReportEntry}.
	 * 
	 */
	public CampaignReportEntry getCampaignReportEntry(
			CampaignReportKey campaignReportKey) {
		return getEntry(campaignReportKey);
	}

	/**
	 * Generates a {@link campaignReportKey} according to given parameters.
	 * 
	 * @param id
	 * @return Corresponding {@link CampaignReportKey}.
	 */
	private CampaignReportKey getKey(Integer id) {
		return new CampaignReportKey(id);
	}

	public void addStatsEntry(Integer campaignId, CampaignStats campaignStats) {
		CampaignReportKey key = getKey(campaignId);
		CampaignReportEntry reportEntry = getCampaignReportEntry(key);
		if (reportEntry == null) {
			reportEntry = addReportEntry(key);
			reportEntry.setCampaignStats(campaignStats);
		}
	}
}
