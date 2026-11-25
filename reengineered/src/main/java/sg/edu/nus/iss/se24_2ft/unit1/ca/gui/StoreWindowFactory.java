package sg.edu.nus.iss.se24_2ft.unit1.ca.gui;

import sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap.StoreContext;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** The top-level window/process lifecycle stays outside headless feature composition. */
public final class StoreWindowFactory {
    private StoreWindowFactory() { }
    public static MainFrame create(StoreContext context) {
        StoreFeatures features = new StoreFeatures(context);
        MainFrame frame = new MainFrame();
        features.panels().forEach(frame::addFeaturePanel);
        frame.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) {
                features.close();
                System.exit(0);
            }
        });
        frame.pack();
        return frame;
    }
}
