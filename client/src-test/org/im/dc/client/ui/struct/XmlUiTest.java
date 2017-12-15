package org.im.dc.client.ui.struct;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.io.FileUtils;
import org.im.dc.client.SchemaLoader;
import org.im.dc.service.dto.InitialData;

public class XmlUiTest {

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        InitialData id = new InitialData();
        id.xsds = new TreeMap<>();
        InitialData.TypeInfo ti = new InitialData.TypeInfo();
        for (File fi : new File("/data/gits/My/Projects/SlounikPlus/tlum/config/")
                .listFiles(fi -> fi.getName().endsWith(".xsd"))) {
            id.xsds.put(fi.getName(), FileUtils.readFileToByteArray(fi));
        }
        // ti.articleSchema =
        // Files.readAllBytes(Paths.get("/data/gits/My/Projects/SlounikPlus/tlum/config/article.xsd"));
        // ti.typeId="article";
        ti.typeId = "article";
        id.articleTypes.add(ti);
        SchemaLoader.init(id);

        // XSElementDeclaration root = model.getElementDeclaration("infarmant",
        // null);

        ArticleUIContext context = new ArticleUIContext() {
            @Override
            public String getArticleTypeId() {
                return ti.typeId;
            }

            @Override
            public void resetChanged() {
            }

            @Override
            public void fireChanged() {
            }

            @Override
            public Font getFont() {
                return f.getFont();
            }
        };
        context.articleState = "0";
        context.userRole = "укладальнік";
        f.setBounds(100, 100, 600, 700);
        f.setVisible(true);

        IXSContainer c = SchemaLoader.createUI(context);
        System.out.println(c.dump(""));
        // XmlGroup rootGroup = new XmlGroup(context, null, root, new
        // AnnotationInfo(root.getAnnotation(), root.getName()),
        // true);
        f.getContentPane().add(new JScrollPane(c.getUIComponent()));
f.validate();
        
        XMLStreamReader rd = XMLInputFactory.newInstance().createXMLStreamReader(
                new FileInputStream("/data/gits/My/Projects/SlounikPlus/ital/config/root-test.xml"));
         rd.nextTag();
        System.out.println(rd.getLocalName());
       // c.insertData(rd);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StringWriter w = new StringWriter();
                try {
                    XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
                    c.extractData(wr);
                    wr.flush();
                    System.out.println(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
