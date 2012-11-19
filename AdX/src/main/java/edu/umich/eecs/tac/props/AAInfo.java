/*
 * ContextFactory.java
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
package edu.umich.eecs.tac.props;

import se.sics.isl.transport.Context;
import se.sics.isl.transport.ContextFactory;
import se.sics.tasim.props.*;

/**
 * AAInfo is a context factory that provides the allowable transports
 * for the TAC/AA simulation.
 *
 * @author Lee Callender, Patrick Jordan
 */
public class AAInfo implements ContextFactory {
    /**
     * Basic context name.
     */
    private static final String CONTEXT_NAME = "aacontext";

    /**
     * Cache of the last created context (since contexts should be constants).
     */
    private static Context lastContext;

    /**
     * Creates a new AA context factory.
     */
    public AAInfo() {
    }

    /**
     * Adds the allowable transports to the context.
     *
     * @return the base context with new transports added.
     */
    public final Context createContext() {
        return createContext(null);
    }

    /**
     * Creates the allowable transports in a {@link Context}.
     *
     * @param parentContext the parent context
     * @return the context with new transports added.
     */
    public final Context createContext(final Context parentContext) {
        Context context = lastContext;
        if (context != null && context.getParent() == parentContext) {
            return context;
        }

        context = new Context(CONTEXT_NAME, parentContext);
        context.addTransportable(new Ping());

        context.addTransportable(new Alert());
        context.addTransportable(new BankStatus());
        context.addTransportable(new AdminContent());
        context.addTransportable(new SimulationStatus());
        context.addTransportable(new StartInfo());
        context.addTransportable(new SlotInfo());
        context.addTransportable(new ReserveInfo());
        context.addTransportable(new PublisherInfo());
        context.addTransportable(new ServerConfig());
        context.addTransportable(new Query());
        context.addTransportable(new Product());
        context.addTransportable(new Ad());
        context.addTransportable(new AdLink());
        context.addTransportable(new SalesReport());
        context.addTransportable(new SalesReport.SalesReportEntry());
        context.addTransportable(new QueryReport());
        context.addTransportable(new QueryReport.QueryReportEntry());
        context.addTransportable(new QueryReport.DisplayReportEntry());
        context.addTransportable(new QueryReport.DisplayReport());
        context.addTransportable(new RetailCatalog());
        context.addTransportable(new RetailCatalog.RetailCatalogEntry());
        context.addTransportable(new BidBundle());
        context.addTransportable(new BidBundle.BidEntry());
        context.addTransportable(new Ranking());
        context.addTransportable(new Ranking.Slot());
        context.addTransportable(new Pricing());
        context.addTransportable(new UserClickModel());
        context.addTransportable(new Auction());
        context.addTransportable(new AdvertiserInfo());
        context.addTransportable(new ManufacturerComponentComposable());
        context.addTransportable(new UserPopulationState());
        context.addTransportable(new UserPopulationState.UserPopulationEntry());

        // Cache the last context
        lastContext = context;
        return context;
    }
}
