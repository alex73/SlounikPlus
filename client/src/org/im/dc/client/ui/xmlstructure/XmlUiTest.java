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

import com.sun.org.apache.xerces.internal.impl.xs.XMLSchemaLoader;
import com.sun.org.apache.xerces.internal.xs.XSElementDeclaration;
import com.sun.org.apache.xerces.internal.xs.XSLoader;
import com.sun.org.apache.xerces.internal.xs.XSModel;

public class XmlUiTest {
    static final String ROOT = "root";

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        XSLoader schemaLoader = new XMLSchemaLoader();
        XSModel model = schemaLoader.loadURI("article.xsd");
        XSElementDeclaration root = model.getElementDeclaration(ROOT, null);

        XmlGroup rootGroup = new XmlGroup(root, new AnnotationInfo(root.getAnnotation()));
        f.getContentPane().add(new JScrollPane(rootGroup));

        f.setBounds(100, 100, 600, 700);
        f.setVisible(true);

//        XMLStreamReader rd = XMLInputFactory.newInstance()
//                .createXMLStreamReader(new FileInputStream("article.xml"));
//        int t = rd.nextTag();
//        System.out.println(rd.getLocalName());
        // rootGroup.insertData(rd);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                StringWriter w = new StringWriter();
                try {
                    XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(w);
                    rootGroup.extractData(ROOT, wr);
                    wr.flush();
                    System.out.println(w);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }
}
