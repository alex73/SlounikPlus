package org.im.dc.client.ui.struct;

import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.im.dc.client.SchemaLoader;
import org.im.dc.service.dto.InitialData;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

/**
 * It tests that load/save XML into/from UI produces the same XML. Required for
 * validate UI parsing.
 */
public class XMLZipTest {
    static DocumentBuilder db;
    static Transformer transformer;

    public static void main(String[] args) throws Exception {
        List<InitialData.TypeInfo> types = new ArrayList<>();
        InitialData.TypeInfo ti = new InitialData.TypeInfo();
        ti.typeId = "root";
        types.add(ti);
    //    SchemaLoader.init(types);
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
                return new Font("System", 0, 10);
            }
        };

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringElementContentWhitespace(true);
        db = dbf.newDocumentBuilder();
        transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        try (ZipInputStream zip = new ZipInputStream(
                new FileInputStream("/data/Repo-MyShare-Temp/nasovic-20171208.zip"))) {
            ZipEntry en;
            while ((en = zip.getNextEntry()) != null) {
                if (!en.getName().endsWith(".xml")) {
                    throw new Exception(en.getName());
                }
                byte[] xml = IOUtils.toByteArray(zip);

                System.out.println(en.getName());
                IXSContainer c = SchemaLoader.createUI(context);
                XMLStreamReader rd = XMLInputFactory.newInstance().createXMLStreamReader(new ByteArrayInputStream(xml));
                rd.nextTag();
                c.insertData(rd);

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                XMLStreamWriter wr = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
                wr.writeStartDocument("UTF-8", "1.0");
                c.extractData(wr);
                wr.flush();

                compare(xml, out.toByteArray());
            }
        }
    }

    static void compare(byte[] xml1, byte[] xml2) throws Exception {
        StreamResult result = new StreamResult(new StringWriter());
        StreamSource source = new StreamSource(new ByteArrayInputStream(xml2));
        transformer.transform(source, result);
        String xml2s = result.getWriter().toString();

        result = new StreamResult(new StringWriter());
        source = new StreamSource(new ByteArrayInputStream(xml1));
        transformer.transform(source, result);
        String xml1s = result.getWriter().toString();

        Diff d = DiffBuilder.compare(Input.fromString(xml1s)).withTest(Input.fromString(xml2s)).build();
        if (!d.hasDifferences()) {
            return;
        }

        // initialize StreamResult with File object to save to file

        System.out.println(xml1s);
        System.out.println(xml2s);

        FileUtils.writeStringToFile(new File("/tmp/1"), xml1s);
        FileUtils.writeStringToFile(new File("/tmp/2"), xml2s);
        throw new RuntimeException(d.toString());
    }
}
