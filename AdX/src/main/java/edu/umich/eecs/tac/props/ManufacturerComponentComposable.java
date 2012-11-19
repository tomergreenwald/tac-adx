/*
 * ManufacturerComponentComposable.java
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

import se.sics.isl.transport.TransportReader;
import se.sics.isl.transport.TransportWriter;

import java.text.ParseException;

/**
 * The manufacturer component composable holds a manufacturer and component.  This is the superclass of the
 * {@link Query} and {@link Product} classes.
 * @author Patrick Jordan
 */
public class ManufacturerComponentComposable extends AbstractTransportable {
    /**
     * The manufacturer key.
     */
    private static final String MANUFACTURER_KEY = "manufacturer";
    /**
     * The component key.
     */
    private static final String COMPONENT_KEY = "component";
    /**
     * Cached hashcode.
     */
    private int hashCode;
    /**
     * The string representing the manufacturer. May be <code>null</code>.
     */
    private String manufacturer;
    /**
     * The string representing the component. May be <code>null</code>.
     */
    private String component;

    /**
     * Creates a manufacturer component composable.
     */
    public ManufacturerComponentComposable() {
        calculateHashCode();
    }

    /**
     * The string representing the manufacturer.
     *
     * @return the string representing the manufacturer. May be <code>null</code>.
     */
    public final String getManufacturer() {
        return manufacturer;
    }

    /**
     * Set the string representing the manufacturer.
     *
     * @param manufacturer the string representing the manufacturer. May be <code>null</code> if the manufacturer is
     *                     not included in the query.
     */
    public final void setManufacturer(final String manufacturer) {
        lockCheck();
        this.manufacturer = manufacturer;
        calculateHashCode();
    }

    /**
     * Returns the string representing the component.
     *
     * @return the string representing the component. May be <code>null</code>.
     */
    public final String getComponent() {
        return component;
    }

    /**
     * Sets the string representing the component.
     *
     * @param component the string representing the component. May be <code>null</code> if the component is not included
     *                  in the query.
     * @throws IllegalStateException if the object is locked.
     */
    public final void setComponent(final String component) throws IllegalStateException {
        lockCheck();
        this.component = component;
        calculateHashCode();
    }

    /**
     * Reads the manufacturer and component from the reader.
     * @param reader the reader to read data from.
     * @throws ParseException if an exception occurs while reading the manufacturer and component.
     */
    @Override
    protected final void readWithLock(final TransportReader reader) throws ParseException {
        this.setManufacturer(reader.getAttribute(MANUFACTURER_KEY, null));
        this.setComponent(reader.getAttribute(COMPONENT_KEY, null));
        calculateHashCode();
    }

    /**
     * Writes the manufacturer and component to the writer.
     * @param writer the writer to write data to.
     */
    @Override
    protected final void writeWithLock(final TransportWriter writer) {
        if (getManufacturer() != null) {
            writer.attr(MANUFACTURER_KEY, getManufacturer());
        }

        if (getComponent() != null) {
            writer.attr(COMPONENT_KEY, getComponent());
        }
    }

    /**
     * Returns the precalculated hash code.
     * @return precalculated hash code.
     */
    @Override
    public final int hashCode() {
        return hashCode;
    }

    /**
     * Returns the calculated hash code.
     */
    protected final void calculateHashCode() {
        int result;
        result = (manufacturer != null ? manufacturer.hashCode() : 0);
        result = 31 * result + (component != null ? component.hashCode() : 0);

        hashCode = result;
    }

    /**
     * Returns a string representation of the manufacturer component composable.
     * @return a string representation of the manufacturer component composable.
     */
    public final String toString() {
        return String.format("(%s (%s,%s))", this.getClass().getSimpleName(), getManufacturer(), getComponent());
    }

    /**
     * Returns <code>true</code> if a manufacturer component composable has the same manufacturer and component.
     * @param o the other manufacturer component  composable.
     * @return <code>true</code> if a manufacturer component composable has the same manufacturer and component.
     */
    protected final boolean composableEquals(final ManufacturerComponentComposable o) {
        if (o == null) {
            return false;
        }

        if (isLocked() != o.isLocked()) {
            return false;
        }

        if (component != null ? !component.equals(o.component) : o.component != null) {
            return false;
        }

        return !(manufacturer != null ? !manufacturer.equals(o.manufacturer) : o.manufacturer != null);

    }

    /**
     * Returns <code>true</code> if the object is a manufacturer component composable that has the same manufacturer and
     * component.
     *
     * @param o the object being compared.
     * @return <code>true</code> if the object is a manufacturer component composable that has the same manufacturer and
     * component.
     */
    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || hashCode() != o.hashCode() || getClass() != o.getClass()) {
            return false;
        }

        return composableEquals((ManufacturerComponentComposable) o);
    }
}
