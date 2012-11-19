/*
 * DayChanger.java
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

package edu.umich.eecs.tac.logviewer.gui;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/**
 * Adapted from TAC-SCM/SICS
 */
public class DayChanger {
    protected final int SLIDER_DELAY = 150;

    JPanel mainPane, buttonPane;
    JSlider daySlider;
    JButton nextDayButton, prevDayButton, lastDayButton, firstDayButton;
    JLabel dayLabel;

    ActionListener actionListeners = null;

    PositiveBoundedRangeModel dayModel;

    public DayChanger(PositiveBoundedRangeModel dm) {
	    dayModel = dm;

	    mainPane = new JPanel();
	    mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
	    mainPane.setBorder(BorderFactory.createTitledBorder
			   (BorderFactory.createEtchedBorder(),
			    " Day Changer "));

	    buttonPane = new JPanel();
	    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

	    daySlider = new JSlider();
	    daySlider.setMinimum(0);
	    daySlider.setMaximum(dayModel.getLast());
	    daySlider.setValue(dayModel.getCurrent());
	    daySlider.addChangeListener(new ChangeListener() {
		  int value = -1;

		  Timer timer = new Timer(SLIDER_DELAY, new ActionListener() {
			  public void actionPerformed(ActionEvent evt) {
			    dayModel.setCurrent(value);

			    // Stop timer if slider was released
			    if (!daySlider.getValueIsAdjusting())
				    timer.stop();
			  }
		  });

		public void stateChanged(ChangeEvent ce) {
		    value = daySlider.getValue();

		    // Start timer for updating if it isn't running
		    if(!timer.isRunning())
			timer.start();
		}
	    });

	dayLabel = new JLabel(dayModel.getCurrent() + " / " +
			      dayModel.getLast());

	dayModel.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent ce) {
		    daySlider.setMaximum(dayModel.getLast());
		    daySlider.setValue(dayModel.getCurrent());
		    dayLabel.setText(dayModel.getCurrent() +
				     " / " +
				     dayModel.getLast());
		}
	    });


	nextDayButton = new JButton(">");
	prevDayButton = new JButton("<");
	lastDayButton = new JButton(">|");
	firstDayButton = new JButton("|<");

	nextDayButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	prevDayButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	lastDayButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
	firstDayButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

	nextDayButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    dayModel.changeCurrent(1);
		}
	    });

	prevDayButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    dayModel.changeCurrent(-1);
		}
	    });

	firstDayButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    dayModel.setCurrent(0);
		}
	    });

	lastDayButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    dayModel.setCurrent(dayModel.getLast());
		}
	    });

	buttonPane.add(firstDayButton);
	buttonPane.add(prevDayButton);
	buttonPane.add(Box.createHorizontalGlue());
	buttonPane.add(dayLabel);
	buttonPane.add(Box.createHorizontalGlue());
	buttonPane.add(nextDayButton);
	buttonPane.add(lastDayButton);

	mainPane.add(buttonPane);
	mainPane.add(daySlider);
    }

  public JPanel getMainPane() {
	    return mainPane;
  }
} // DayChanger
