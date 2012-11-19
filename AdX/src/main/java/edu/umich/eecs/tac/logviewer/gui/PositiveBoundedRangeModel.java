/**
 * SICS ISL Java Utilities
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * PositiveBoundedRangeModel
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Sun Feb 02 13:15:02 2003
 * Updated : $Date: 2003/07/15 08:48:25 $
 *           $Revision: 1.2 $
 */

package edu.umich.eecs.tac.logviewer.gui;

import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;
import javax.swing.event.ChangeListener;



/**
* The <code>PositiveBoundedRangeModel</code> class is similar to a
* BoundedRangeModel, but it is implicitly limited by 0 at the lower end.
* It holds a last and a current value, where 0 <= current <= last.
*
* @author - SICS, Lee Callender
*/
public class PositiveBoundedRangeModel {
    protected ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();

    protected int last = 0;
    protected int current = 0;

    public PositiveBoundedRangeModel() {
    }

    /**
     * @return Last (maximum) day.
     */
    public int getLast() {
        return last;
    }

    /**
     * @param newLast Sets the last (maximum) day.
     */
    public void setLast(int newLast) {
        setDayProperties(current, newLast);
    }

    /**
     * @return Current day - an <code>int</code>.
     */
    public int getCurrent() {
        return current;
    }

    /**
     * Sets the current day in the model, and fires a change event.
     *
     * @param newCurrent The new current day.
     */
    public void setCurrent(int newCurrent) {
	setDayProperties(newCurrent, last);
    }

    /**
     * Changes the current day, and fires a change event.
     *
     * @param change Modifies the current day by adding change
     *               days to it.
     */
    public void changeCurrent(int change) {
	setDayProperties(current + change, last);
    }


    /**
     * Sets the current and last day used by the model. If the condition
     * 0 <= newCurrent <= newLast is not met, nothing is done.
     *
     * @param newCurrent an <code>int</code> value
     * @param newLast an <code>int</code> value
     */
    public void setDayProperties(int newCurrent, int newLast) {
      if(newCurrent > newLast || newCurrent < 0 || newLast < 0 ||
	        (newCurrent == current && newLast == last))
	       return;

	    current = newCurrent;
	    last = newLast;
	    fireStateChanged();
    }

    /**
     * Adds a <code>ChangeListener</code> to the model. The listener is
     * notified each time the model changes.
     *
     * @param l The <code>ChangeListener</code>
     */
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }

    /**
     * Removes a <code>ChangeListener</code> from the model. The listener
     * will no longer be notified when the model changes.
     *
     * @param l The <code>ChangeListener</code>
     */
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }

    /**
     * Notifies registered <code>ChangeListeners</code>
     *
     */
    protected void fireStateChanged() {
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null) {
                    changeEvent = new ChangeEvent(this);
                }
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
} // PositiveBoundedRangeModel

