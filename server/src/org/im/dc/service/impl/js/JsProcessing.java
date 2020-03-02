package org.im.dc.service.impl.js;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.IOUtils;

public class JsProcessing implements Closeable {

    private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

    private static final List<JsPreprocessor> preprocessors = new ArrayList<>();

    public static void addPreprocessor(JsPreprocessor preprocessor) {
        synchronized (preprocessors) {
            preprocessors.add(preprocessor);
        }
    }

    private final ScriptEngine engine;
    private final String script;

    public JsProcessing(String scriptFile) throws Exception {
        engine = FACTORY.getEngineByName("JavaScript");
        script = IOUtils.toString(new File(scriptFile).toURI(), "UTF-8");
    }

    public void exec(ScriptContext context) throws Exception {
        synchronized (preprocessors) {
            for (JsPreprocessor p : preprocessors) {
                p.prepare(context);
            }
        }
        engine.eval(script, context);
    }

    @Override
    public void close() throws IOException {
    }

    public interface JsPreprocessor {
        void prepare(ScriptContext context) throws Exception;
    }
}
