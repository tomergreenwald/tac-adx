package tau.tac.adx;

import se.sics.tasim.viewer.applet.ViewerApplet;

import javax.swing.*;
import java.applet.AppletStub;
import java.awt.*;

/**
 * Created by Tomer on 09/07/2016.
 */
public class AppletMain extends JApplet {


    public static void main(String[] args) {

        // create and set up the applet
        ViewerApplet applet = new ViewerApplet();
        applet.setPreferredSize(new Dimension(400, 300));
        applet.init("localhost", "tomer", "4042", "tau.tac.adx.props.AdxInfoContextFactory");


        // create a frame to host the applet, which is just another type of Swing Component
        JFrame mainFrame = new JFrame();
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add the applet to the frame and show it
        mainFrame.getContentPane().add(applet);
        mainFrame.pack();
        mainFrame.setVisible(true);

        // start the applet
        applet.start();
    }

    public void init() {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    JLabel label = new JLabel("Hello World");
                    label.setHorizontalAlignment(SwingConstants.CENTER);
                    add(label);
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't complete successfully");
        }
    }
}
