package org.im.dc.service.impl.js;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
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
    private final CompiledScript compiledScript;

    public JsProcessing(String scriptFile) throws Exception {
        engine = FACTORY.getEngineByName("graal.js");
        script = IOUtils.toString(new File(scriptFile).toURI(), "UTF-8");
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        compiledScript = ((Compilable) engine).compile(script);
    }

    public void exec(ScriptContext context) throws Exception {
        synchronized (preprocessors) {
            for (JsPreprocessor p : preprocessors) {
                p.prepare(context);
            }
        }
        compiledScript.eval(context);
    }

    @Override
    public void close() throws IOException {
    }

    public interface JsPreprocessor {
        void prepare(ScriptContext context) throws Exception;
    }
}
