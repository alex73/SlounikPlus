package org.im.dc.client.ui.struct.editors;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.im.dc.client.WS;
import org.im.dc.client.ui.ArticleEditController;
import org.im.dc.client.ui.MainController;
import org.im.dc.client.ui.struct.AnnotationInfo;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.client.ui.struct.IXSContainer;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.ArticlesFilter;

@SuppressWarnings("serial")
public class XSEditArticlesList extends XSNamedControl<JPanel> implements IXSEdit {
    private String articleTypeId;
    private JFilterComboBox combo;

    public XSEditArticlesList(ArticleUIContext context, IXSContainer parentContainer, AnnotationInfo ann) {
        super(context, parentContainer, ann);
    }

    @Override
    protected void initEditor() {
        articleTypeId = ann.editDetails;
        SwingUtilities.invokeLater(() -> context.editController.requestRetrieveHeaders(articleTypeId, null));

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
        combo.setSelectedItem("");
        combo.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                context.fireChanged();
            }
        });
        combo.setEditable(context.getWritable(parentContainer, ann));

        ImageIcon addIcon = new ImageIcon(XSEditArticlesList.class.getResource("add.png"));
        ImageIcon editIcon = new ImageIcon(XSEditArticlesList.class.getResource("edit.png"));
        JButton add = new JButton(addIcon);
        add.setToolTipText("Дадаць у спіс");
        JButton edit = new JButton(editIcon);
        edit.setToolTipText("Выправіць дэталі абранага");

        add.addActionListener(l -> openEdit(null));
        edit.addActionListener(l -> openEdit(getData()));

        editor = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 3, 0, 3);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        editor.add(combo, gbc);

        gbc.weightx = 0;
        gbc.gridx = 1;
        editor.add(add, gbc);
        gbc.gridx = 2;
        editor.add(edit, gbc);
    }

    private void afterUpdated(ArticleFull article) {
        context.editController.requestForceRetrieveHeaders(articleTypeId, new Runnable() {
            @Override
            public void run() {
                setData(article.header);
            }
        });
    }

    @Override
    public void setData(String data) {
        if (!combo.isEditable()) {
            combo.setModel(new DefaultComboBoxModel<String>(new String[] { data }));
        }
        combo.setSelectedItem(data);
    }

    @Override
    public String getData() {
        if (combo.array.isEmpty()) {
            List<String> headers = context.editController.getHeaders(ann.editDetails);
            if (headers != null && !headers.isEmpty()) {
                combo.array = headers;
            }
        }
        return (String) combo.getSelectedItem();
    }

    private void openEdit(String header) {
        if (header == null) {
            new ArticleEditController(MainController.initialData.getTypeInfo(articleTypeId)) {
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
                    found = WS.getArticleService().listArticles(WS.header, articleTypeId, filter);
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
                        new ArticleEditController(MainController.initialData.getTypeInfo(articleTypeId),
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
