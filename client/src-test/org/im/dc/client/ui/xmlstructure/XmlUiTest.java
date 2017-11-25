package org.im.dc.client.ui.xmlstructure;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;

public class XmlUiTest {

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XSLoader schemaLoader = new XMLSchemaLoader();
        XSModel model = schemaLoader.loadURI("/data/gits/My/Projects/SlounikPlus/archiu/config/infarmant.xsd");

        XSElementDeclaration root = model.getElementDeclaration("infarmant", null);

        ArticleUIContext context = new ArticleUIContext();
        context.articleState = "0";
        context.userRole = "укладальнік";
        XmlGroup rootGroup = new XmlGroup(context, null, root, new AnnotationInfo(root.getAnnotation(), root.getName()),
                true);
        f.getContentPane().add(new JScrollPane(rootGroup));

        f.setBounds(100, 100, 600, 700);
        f.setVisible(true);

        XMLStreamReader rd = XMLInputFactory.newInstance().createXMLStreamReader(
                new FileInputStream("/data/gits/My/Projects/SlounikPlus/archiu/config/infarmant-test.xml"));
        int t = rd.nextTag();
        // rootGroup.insertData(rd);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StringWriter w = new StringWriter();
                try {
                    XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
                    rootGroup.extractData("infarmant", wr);
                    wr.flush();
                    System.out.println(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
