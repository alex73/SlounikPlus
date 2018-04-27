package org.im.dc.server.js;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class JsProcessing {

    private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

    private static final List<JsPreprocessor> preprocessors = new ArrayList<>();

    public static void addPreprocessor(JsPreprocessor preprocessor) {
        synchronized (preprocessors) {
            preprocessors.add(preprocessor);
        }
    }

    public static void exec(String scriptFile, ScriptContext context) throws Exception {
        synchronized (preprocessors) {
            for (JsPreprocessor p : preprocessors) {
                p.prepare(context);
            }
        }
        ScriptEngine engine = FACTORY.getEngineByName("JavaScript");
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), "UTF-8"))) {
            engine.eval(rd, context);
        }
    }

    public interface JsPreprocessor {
        void prepare(ScriptContext context) throws Exception;
    }
}
