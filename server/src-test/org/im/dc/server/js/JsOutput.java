package org.im.dc.server.js;

import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.js.HtmlOut;
import org.junit.Before;
import org.junit.Test;

public class JsOutput {
    @Before
    public void prepare() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load(System.getProperty("CONFIG_DIR"));
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

    private void process(String[] words, String articleFile) throws Exception {
        byte[] xml = Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile + ".xml"));

        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", words, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(xml), ScriptContext.ENGINE_SCOPE);

        JsProcessing.exec("config/output.js", context);
        System.out.print(out.toString());
        Files.write(new File("/tmp/" + articleFile + "-out.html").toPath(), out.toString().getBytes("UTF-8"));

        byte[] html = Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile + ".html"));
        if (!Arrays.equals(html, out.toString().getBytes("UTF-8"))) {
            fail("Wrong html");
        }
    }

    public static void main(String[] a) throws Exception {
        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", new String[] { "камфара+", "ка+мфара", "камфо+ра" }, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/v-kamfara.xml"))),
                ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/output.js", context);

        System.out.print(out);

        Files.write(new File("/tmp/a.html").toPath(), out.toString().getBytes());
    }
}
