package org.im.dc.server.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.server.startup.Server;
import org.im.dc.service.ValidationSummaryStorage;
import org.im.dc.service.impl.ArticleWebserviceImpl;
import org.im.dc.service.js.HtmlOut;
import org.w3c.dom.Document;

/**
 * It exports all records to HTML.
 */
public class ExportOutput {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportOutput <dir|zip>");
            System.exit(1);
        }

        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init();
        Server.initPlugins();

        List<Integer> ids = Db.execAndReturn((api) -> {
            return api.getArticleMapper().selectAllIds(null);
        });
        Collections.sort(ids);

        try (BufferedWriter wr = Files.newBufferedWriter(Paths.get(args[0]))) {
            ValidationSummaryStorage storage = new ValidationSummaryStorage();
            for (int id : ids) {
                Db.exec((api) -> {
                    RecArticle a = api.getArticleMapper().selectArticle(id);

                    String err = ArticleWebserviceImpl.validateArticle(a, storage);
                    String text;
                    if (err != null) {
                        text = "ERROR: " + err;
                    } else {
                        try {
                            HtmlOut out = new HtmlOut();
                            SimpleScriptContext context = new SimpleScriptContext();
                            context.setAttribute("out", out, ScriptContext.ENGINE_SCOPE);
                            context.setAttribute("header", a.getHeader(), ScriptContext.ENGINE_SCOPE);
                            Document doc = JsDomWrapper.parseDoc(a.getXml());
                            context.setAttribute("articleDoc", doc, ScriptContext.ENGINE_SCOPE);
                            context.setAttribute("article", new JsDomWrapper(doc.getDocumentElement()),
                                    ScriptContext.ENGINE_SCOPE);
                            context.setAttribute("mode", "output", ScriptContext.ENGINE_SCOPE);
                            context.setAttribute("summaryStorage", storage, ScriptContext.ENGINE_SCOPE);
                            JsProcessing.exec(
                                    new File(Config.getConfigDir(), a.getArticleType() + ".js").getAbsolutePath(),
                                    context);
                            out.normalize();
                            text = out.toString();
                        } catch (Exception ex) {
                            text = "ERROR: " + ex.getMessage();
                        }
                    }

                    wr.write("## " + id + "\n");
                    wr.write(text + "\n");
                });
            }
        }
    }
}
