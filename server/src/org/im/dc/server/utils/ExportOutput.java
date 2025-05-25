package org.im.dc.server.utils;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.im.dc.gen.config.Type;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.startup.Server;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.impl.js.JsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * It exports all records to HTML.
 */
public class ExportOutput {
    private static final Logger LOG = LoggerFactory.getLogger(ExportOutput.class);

    public static String HTML_PREFIX = "<!DOCTYPE html>\n<html><head><meta charset=\"UTF-8\"></head><body>\n";
    public static String HTML_SUFFIX = "\n</body></html>\n";

    static boolean needHr;

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("ExportOutput <dir|zip>");
            System.exit(1);
        }

        Config.load(System.getProperty("CONFIG_DIR"));
        Db.init(System.getProperty("CONFIG_DIR"));
        Server.initPlugins();

        for (Type type : Config.getConfig().getTypes().getType()) {
            String articleType = type.getId();
            List<RecArticle> todo = Db.execAndReturn((api) -> {
                List<RecArticle> r = new ArrayList<>();
                List<RecArticle> articles = api.getArticleMapper().getAllArticles(articleType);
                for (RecArticle a : articles) {
                    if (a == null) {
                        // no such article
                        continue;
                    }
                    if (!articleType.equals(a.getArticleType())) {
                        LOG.warn("<< preparePreviews: wrong type/id requested");
                        throw new Exception("Запыт няправільнага ID для вызначанага тыпу");
                    }
                    r.add(a);
                }
                return r;
            });
            LOG.info("   validateAll - preview for each article(type:" + articleType + ") started");
            OutputSummaryStorage storage = JsHelper.previewSomeArticles(articleType, todo);
            LOG.info("   validateAll - summary validation start");
            JsHelper.validateSummary(articleType, storage);

            StringBuilder out = new StringBuilder(HTML_PREFIX);
            needHr = false;

            for (String e : storage.summaryErrors) {
                out.append("<b>АГУЛЬНАЯ ПАМЫЛКА: " + e + "</b><br/>\n");
                needHr = true;
            }
            for (OutputSummaryStorage.ArticleError e : storage.errors) {
                out.append("<b>ПАМЫЛКА: " + e.error + "</b><br/>\n");
                needHr = true;
            }
            storage.outputs.forEach(ao -> {
                if (needHr) {
                    //out.append("<hr/>\n");
                } else {
                    needHr = true;
                }
                out.append(ao.html);
                //out.append("<br/>\n");
            });

            out.append(HTML_SUFFIX);
            Files.write(Paths.get(args[0] + articleType + ".html"), out.toString().getBytes(StandardCharsets.UTF_8));
        }
    }
}
