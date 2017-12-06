package org.im.dc.client.ui.xmlstructure;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.im.dc.client.WS;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.IArticleUpdatedListener;
import org.im.dc.client.ui.MainController;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;

@SuppressWarnings("serial")
public class XmlEditArticlesList extends XmlEditBase<JPanel> implements IXmlSimpleElement {
    private JFilterComboBox combo;

    public XmlEditArticlesList(ArticleUIContext context, XmlGroup parentPanel, AnnotationInfo ann,
            boolean parentWritable) {
        super(context, parentPanel, ann, parentWritable);
    }

    @Override
    protected JPanel createField() {
        context.editController.requestRetrieveHeaders(ann.editDetails, null);

        combo = new JFilterComboBox(new ArrayList<>()) {
            @Override
            public void comboFilter(String enteredText) {
                List<String> headers = context.editController.getHeaders(ann.editDetails);
                if (headers != null && !headers.isEmpty()) {
                    array = headers;
                }
                super.comboFilter(enteredText);
            }
        };
        combo.setFont(context.getFont());
        combo.setSelectedItem("");
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                context.fireChanged();
            }
        });
        combo.setEditable(writable);

        ImageIcon addIcon = new ImageIcon(XmlEditArticlesList.class.getResource("add.png"));
        ImageIcon editIcon = new ImageIcon(XmlEditArticlesList.class.getResource("edit.png"));
        JButton add = new JButton(addIcon);
        JButton edit = new JButton(editIcon);

        add.addActionListener(l -> openEdit(null));
        edit.addActionListener(l -> openEdit(getData()));

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 3, 0, 3);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        panel.add(combo, gbc);

        gbc.weightx = 0;
        gbc.gridx = 1;
        panel.add(add, gbc);
        gbc.gridx = 2;
        panel.add(edit, gbc);

        return panel;
    }

    private void afterUpdated(ArticleFull article) {
        context.editController.requestForceRetrieveHeaders(ann.editDetails, new Runnable() {
            @Override
            public void run() {
                setData(article.header);
            }
        });
    }

    @Override
    public void setData(String data) {
        combo.setSelectedItem(data);
    }

    @Override
    public String getData() {
        return (String) combo.getSelectedItem();
    }

    private void openEdit(String header) {
        if (header == null) {
            new ArticleEditController(MainController.initialData.getTypeInfo(ann.editDetails)) {
                @Override
                protected void afterSave(ArticleFull article) {
                    super.afterSave(article);
                    afterUpdated(article);
                }
            };
        } else {
            context.editController.new LongProcess() {
                List<ArticleShort> found;

                @Override
                protected void exec() throws Exception {
                    ArticlesFilter filter = new ArticlesFilter();
                    filter.exactHeader = header;
                    found = WS.getArticleService().listArticles(WS.header, ann.editDetails, filter);
                }

                @Override
                protected void ok() {
                    switch (found.size()) {
                    case 0:
                        JOptionPane.showMessageDialog(context.editController.window,
                                "Не знойдзены артыкул з загалоўкам '" + header + "'", "Памылка",
                                JOptionPane.ERROR_MESSAGE);
                        break;
                    case 1:
                        new ArticleEditController(MainController.initialData.getTypeInfo(ann.editDetails),
                                found.get(0).id) {
                            @Override
                            protected void afterSave(ArticleFull article) {
                                super.afterSave(article);
                                afterUpdated(article);
                            }
                        };
                        break;
                    default:
                        JOptionPane.showMessageDialog(context.editController.window,
                                "Зашмат артыкулаў з загалоўкам '" + header + "'", "Памылка", JOptionPane.ERROR_MESSAGE);
                        break;
                    }
                }
            };
        }
    }
}
