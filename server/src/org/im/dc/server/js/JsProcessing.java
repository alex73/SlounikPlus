package org.im.dc.server.js;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.im.dc.server.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsProcessing {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

    public static void exec(String scriptFile, ScriptContext context) throws Exception {
        LOG.debug(">> Execute script from " + scriptFile);
        ScriptEngine engine = FACTORY.getEngineByName("JavaScript");
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(scriptFile), "UTF-8"))) {
            engine.eval(rd, context);
        }
        LOG.debug("<< Execute script from " + scriptFile);
    }
}
