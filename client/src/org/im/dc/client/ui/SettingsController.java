package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableColumnModel;

public class SettingsController extends BaseController<SettingsDialog> {
    private static int fontSize;

    public SettingsController() {
        super(new SettingsDialog(MainController.instance.window, true), MainController.instance.window);

        window.spFontSize.setValue(fontSize);

        // setup default button
        window.getRootPane().setDefaultButton(window.btnOk);
        window.btnOk.addActionListener((e) -> {
            setup();
            window.dispose();
        });

        setupCloseOnEscape();

        displayOnParent();
    }

    private void setup() {
        int size = ((SpinnerNumberModel) window.spFontSize.getModel()).getNumber().intValue();

        fontSize = size;
        savePreferences();
        setupFonts();
    }

    /**
     * Усталёўвае запісаныя памеры шрыфта на старце.
     */
    public static void initialize() {
        loadPreferences();
        setupFonts();
    }

    /**
     * Чытае запісаныя налады.
     */
    private static void loadPreferences() {
        fontSize = Preferences.userNodeForPackage(SettingsController.class).getInt("application.font", 12);
    }

    /**
     * Запісвае налады.
     */
    public static void savePreferences() {
        Preferences.userNodeForPackage(SettingsController.class).putInt("application.font", fontSize);
    }

    /**
     * Усталёўвае запісаныя памеры шрыфта.
     */
    public static void setupFonts() {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) value;
                Font font = orig.deriveFont((float) fontSize);
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }

    /**
     * Усталёўвае вышыню радкоў у табліцах.
     */
    public static void setupFontForWindow(Container c) {
        for (int i = 0; i < c.getComponentCount(); i++) {
            Component cc = c.getComponent(i);
            if (cc instanceof Container) {
                setupFontForWindow((Container) cc);
            }
            if (cc instanceof JTable) {
                ((JTable) cc).setRowHeight(fontSize + 2);
            }
        }
    }

    /**
     * Усталёўвае запісаныя месца вакна, пазыцыі слупкоў табліцы, JSplitPane.
     */
    public static void loadPlacesForWindow(Window root) {
        Preferences prefs = Preferences.userNodeForPackage(root.getClass());
        String prefix = root.getClass().getSimpleName() + '.';
        Rectangle rect = root.getBounds();
        rect.x = prefs.getInt(prefix + "x", rect.x);
        rect.y = prefs.getInt(prefix + "y", rect.y);
        rect.width = prefs.getInt(prefix + "w", rect.width);
        rect.height = prefs.getInt(prefix + "h", rect.height);
        root.setBounds(rect);
        loadPlacesForWindow(root, prefs, prefix);
    }

    private static void loadPlacesForWindow(Container c, Preferences prefs, String prefix) {
        for (int i = 0; i < c.getComponentCount(); i++) {
            Component cc = c.getComponent(i);
            if (cc instanceof Container) {
                loadPlacesForWindow((Container) cc, prefs, prefix);
            }
            if (cc instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane) cc;
                int location = prefs.getInt(prefix + "JSplitPane." + sp.getName(), sp.getDividerLocation());
                sp.setDividerLocation(location);
            }
            if (cc instanceof JTable) {
                JTable tab = (JTable) cc;
                TableColumnModel cm = tab.getColumnModel();
                for (int j = 0; j < cm.getColumnCount(); j++) {
                    int w = prefs.getInt(prefix + "JTable." + tab.getName() + '[' + j + ']',
                            cm.getColumn(j).getWidth());
                    cm.getColumn(j).setPreferredWidth(w);
                }
            }
        }
    }

    /**
     * Запісвае месца вакна, пазыцыі слупкоў табліцы, JSplitPane.
     */
    public static void savePlacesForWindow(Window root) {
        Preferences prefs = Preferences.userNodeForPackage(root.getClass());
        String prefix = root.getClass().getSimpleName() + '.';
        Rectangle rect = root.getBounds();
        prefs.putInt(prefix + "x", rect.x);
        prefs.putInt(prefix + "y", rect.y);
        prefs.putInt(prefix + "w", rect.width);
        prefs.putInt(prefix + "h", rect.height);
        savePlacesForWindow(root, prefs, prefix);
    }

    private static void savePlacesForWindow(Container c, Preferences prefs, String prefix) {
        for (int i = 0; i < c.getComponentCount(); i++) {
            Component cc = c.getComponent(i);
            if (cc instanceof Container) {
                savePlacesForWindow((Container) cc, prefs, prefix);
            }
            if (cc instanceof JSplitPane) {
                JSplitPane sp = (JSplitPane) cc;
                prefs.putInt(prefix + "JSplitPane." + sp.getName(), sp.getDividerLocation());
            }
            if (cc instanceof JTable) {
                JTable tab = (JTable) cc;
                TableColumnModel cm = tab.getColumnModel();
                for (int j = 0; j < cm.getColumnCount(); j++) {
                    prefs.putInt(prefix + "JTable." + tab.getName() + '[' + j + ']', cm.getColumn(j).getWidth());
                }
            }
        }
    }
}
