package org.im.dc.service.impl;

import java.io.File;
import java.text.Collator;
import java.util.List;
import java.util.Locale;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.ValidationHelper;
import org.w3c.dom.Document;

public class JsHelper {
    public static OutputSummaryStorage previewSomeArticles(String articleType, List<RecArticle> articles)
            throws Exception {
        OutputSummaryStorage storage = new OutputSummaryStorage();
        Validator validator = Config.schemas.get(articleType).newValidator();
        for (RecArticle a : articles) {
            ValidationHelper helper = new ValidationHelper(a.getArticleId(), validator, a.getXml(), storage);
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("helper", helper, ScriptContext.ENGINE_SCOPE);
            Document doc = JsDomWrapper.parseDoc(a.getXml());
            context.setAttribute("articleDoc", doc, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("article", new JsDomWrapper(doc.getDocumentElement()), ScriptContext.ENGINE_SCOPE);
            JsProcessing.exec(new File(Config.getConfigDir(), articleType + ".js").getAbsolutePath(), context);
            storage.textForSearch.put(a.getArticleId(), new WordSplitter().parse(a.getXml()));
        }
        return storage;
    }

    public static void validateSummary(String articleType, OutputSummaryStorage storage, boolean processedFull) throws Exception {
        File js = new File(Config.getConfigDir(), articleType + "-summary.js");
        if (!js.exists()) {
            return;
        }
        Collator c = Config.getConfig().getHeaderLocale() != null
                ? Collator.getInstance(new Locale(Config.getConfig().getHeaderLocale()))
                : null;
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("collator", c, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("processedFull", processedFull, ScriptContext.ENGINE_SCOPE);
        context.setAttribute("summaryStorage", storage, ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec(js.getAbsolutePath(), context);
    }
}
