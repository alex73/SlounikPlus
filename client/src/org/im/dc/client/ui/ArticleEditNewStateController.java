package org.im.dc.client.ui;

import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.ArticleShort;
import org.im.dc.service.dto.InitialData;

public class ArticleEditNewStateController extends BaseController<ArticleEditNewStateDialog> {
    private final ArticleEditController parentEdit;
    private InitialData.TypeInfo typeInfo;
    private List<ArticleShort> articles;

    public ArticleEditNewStateController(ArticleEditController parent) {
        super(new ArticleEditNewStateDialog(MainController.instance.window, true), parent.window);
        this.parentEdit = parent;
        this.typeInfo = null;
        this.articles = null;

        for (String state : parent.article.youCanChangeStateTo) {
            JRadioButton rb = new JRadioButton(state);
            window.statesGroup.add(rb);
            window.panelStates.add(rb);
        }

        window.btnChange.addActionListener(changeOne);
        window.btnCancel.addActionListener((e) -> window.dispose());

        setupCloseOnEscape();
        window.pack();

        displayOnParent();
    }

    public ArticleEditNewStateController(MainController parent, InitialData.TypeInfo typeInfo, List<ArticleShort> articles) {
        super(new ArticleEditNewStateDialog(MainController.instance.window, true), parent.window);
        this.parentEdit = null;
        this.typeInfo = typeInfo;
        this.articles = articles;

        for (String state : MainController.initialData.states) {
            JRadioButton rb = new JRadioButton(state);
            window.statesGroup.add(rb);
            window.panelStates.add(rb);
        }

        window.btnChange.addActionListener(changeMany);
        window.btnCancel.addActionListener((e) -> window.dispose());

        setupCloseOnEscape();
        window.pack();

        displayOnParent();
    }

    private String getSelectedState() {
        for (int i = 0; i < window.panelStates.getComponentCount(); i++) {
            JRadioButton rb = (JRadioButton) window.panelStates.getComponent(i);
            if (rb.isSelected()) {
                return rb.getText();
            }
        }
        return null;
    }

    ActionListener changeOne = (e) -> {
        String newState = getSelectedState();
        if (newState == null) {
            JOptionPane.showMessageDialog(window, "Новы стан не абраны", "Памылка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                parentEdit.article = WS.getArticleService().changeState(WS.header, parentEdit.article.article.type, parentEdit.article.article.id, newState,
                        parentEdit.article.article.lastUpdated);
                MainController.instance.fireArticleUpdated(parentEdit.article.article);
            }

            @Override
            protected void ok() {
                window.dispose();
                parentEdit.show();
            }
        };
    };

    ActionListener changeMany = (e) -> {
        String newState = getSelectedState();
        if (newState == null) {
            JOptionPane.showMessageDialog(window, "Новы стан не абраны", "Памылка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        for (ArticleShort article : articles) {
            String[] newStates = typeInfo.currentUserStateChanges.computeIfAbsent(article.state, a -> new String[0]);
            boolean allowed = Arrays.asList(newStates).contains(newState);
            if (!allowed) {
                JOptionPane.showMessageDialog(window, "Немагчыма змяніць стан артыкула " + article.id + " з " + article.state + " на " + newState, "Памылка",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        new LongProcess() {
            @Override
            protected void exec() throws Exception {
                for (ArticleShort article : articles) {
                    ArticleFullInfo a = WS.getArticleService().getArticleFullInfo(WS.header, article.type, article.id);
                    WS.getArticleService().changeState(WS.header, a.article.type, a.article.id, newState, a.article.lastUpdated);
                    MainController.instance.fireArticleUpdated(a.article);
                }
            }

            @Override
            protected void ok() {
                window.dispose();
            }
        };
    };
}
