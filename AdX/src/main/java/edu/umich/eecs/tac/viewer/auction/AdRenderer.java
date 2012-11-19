/*
 * AdRenderer.java
 * 
 * Copyright (C) 2006-2009 Patrick R. Jordan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.umich.eecs.tac.viewer.auction;

import edu.umich.eecs.tac.props.Query;
import edu.umich.eecs.tac.viewer.GraphicUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrick R. Jordan, Lee Callender
 */
public class AdRenderer extends DefaultListCellRenderer {
    private String adCopy;
    private final Map<String, String> textCache;

    public AdRenderer(Query query) {
        textCache = new HashMap<String, String>();

        switch (query.getType()) {
            case FOCUS_LEVEL_ZERO:
                adCopy = "products";
                break;
            case FOCUS_LEVEL_ONE:
                adCopy = String.format("%s products", query.getManufacturer() == null ?
                        query.getComponent() :
                        query.getManufacturer());
                break;
            case FOCUS_LEVEL_TWO:
                adCopy = String.format("%s %s units", query.getManufacturer(), query.getComponent());
                break;
        }
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        ResultsItem item = (ResultsItem) value;

        ImageIcon icon = GraphicUtils.iconForProduct(item.getAd().getProduct());

        if (icon == null) {
          if(item.getAd().isGeneric())
            icon = GraphicUtils.genericIcon();
          else
            icon = GraphicUtils.invalidIcon();
        }

        label.setIcon(icon);


        String text = textCache.get(item.getAdvertiser());
        if (text == null) {
            text = String.format("%s's %s", item.getAdvertiser(), adCopy);
            textCache.put(item.getAdvertiser(), text);
        }

        label.setText(text);


        return label;
    }
}
