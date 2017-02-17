package org.im.dc.server.js;

import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.junit.Test;

public class JsTest {
    @Test
    public void test() throws Exception {
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/test-article.xml"))),
                ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("src-test/org/im/dc/server/js/test.js", context);
    }
}
