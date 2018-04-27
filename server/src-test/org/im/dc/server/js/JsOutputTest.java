package org.im.dc.server.js;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.im.dc.client.ui.tlum.OutputInfo;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.js.HtmlOut;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class JsOutputTest {
    DocumentBuilderFactory DOC_FACTORY = DocumentBuilderFactory.newInstance();

    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load("config/");
        Db.init();

        // process(new String[] { "камфара+", "ка+мфара", "камфо+ра" }, "v-kamfara.xml");
        // process(new String[] { "гамё+лак", "гамё+лка" }, "v-hamiolak.xml");
        // process(new String[] { "кайданы+", "кайда+ны" }, "v-kajdany.xml");
        // process(new String[] { "до+гляд", "дагля+д" }, "v-dohlad.xml");
        
      
        // process(new String[] { "ара+бы", "ара+б", "ара+бка" }, "v-araby.xml");
        // process(new String[] { "ма+нсі" }, "v-mansi.xml");
    }
    
    @Test
    public void testBlizniaty() throws Exception {
        process(new String[] { "блізня+ты", "блізня+", "блізнё+" }, "v-blizniaty");
    }
    
    @Test
    public void testPalaki() throws Exception {
        process(new String[] { "палякі", "паляк", "полька", "палячка" }, "v-palaki");
    }

    @Test
    public void testPalakiFull() throws Exception {
        outFull("src-test/test-palaki.xml");
    }

    void outFull(String file) throws Exception {
        DocumentBuilder builder = DOC_FACTORY.newDocumentBuilder();
        Document doc = builder.parse(new File(file));
        
        
    }

    private void process(String[] words, String articleFile) throws Exception {
        byte[] xml = Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile + ".xml"));

        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", words, ScriptContext.ENGINE_SCOPE);
        Document doc = JsDomWrapper.parseDoc(xml);
        context.setAttribute("info", new OutputInfo(doc), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("articleDoc", doc, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(doc.getDocumentElement()), ScriptContext.ENGINE_SCOPE);

        JsProcessing.exec("config/output.js", context);
        out.normalize();
        System.out.print(out.toString());
        Files.write(new File("/tmp/" + articleFile + "-out.html").toPath(), out.toString().getBytes("UTF-8"));

        byte[] html = Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile + ".html"));
        if (!Arrays.equals(html, out.toString().getBytes("UTF-8"))) {
            fail("Wrong html");
        }
    }
}
