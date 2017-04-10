package org.im.dc.server.js;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.im.dc.service.impl.HtmlOut;

public class JsTest {
    public static void main(String[] a) throws Exception {
        HtmlOut out = new HtmlOut();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("words", new String[] { "хадзіць" }, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article",
                new JsDomWrapper(Files.readAllBytes(Paths.get("src-test/org/im/dc/server/js/test-article.xml"))),
                ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("src-test/org/im/dc/server/js/test.js", context);

        System.out.print(out);

        Files.write(new File("/tmp/a.html").toPath(), out.toString().getBytes());
    }
}
