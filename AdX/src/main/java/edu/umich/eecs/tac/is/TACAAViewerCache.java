/*
 * TACAAViewerCache.java
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
package edu.umich.eecs.tac.is;

import se.sics.tasim.is.common.ViewerCache;
import se.sics.tasim.is.EventWriter;
import se.sics.isl.transport.Transportable;

import java.util.logging.Logger;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;

import edu.umich.eecs.tac.props.RetailCatalog;
import edu.umich.eecs.tac.props.SlotInfo;
import edu.umich.eecs.tac.props.AdvertiserInfo;
import edu.umich.eecs.tac.props.PublisherInfo;
import static tau.tac.adx.sim.TACAdxConstants.*;

/**
 * @author Patrick Jordan, Lee Callender
 */
public class TACAAViewerCache extends ViewerCache {
    private static final Logger log = Logger.getLogger(TACAAViewerCache.class.getName());

    private RetailCatalog catalog;
    private SlotInfo slotInfo;
    private Map<Integer, AdvertiserInfo> advertiserInfos;
    private Map<Integer, PublisherInfo> publisherInfoMap;

    private int timeUnit;

    private static final int DU_AGENT = 0;
    private static final int DU_TYPE = 1;
    private static final int DU_VALUE = 2;
    private static final int DU_PARTS = 3;

    private int[] dataUpdatedConstants;
    private int dataUpdatedCount = 0;
    private int noCachedData = 0;

    private Hashtable cache = new Hashtable();

    public static final int MAX_CACHE = 60;

    public TACAAViewerCache() {
        advertiserInfos = new HashMap<Integer, AdvertiserInfo>();
        publisherInfoMap = new HashMap<Integer, PublisherInfo>();
    }

    private void addToCache(int agent, int type, int value) {
        String key = "" + agent + "_" + type;
        CacheEntry ce = (CacheEntry) cache.get(key);
        if (ce == null) {
            ce = new CacheEntry();
            ce.agent = agent;
            ce.type = type;
            ce.cachedData = new int[MAX_CACHE];
            cache.put(key, ce);
        }
        ce.addCachedData(value);
    }

    @Override
    public void writeCache(EventWriter eventWriter) {
        super.writeCache(eventWriter);

        if (catalog != null) {
            eventWriter.dataUpdated(TYPE_NONE, catalog);
        }
        if (slotInfo != null) {
            eventWriter.dataUpdated(TYPE_NONE, slotInfo);
        }
        for (Integer agent : publisherInfoMap.keySet()) {
            eventWriter.dataUpdated(agent, DU_PUBLISHER_INFO, publisherInfoMap.get(agent));
        }
        for (Integer agent : advertiserInfos.keySet()) {
            eventWriter.dataUpdated(agent, DU_ADVERTISER_INFO, advertiserInfos.get(agent));
        }
        if (timeUnit > 0) {
            eventWriter.nextTimeUnit(timeUnit);
        }
        if (dataUpdatedCount > 0) {
            for (int i = 0, n = dataUpdatedCount * DU_PARTS; i < n; i += DU_PARTS) {
                eventWriter.dataUpdated(dataUpdatedConstants[i + DU_AGENT],
                        dataUpdatedConstants[i + DU_TYPE],
                        dataUpdatedConstants[i + DU_VALUE]);
            }
        }

        Object[] keys = cache.keySet().toArray();
        if (keys != null) {
            for (int i = 0, n = keys.length; i < n; i++) {
                CacheEntry ce = (CacheEntry) cache.get(keys[i]);
                if (ce != null) {
                    eventWriter.intCache(ce.agent, ce.type, ce.getCache());
                }
            }
        }
    }

    public void nextTimeUnit(int timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public void dataUpdated(int agent, int type, int value) {
        super.dataUpdated(agent, type, value);

        if (type == DU_NON_SEARCHING
                || type == DU_NON_SEARCHING
                || type == DU_INFORMATIONAL_SEARCH
                || type == DU_FOCUS_LEVEL_ZERO
                || type == DU_FOCUS_LEVEL_ONE
                || type == DU_FOCUS_LEVEL_TWO) {
            addToCache(agent, type, value);
        }
    }

    @Override
    public void dataUpdated(int agent, int type, long value) {
        super.dataUpdated(agent, type, value);

        if ((type & DU_BANK_ACCOUNT) != 0) {
            addToCache(agent, type, (int) value);
        }
    }

    @Override
    public void dataUpdated(int type, Transportable value) {
        super.dataUpdated(type, value);

        Class valueType = value.getClass();
        if (valueType == RetailCatalog.class) {
            this.catalog = (RetailCatalog) value;
        } else if (valueType == SlotInfo.class) {
            this.slotInfo = (SlotInfo) value;
        }
    }

    @Override
    public void dataUpdated(int agent, int type, Transportable content) {
        super.dataUpdated(agent, type, content);

        if (type == DU_ADVERTISER_INFO && content.getClass() == AdvertiserInfo.class) {
            advertiserInfos.put(agent, (AdvertiserInfo) content);
        } else if (type == DU_PUBLISHER_INFO && content.getClass() == PublisherInfo.class) {
            publisherInfoMap.put(agent, (PublisherInfo) content);
        }
    }

    private static class CacheEntry {
        int agent;
        int type;
        int[] cachedData;
        int pos;
        int len;

        public void addCachedData(int value) {
            // System.out.println("**** CacheEntity: adding cache[" + pos + "]="
            // +
            // value);
            cachedData[pos] = value;
            pos = (pos + 1) % MAX_CACHE;
            if (len < MAX_CACHE) {
                len++;
            }
        }

        public int[] getCache() {
            int[] tmp = new int[len];
            int start = ((pos - len) + MAX_CACHE) % MAX_CACHE;
            for (int i = 0, n = len; i < n; i++) {
                tmp[i] = cachedData[(start + i) % MAX_CACHE];
            }
            return tmp;
        }
    }
}
