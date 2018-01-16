package org.im.dc.server.js;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.service.ValidationHelper;
import org.junit.Test;

public class JsValidation {

    @Test
    public void tests() throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load(System.getProperty("CONFIG_DIR"));

        Db.init();

        process(new String[] { "камфара+", "ка+мфара", "камфо+ра" }, null, "v2.xml",
                "Колькасць паметаў загалоўных слоў несупадае з колькасцю слоў");
        process(new String[] { "камфара+" }, null, "v2.xml", "Род непазначаны");
        process(new String[] { "f+" }, null, "v2.xml", "Няправільныя сімвалы ў загалоўным слове: f+");
        process(new String[] { "камфара+", "ка+мфара", "камфо+ра" }, null, "v-kamfara.xml", null);
    }

    private void process(String[] words, String articleType, String articleFile, String expectedError)
            throws Exception {
        byte[] xml = Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/" + articleFile));

        Validator validator = Config.schemas.get(articleType).newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(xml)));

        SimpleScriptContext context = new SimpleScriptContext();
        ValidationHelper helper = new ValidationHelper(-1);
        context.setAttribute("helper", helper, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", words, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(xml), ScriptContext.ENGINE_SCOPE);
        try {
            JsProcessing.exec("config/validation.js", context);
            if (expectedError != null) {
                fail("Выканалася без памылкі");
            }
        } catch (ScriptException ex) {
            assertEquals(expectedError, ex.getCause().getMessage());
        }
    }

    public static void main(String[] a) throws Exception {
        System.setProperty("log4j.configurationFile", new File("config/log4j.xml").getAbsolutePath());
        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();

        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("helper", new ValidationHelper(-1), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", new String[] { "хадзіць" }, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/test-article.xml"))),
                ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/validation.js", context);
    }
}
