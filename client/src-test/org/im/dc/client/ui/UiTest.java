package org.im.dc.client.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.im.dc.client.SchemaLoader;
import org.im.dc.client.ui.struct.ArticleUIContext;
import org.im.dc.service.dto.ArticleFull;
import org.im.dc.service.dto.ArticleFullInfo;
import org.im.dc.service.dto.InitialData;

public class UiTest {

    public static void main(String[] args) throws Exception {
        // JFrame f = new JFrame();
        // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // f.setBounds(100, 100, 600, 700);
        // f.setVisible(true);

        InitialData id = new InitialData();
        id.xsds = new TreeMap<>();
        InitialData.TypeInfo ti = new InitialData.TypeInfo();
        for (File fi : new File("/data/gits/My/Projects/SlounikPlus/tlum/config/")
                .listFiles(fi -> fi.getName().endsWith(".xsd"))) {
            id.xsds.put(fi.getName(), FileUtils.readFileToByteArray(fi));
        }
        ti.typeId = "article";
        id.articleTypes.add(ti);
        SchemaLoader.init(id);

        new MainController("").window.setVisible(true);
        ArticleUIContext context = new ArticleUIContext("article") {
            @Override
            public void resetChanged() {
            }

            @Override
            public void fireChanged() {
            }
        };
        MainController.initialData = new InitialData();
        if (args.length > 0) {
            context.editController = new ArticleEditController(ti, loadArticle(ti, args[0]));
        } else {
            context.editController = new ArticleEditController(ti);
        }
        context.articleState = "0";
        context.userRole = "укладальнік";

        // IXSContainer c = SchemaLoader.createUI(context);
        // System.out.println(c.dump(""));
        // f.getContentPane().add(new JScrollPane(c.getUIComponent()));
        // f.validate();

        // XMLStreamReader rd = XMLInputFactory.newInstance().createXMLStreamReader(
        // new FileInputStream("/data/gits/My/Projects/SlounikPlus/ital/config/root-test.xml"));
        // rd.nextTag();

        context.editController.window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StringWriter w = new StringWriter();
                try {
                    XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
                    context.editController.editorUI.extractData(wr);
                    wr.flush();
                    System.out.println(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                System.exit(1);
            }
        });
    }

    private static ArticleFullInfo loadArticle(InitialData.TypeInfo typeInfo, String file) throws Exception {
        ArticleFullInfo article = new ArticleFullInfo();
        article.youCanEdit = true;
        article.article = new ArticleFull();
        article.article.type = typeInfo.typeId;
        article.article.state = typeInfo.newArticleState;
        article.article.assignedUsers = MainController.initialData.newArticleUsers;
        article.article.xml = Files.readAllBytes(Paths.get(file));

        return article;
    }
}
