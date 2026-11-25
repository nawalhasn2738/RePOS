package sg.edu.nus.iss.se24_2ft.unit1.ca;

import sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap.StoreContext;
import sg.edu.nus.iss.se24_2ft.unit1.ca.bootstrap.StorePaths;
import sg.edu.nus.iss.se24_2ft.unit1.ca.gui.LoginPanel;
import sg.edu.nus.iss.se24_2ft.unit1.ca.gui.MainFrame;
import sg.edu.nus.iss.se24_2ft.unit1.ca.gui.StoreWindowFactory;
import sg.edu.nus.iss.se24_2ft.unit1.ca.storekeeper.StoreKeeperManager;

/** Launcher: preserve login-before-store-loading and the original default data directory. */
public class StoreApplication {
    public static void main(String[] args) {
        StorePaths paths = StorePaths.workingDirectory();
        StoreKeeperManager staff = new StoreKeeperManager(paths.storekeepers().toString());
        LoginPanel login = new LoginPanel() {
            @Override protected boolean login(String username, String password) {
                return staff.login(username, password);
            }
        };
        login.setVisible(true);
        if (!login.isSuccess()) System.exit(0);
        StoreContext context = StoreContext.load(paths);
        MainFrame frame = StoreWindowFactory.create(context);
        frame.setVisible(true);
    }
}
