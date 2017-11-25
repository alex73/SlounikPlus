package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockableState;
import com.vlsolutions.swing.docking.DockableState.Location;
import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.TabbedDockableContainer;

public class SettingsController extends BaseController<SettingsDialog> {
    private static String fontName;
    private static int fontSize;

    public static List<Dockable> articleDockables;

    public SettingsController() {
        super(new SettingsDialog(MainController.instance.window, true), MainController.instance.window);

        String[] fs = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] fonts = new String[fs.length + 1];
        System.arraycopy(fs, 0, fonts, 1, fs.length);
        fonts[0] = "";
        window.cbFontName.setModel(new DefaultComboBoxModel<>(fonts));
        if (fontName != null) {
            window.cbFontName.setSelectedItem(fontName);
        }
        window.spFontSize.setValue(fontSize);
        window.cbFontName.addItemListener(l -> updateTest());
        window.spFontSize.addChangeListener(c -> updateTest());

        // setup default button
        window.getRootPane().setDefaultButton(window.btnOk);
        window.btnOk.addActionListener((e) -> {
            setup();
            window.dispose();
        });
        window.btnCancel.addActionListener((e) -> {
            window.dispose();
        });

        setupCloseOnEscape();

        displayOnParent();
    }

    private void updateTest() {
        String fontName = (String) window.cbFontName.getSelectedItem();
        int fontSize = ((SpinnerNumberModel) window.spFontSize.getModel()).getNumber().intValue();
        Font font;
        if (fontName != null && !fontName.trim().isEmpty()) {
            font = new Font(fontName, Font.PLAIN, fontSize);
        } else {
            font = window.getFont().deriveFont((float) fontSize);
        }
        window.txtTest.setFont(font);
        window.validate();
    }

    private void setup() {
        fontName = (String) window.cbFontName.getSelectedItem();
        fontSize = ((SpinnerNumberModel) window.spFontSize.getModel()).getNumber().intValue();
        savePreferences();
        setupFonts();
        setupFontForWindow(MainController.instance.window);
        MainController.instance.window.validate();
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
        fontName = Preferences.userNodeForPackage(SettingsController.class).get("application.font.name", null);
        fontSize = Preferences.userNodeForPackage(SettingsController.class).getInt("application.font.size", 12);
    }

    /**
     * Запісвае налады.
     */
    public static void savePreferences() {
        Preferences.userNodeForPackage(SettingsController.class).put("application.font.name", fontName);
        Preferences.userNodeForPackage(SettingsController.class).putInt("application.font.size", fontSize);
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
                Font font;
                if (fontName != null && !fontName.trim().isEmpty()) {
                    font = new Font(fontName, Font.PLAIN, fontSize);
                } else {
                    FontUIResource orig = (FontUIResource) value;
                    font = orig.deriveFont((float) fontSize);
                }
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

    public static void resetPlaces(Class<? extends Window> rootClass) {
        Preferences prefs = Preferences.userNodeForPackage(rootClass);
        String prefix = rootClass.getSimpleName() + '.';
        prefs.remove(prefix + "x");
        prefs.remove(prefix + "y");
        prefs.remove(prefix + "w");
        prefs.remove(prefix + "h");
        prefs.remove(prefix + "desk");
    }

    /**
     * Усталёўвае запісаныя месца вакна, пазыцыі слупкоў табліцы, JSplitPane.
     */
    public static void loadPlacesForWindow(Window root, DockingDesktop desk) {
        Preferences prefs = Preferences.userNodeForPackage(root.getClass());
        String prefix = root.getClass().getSimpleName() + '.';
        Rectangle rect = root.getBounds();
        rect.x = prefs.getInt(prefix + "x", rect.x);
        rect.y = prefs.getInt(prefix + "y", rect.y);
        rect.width = prefs.getInt(prefix + "w", rect.width);
        rect.height = prefs.getInt(prefix + "h", rect.height);
        root.setBounds(rect);
        loadPlacesForWindow(root, prefs, prefix);
        loadDocking(root, desk);
    }

    private static void loadPlacesForWindow(Container c, Preferences prefs, String prefix) {
        for (int i = 0; i < c.getComponentCount(); i++) {
            Component cc = c.getComponent(i);
            if (cc instanceof Container) {
                loadPlacesForWindow((Container) cc, prefs, prefix);
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

    public static void loadDocking(Window root, DockingDesktop desk) {
        Preferences prefs = Preferences.userNodeForPackage(root.getClass());
        String prefix = root.getClass().getSimpleName() + '.';
        byte[] xml = prefs.getByteArray(prefix + "desk", null);
        try {
            if (xml != null) {
                desk.readXML(new ByteArrayInputStream(xml));
            } else {
                String xmlName = root.getClass().getSimpleName() + ".desk";
                try (InputStream in = root.getClass().getResourceAsStream(xmlName)) {
                    desk.readXML(in);
                }
                TabbedDockableContainer c = findTabbedDockableContainer(desk);
                if (c != null && articleDockables != null) {
                    for (Dockable d : articleDockables) {
                        desk.getContext().setDockableState(d, new DockableState(desk, d, Location.DOCKED));
                        d.getDockKey().setLocation(Location.DOCKED);
                        c.addDockable(d, c.getTabCount());
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public static void initializeDockingLayour(Window root, DockingDesktop desk) {
        try {
            String xmlName = root.getClass().getSimpleName() + ".desk";
            try (InputStream in = root.getClass().getResourceAsStream(xmlName)) {
                desk.readXML(in);
            }
            TabbedDockableContainer c = findTabbedDockableContainer(desk);
            if (c != null && articleDockables != null) {
                for (Dockable d : articleDockables) {
                    desk.getContext().setDockableState(d, new DockableState(desk, d, Location.DOCKED));
                    d.getDockKey().setLocation(Location.DOCKED);
                    c.addDockable(d, c.getTabCount());
                }
            }
        } catch (Exception ex) {
        }
    }

    public static TabbedDockableContainer findTabbedDockableContainer(Container parent) {
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            if (c instanceof TabbedDockableContainer) {
                return (TabbedDockableContainer) c;
            }
        }
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component c = parent.getComponent(i);
            if (c instanceof Container) {
                TabbedDockableContainer r = findTabbedDockableContainer((Container) c);
                if (r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    /**
     * Запісвае месца вакна, пазыцыі слупкоў табліцы, JSplitPane.
     */
    public static void savePlacesForWindow(Window root, DockingDesktop desk) {
        Preferences prefs = Preferences.userNodeForPackage(root.getClass());
        String prefix = root.getClass().getSimpleName() + '.';
        Rectangle rect = root.getBounds();
        prefs.putInt(prefix + "x", rect.x);
        prefs.putInt(prefix + "y", rect.y);
        prefs.putInt(prefix + "w", rect.width);
        prefs.putInt(prefix + "h", rect.height);
        savePlacesForWindow(root, prefs, prefix);

        if (desk != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                desk.writeXML(out);
                prefs.putByteArray(prefix + "desk", out.toByteArray());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private static void savePlacesForWindow(Container c, Preferences prefs, String prefix) {
        for (int i = 0; i < c.getComponentCount(); i++) {
            Component cc = c.getComponent(i);
            if (cc instanceof Container) {
                savePlacesForWindow((Container) cc, prefs, prefix);
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

    /**
     * This method replaces model and save/restore column width.
     */
    public static void replaceModel(JTable table, TableModel model) {
        TableColumnModel cm = table.getColumnModel();
        int[] widths = new int[cm.getColumnCount()];
        for (int j = 0; j < cm.getColumnCount(); j++) {
            widths[j] = cm.getColumn(j).getWidth();
        }

        table.setModel(model);

        cm = table.getColumnModel();
        for (int j = 0; j < Math.min(cm.getColumnCount(), widths.length); j++) {
            cm.getColumn(j).setPreferredWidth(widths[j]);
        }
    }
}
