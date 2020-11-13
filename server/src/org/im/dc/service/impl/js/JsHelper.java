package org.im.dc.service.impl.js;

import java.io.File;
import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.validation.Validator;

import org.im.dc.server.Config;
import org.im.dc.server.db.RecArticle;
import org.im.dc.service.OutputSummaryStorage;
import org.im.dc.service.impl.ValidationHelper;
import org.im.dc.service.impl.WordSplitter;
import org.w3c.dom.Document;

public class JsHelper {
    public static Map<String, Comparator<String>> COMPARATORS = new TreeMap<>();

    public static OutputSummaryStorage previewSomeArticles(String articleType, List<RecArticle> articles)
            throws Exception {
        OutputSummaryStorage storage = new OutputSummaryStorage();
        Validator validator = Config.schemas.get(articleType).newValidator();
        try (JsProcessing js = new JsProcessing(
                new File(Config.getConfigDir(), articleType + ".js").getAbsolutePath())) {
            Map<String, Object> contextVar = new TreeMap<>();
            for (RecArticle a : articles) {
                ValidationHelper helper = new ValidationHelper(a.getArticleId(), validator, a.getXml(), storage);
                SimpleScriptContext context = new SimpleScriptContext();
                context.setAttribute("helper", helper, ScriptContext.ENGINE_SCOPE);
                Document doc = JsDomWrapper.parseDoc(a.getXml());
                context.setAttribute("articleDoc", doc, ScriptContext.ENGINE_SCOPE);
                context.setAttribute("articleState", a.getState(), ScriptContext.ENGINE_SCOPE);
                context.setAttribute("article", new JsDomWrapper(doc.getDocumentElement()), ScriptContext.ENGINE_SCOPE);
                context.setAttribute("context", contextVar, ScriptContext.ENGINE_SCOPE);
                js.exec(context);
                storage.textForSearch.put(a.getArticleId(), new WordSplitter(Config.getConfig().getStress()).parse(a.getXml()));
            }
        }
        Comparator<String> comparator = COMPARATORS.get(articleType);
        if (comparator != null) {
            storage.outputs.sort((a, b) -> comparator.compare(a.key, b.key));
        }
        return storage;
    }

    public static void validateSummary(String articleType, OutputSummaryStorage storage) throws Exception {
        File script = new File(Config.getConfigDir(), articleType + "-summary.js");
        if (!script.exists()) {
            return;
        }
        Collator c = Config.getConfig().getHeaderLocale() != null
                ? Collator.getInstance(new Locale(Config.getConfig().getHeaderLocale()))
                : null;
        try (JsProcessing js = new JsProcessing(script.getAbsolutePath())) {
            SimpleScriptContext context = new SimpleScriptContext();
            context.setAttribute("collator", c, ScriptContext.ENGINE_SCOPE);
            context.setAttribute("summaryStorage", storage, ScriptContext.ENGINE_SCOPE);
            js.exec(context);
        }
    }
}
