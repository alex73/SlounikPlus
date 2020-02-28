package org.im.dc.client.ui.templates;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.im.dc.client.WS;
import org.im.dc.client.ui.BaseController;
import org.im.dc.client.ui.MainController;
import org.im.dc.service.dto.ArticleFull;
import org.w3c.dom.Document;

public abstract class AddWordsController extends BaseController<AddWordsDialog> {
    protected Transformer transformer;
    protected DocumentBuilder builder;

    public AddWordsController() {
        super(new AddWordsDialog(MainController.instance.window, true), MainController.instance.window);
        setupCloseOnEscape();

        try {
            transformer = TransformerFactory.newInstance().newTransformer();
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        window.btnOk.addActionListener(ok);
        window.btnCancel.addActionListener((e) -> window.dispose());

        window.cbInitialState.setModel(new DefaultComboBoxModel<>(new Vector<>(MainController.initialData.states)));
        for (Map.Entry<String, String[]> en : MainController.initialData.allUsers.entrySet()) {
            JCheckBox cb = new JCheckBox(en.getKey() + " (" + String.join(",", en.getValue()) + ')');
            cb.setName(en.getKey());
            window.panelUsers.add(cb);
        }

        displayOnParent();
    }

    abstract protected String getArticleType();

    abstract protected String createHeader(String row);

    abstract protected Document createDoc(String row);

    protected byte[] createXml(String row) throws Exception {
        Document doc = createDoc(row);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(out));
        return out.toByteArray();
    }

    protected ArticleFull[] createArticles(String text, String initialState, String[] users) throws Exception {
        List<ArticleFull> r = new ArrayList<>();
        for (String w : text.split("\n")) {
            w = w.trim();
            if (w.isEmpty()) {
                continue;
            }
            ArticleFull a = new ArticleFull();
            a.assignedUsers = users;
            a.type = getArticleType();
            a.xml = createXml(w);
            a.header = createHeader(w);
            a.state = initialState;
            r.add(a);
        }
        return r.toArray(new ArticleFull[r.size()]);
    }

    private ActionListener ok = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            List<String> users = new ArrayList<>();
            for (int i = 0; i < window.panelUsers.getComponentCount(); i++) {
                JCheckBox cb = (JCheckBox) window.panelUsers.getComponent(i);
                if (cb.isSelected()) {
                    users.add(cb.getName());
                }
            }
            String w = window.words.getText();
            new LongProcess() {
                @Override
                protected void exec() throws Exception {
                    WS.getToolsWebservice().addArticles(WS.header, getArticleType(),
                            createArticles(w, window.cbInitialState.getSelectedItem().toString(),
                                    users.toArray(new String[users.size()])));
                }

                @Override
                protected void ok() {
                    JOptionPane.showMessageDialog(window, "Словы паспяхова захаваныя", "Новыя словы",
                            JOptionPane.INFORMATION_MESSAGE);
                    window.dispose();
                }
            };
        }
    };
}
