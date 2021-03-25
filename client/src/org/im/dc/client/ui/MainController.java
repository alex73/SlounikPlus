package org.im.dc.client.ui;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.jar.Manifest;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.im.dc.client.SchemaLoader;
import org.im.dc.client.WS;
import org.im.dc.client.ui.reports.ReportFieldValuesController;
import org.im.dc.gen.config.CommonPermission;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.InitialData;
import org.im.dc.service.dto.Related;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * Controls main window.
 */
public class MainController extends BaseController<MainFrame> {
    static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("org/im/dc/client/ui/Bundle");

    public static Map<String, Comparator<String>> comparators = new TreeMap<>();
    public static InitialData initialData;
    public static MainController instance;

    private final String addr;
    private MainFrameIssuesModel issuesModel;
    private MainFrameNewsModel newsModel;

    public List<MainControllerArticleType> panelArticleTypes = new ArrayList<>();
    private List<IArticleUpdatedListener> articleUpdatedListeners = new ArrayList<>();
    private MainFramePanelIssues panelIssues = new MainFramePanelIssues();
    private MainFramePanelNews panelNews = new MainFramePanelNews();

    public MainController(String addr) {
        super(new MainFrame(), null);
        setTitle(null);
        this.addr = addr;
        instance = this;
    }

    void initDocking() {
        for (InitialData.TypeInfo ti : initialData.articleTypes) {
            panelArticleTypes.add(new MainControllerArticleType(ti));
        }
        Dockable issues = new Dockable() {
            DockKey key = new DockKey("issuesList", BUNDLE.getString("MainFrame.dock.issuesList"));

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelIssues;
            }
        };
        Dockable news = new Dockable() {
            DockKey key = new DockKey("newsList", BUNDLE.getString("MainFrame.dock.newsList"));

            @Override
            public DockKey getDockKey() {
                return key;
            }

            @Override
            public Component getComponent() {
                return panelNews;
            }
        };

        SettingsController.articleDockables = new ArrayList<>();
        panelArticleTypes.forEach(pc -> SettingsController.articleDockables.add(pc.dock));

