package org.im.dc.server.js;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.impl.HtmlOut;
import org.im.dc.service.impl.ValidationHelper;
import org.junit.Test;

public class JsValidation {

    @Test
    public void tests() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
        Config.load();

        process(new String[] { "хадзіць", "другое" }, "v1.xml",
                "Больш за 1 загалоўнае слова пакуль не падтрымліваецца");
        process(new String[] { "хадзіць" }, "v2.xml", "Колькасць паметаў загалоўных слоў несупадае з колькасцю слоў");
    }

    private void process(String[] words, String articleFile, String expectedError) throws Exception {
        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", words, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile))),
                ScriptContext.ENGINE_SCOPE);
        try {
            JsProcessing.exec("config/validation.js", context);
            fail("Выканалася без памылкі");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith(expectedError + " in <eval>"));
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Db.init();
        Config.load();

        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("helper", new ValidationHelper(-1), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", new String[] { "хадзіць" }, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/test-article.xml"))),
                ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/validation.js", context);
    }
}
