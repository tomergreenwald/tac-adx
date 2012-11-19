/*
 * GraphicUtils.java
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

package edu.umich.eecs.tac.viewer;

import edu.umich.eecs.tac.props.Product;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick R. Jordan, Lee Callender
 */
public class GraphicUtils {
    private GraphicUtils() {
    }

    private static final ImageIcon GENERIC = new ImageIcon(GraphicUtils.class.getResource("/generic_regular.gif"));
    private static final ImageIcon INVALID = new ImageIcon(GraphicUtils.class.getResource("/invalid_regular.gif"));

    private static final Map<Product, ImageIcon> PRODUCT_ICONS;

    static {
        PRODUCT_ICONS = new HashMap<Product, ImageIcon>();
        for (String manufacturer : new String[]{"lioneer", "pg", "flat"}) {
            for (String component : new String[]{"tv", "dvd", "audio"}) {
                PRODUCT_ICONS.put(new Product(manufacturer, component), new ImageIcon(GraphicUtils.class.getResource(String.format("/%s_%s_regular.gif", manufacturer, component))));
            }
        }
    }

    private static final Map<String, ImageIcon> MANUFACTURER_ICONS;

    static {
        MANUFACTURER_ICONS = new HashMap<String, ImageIcon>();
        for (String name : new String[]{"lioneer", "pg", "flat"}) {
            MANUFACTURER_ICONS.put(name, new ImageIcon(GraphicUtils.class.getResource(String.format("/%s_thumb.gif", name))));
        }
    }

    private static final Map<String, ImageIcon> COMPONENT_ICONS;

    static {
        COMPONENT_ICONS = new HashMap<String, ImageIcon>();
        for (String name : new String[]{"tv", "dvd", "audio"}) {
            COMPONENT_ICONS.put(name, new ImageIcon(GraphicUtils.class.getResource(String.format("/%s_thumb.gif", name))));
        }
    }

    public static ImageIcon genericIcon() {
        return GENERIC;
    }

    public static ImageIcon invalidIcon() {
        return INVALID;
    }

    public static ImageIcon iconForProduct(Product product) {
        return PRODUCT_ICONS.get(product);
    }

    public static ImageIcon iconForManufacturer(String manufacturer) {
        return MANUFACTURER_ICONS.get(manufacturer);
    }

    public static ImageIcon iconForComponent(String component) {
        return COMPONENT_ICONS.get(component);
    }


}