        desk = new DockingDesktop();
        window.getContentPane().add(desk);
        panelArticleTypes.forEach(pc -> desk.registerDockable(pc.dock));
        desk.registerDockable(news);
        desk.registerDockable(issues);
        SettingsController.initializeDockingLayour(window, desk);
    }

    @Override
    protected boolean closing() {
        SettingsController.savePlacesForWindow(window, desk);
        System.exit(0);
        return true;
    }

    public void start() {
        window.setSize(400, 300);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        showProgress();
        askPassword();
    }

    public void startWithUser(String username, String pass) {
        window.setSize(400, 300);
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        login(username, pass);
    }

    /**
     * Show ask password dialog
     */
    private void askPassword() {
        // show ask password dialog
        PasswordDialog askPassword = new PasswordDialog(window, true);

        // setup default button
        askPassword.getRootPane().setDefaultButton(askPassword.btnOk);
        askPassword.btnOk.addActionListener((e) -> {
            askPassword.btnCancel = null;
            askPassword.dispose();
            login(askPassword.txtUser.getText().trim(), new String(askPassword.txtPass.getPassword()).trim());
        });

        // setup cancel button
        ActionListener cancelListener = (e) -> {
            askPassword.dispose();
        };
        askPassword.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (askPassword.btnCancel != null) {
                    window.dispose();
                }
            }
        });
        askPassword.btnCancel.addActionListener(cancelListener);
        askPassword.getRootPane().registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        // display
        askPassword.setLocationRelativeTo(window);
        askPassword.setVisible(true);
    }

    /**
     * Login to server and retrieve initial info.
     */
    void login(String username, String pass) {
        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                if (addr.startsWith("http://")|| addr.startsWith("https://")) {
                    WS.initWS(addr, username, pass);
                } else {
                    WS.initGit(addr, username);
                }
                initialData = WS.getToolsWebservice().getInitialData(WS.header);

                SchemaLoader.init(initialData);
            }

            @Override
            protected void ok() {
                setTitle(username);
                initDocking();
                init();
                initPlugins();
            }

            @Override
            protected void error() {
                System.exit(1);
            }
        };
    }

    private void init() {
        panelIssues.tableIssues.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameIssuesModel) panelIssues.tableIssues.getModel()).issues
                        .get(panelIssues.tableIssues.getSelectedRow());
                InitialData.TypeInfo ti = MainController.initialData.getTypeInfo(a.articleTypeId);
                new ArticleEditController(ti, a.articleId);
            }
        });
        panelNews.tableNews.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Related a = ((MainFrameNewsModel) panelNews.tableNews.getModel()).news
                        .get(panelNews.tableNews.getSelectedRow());
                InitialData.TypeInfo ti = MainController.initialData.getTypeInfo(a.articleTypeId);
                new ArticleEditController(ti, a.articleId);
            }
        });

        window.miSettings.addActionListener((e) -> new SettingsController());

        window.miResetDesk.addActionListener(e -> {
            SettingsController.resetPlaces(MainFrame.class);
            SettingsController.resetPlaces(ArticleEditDialog.class);
            SettingsController.resetPlaces(SettingsDialog.class);
            SettingsController.loadPlacesForWindow(window, desk);
            SettingsController.initializeDockingLayour(window, desk);
        });

        if (!initialData.currentUserPermissions.contains(CommonPermission.REPORTS.name())) {
            window.miReports.setVisible(false);
        }

        if (initialData.currentUserPermissions.contains(CommonPermission.FULL_VALIDATION.name())) {
            for (InitialData.TypeInfo ti : initialData.articleTypes) {
                JMenuItem it = new JMenuItem(ti.typeName);
                it.addActionListener((e) -> validateFull(ti.typeId));
                window.miValidateFull.add(it);
            }
        } else {
            window.miValidateFull.setVisible(false);
        }
        if (initialData.currentUserPermissions.contains(CommonPermission.FULL_VALUES.name())) {
            for (InitialData.TypeInfo ti : initialData.articleTypes) {
                JMenuItem it = new JMenuItem(ti.typeName);
                it.addActionListener((e) -> valuesFull(ti.typeId));
                window.miValuesFull.add(it);
            }
        } else {
            window.miValuesFull.setVisible(false);
        }
        if (initialData.currentUserPermissions.contains(CommonPermission.FULL_EXPORT.name())) {
            for (InitialData.TypeInfo ti : initialData.articleTypes) {
                JMenuItem it = new JMenuItem(ti.typeName);
                it.addActionListener((e) -> exportFull(ti.typeId));
                window.miExportFull.add(it);
            }
        } else {
            window.miExportFull.setVisible(false);
        }

        showIssiesAndNews();
    }

    public MainFrame getMainFrame() {
        return window;
    }

    private void initPlugins() {
        try {
            Enumeration<URL> mfs = MainController.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (mfs.hasMoreElements()) {
                URL url = mfs.nextElement();
                try (InputStream in = new BufferedInputStream(url.openStream())) {
                    Manifest m = new Manifest(in);
                    String initClass = m.getMainAttributes().getValue("SlounikPlus-init-client");
                    if (initClass != null) {
                        Class<?> c = Class.forName(initClass);
                        Method me = c.getMethod("clientStarted");
                        me.invoke(c);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Comparator<String> getComparatorForArticleType(String articleType) {
        Comparator<String> result = comparators.get(articleType);
        if (result == null) {
            throw new RuntimeException("Comparator not defined for " + articleType);
        }
        return result;
    }

    private void validateFull(String articleTypeId) {
        PreviewController previewer = new PreviewController(MainController.instance.window, false);
        previewer.window.setTitle("Праверка");
        previewer.setupExecutor(() -> {
            previewer.new LongProcess() {
                String text;

                @Override
                protected void exec() throws Exception {
                    OutputSummaryStorage result = WS.getToolsWebservice().previewValidateAll(WS.header, articleTypeId);
                    if (result.summaryErrors.isEmpty()) {
                        text = "<html><body>Праверка паспяхова скончаная</html></body>";
                    } else {
                        text = "<html><body>" + String.join("<br/>\n", result.summaryErrors) + "</html></body>";
                    }
                }

                @Override
                protected void ok() {
                    previewer.window.text.setText(text);
                    previewer.window.text.setCaretPosition(0);
                    previewer.window.text.getDocument().putProperty("ZOOM_FACTOR", new Double(2.5));
                }
            };
        });
        previewer.execute();
    }

    private void valuesFull(String articleTypeId) {
        new ReportFieldValuesController(articleTypeId);
    }

    private void exportFull(String articleTypeId) {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("HTML files", "html");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showSaveDialog(getMainFrame());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }

        new LongProcess() {
            boolean needHr;

            @Override
            protected void exec() throws Exception {
                OutputSummaryStorage storage = WS.getToolsWebservice().previewValidateAll(WS.header, articleTypeId);
                StringBuilder out = new StringBuilder(
                        "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n");
                needHr = false;
                for (String e : storage.summaryErrors) {
                    out.append("<b>АГУЛЬНАЯ ПАМЫЛКА: " + e + "</b><br/>\n");
                    needHr = true;
                }
                for (OutputSummaryStorage.ArticleError e : storage.errors) {
                    out.append("<b>ПАМЫЛКА у " + e.key + ": " + e.error + "</b><br/>\n");
                    needHr = true;
                }
                storage.outputs.forEach(ao -> {
                    if (needHr) {
                        // out.append("<hr/>\n");
                    } else {
                        needHr = true;
                    }
                    out.append(ao.html);
                    // out.append("<br/>\n");
                });
                out.append("</body></html>\n");

                File o = chooser.getSelectedFile();
                if (!o.getName().endsWith(".html")) {
                    o = new File(o.getAbsolutePath() + ".html");
                }
                Files.write(o.toPath(), out.toString().getBytes(StandardCharsets.UTF_8));
            }

            @Override
            protected void ok() {
                JOptionPane.showMessageDialog(getMainFrame(), "Finished", "Export", JOptionPane.INFORMATION_MESSAGE);
            }
        };
    }

    public void addArticleUpdatedListener(IArticleUpdatedListener listener) {
        synchronized (articleUpdatedListeners) {
            articleUpdatedListeners.add(listener);
        }
    }

    public void removeArticleUpdatedListener(IArticleUpdatedListener listener) {
        synchronized (articleUpdatedListeners) {
            articleUpdatedListeners.remove(listener);
        }
    }

    public void fireArticleUpdated(ArticleFull article) {
        synchronized (articleUpdatedListeners) {
            for(IArticleUpdatedListener listener:articleUpdatedListeners) {
                listener.onArticleUpdated(article);
            }
        }
    }

    private void setTitle(String user) {
        String t = BUNDLE.getString("MainFrame.title");
        if (user != null) {
            t += ": " + user;
        }
        String buildtime = null;
        try {
            Enumeration<URL> mfs = MainController.class.getClassLoader().getResources("META-INF/MANIFEST.MF");
            while (mfs.hasMoreElements()) {
                URL url = mfs.nextElement();
                try (InputStream in = new BufferedInputStream(url.openStream())) {
                    Manifest m = new Manifest(in);
                    buildtime = m.getMainAttributes().getValue("SlounikPlus-buildtime");
                    if (buildtime != null) {
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (buildtime != null) {
            t += " [" + buildtime + "]";
        } else {
            t += " [dev]";
        }
        window.setTitle(t);
    }

    /**
     * Shows issues and news tables every 2 minutes.
     */
    public void showIssiesAndNews() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                issuesModel = new MainFrameIssuesModel(WS.getToolsWebservice().listIssues(WS.header));
                newsModel = new MainFrameNewsModel(WS.getToolsWebservice().listNews(WS.header));
                return null;
            }

            protected void done() {
                try {
                    get();
                    SettingsController.replaceModel(panelIssues.tableIssues, issuesModel);
                    SettingsController.replaceModel(panelNews.tableNews, newsModel);
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                Timer timer = new Timer(2 * 60 * 1000, a -> {
                    showIssiesAndNews();
                });
                timer.setRepeats(false);
                timer.start();
            }
        }.execute();
    }
}
