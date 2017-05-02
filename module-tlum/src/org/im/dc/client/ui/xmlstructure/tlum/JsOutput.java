package org.im.dc.client.ui.xmlstructure.tlum;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import javax.xml.bind.JAXBContext;

public class JsOutput {
    private static final ScriptEngineManager FACTORY = new ScriptEngineManager();

    public static String exec(OneParadigmInfo info) throws Exception {
        ScriptEngine engine = FACTORY.getEngineByName("JavaScript");

        StringWriter out = new StringWriter();
        SimpleScriptContext context = new SimpleScriptContext();
        context.setWriter(out);
        context.setAttribute("para", info, ScriptContext.ENGINE_SCOPE);
        try (BufferedReader rd = new BufferedReader(
                new InputStreamReader(JsOutput.class.getResourceAsStream("para.js"), "UTF-8"))) {
            engine.eval(rd, context);
        }
        return out.toString();
    }

    public static void main(String[] args) throws Exception {
        OneParadigmInfo info = (OneParadigmInfo) JAXBContext.newInstance(OneParadigmInfo.class).createUnmarshaller()
                .unmarshal(new File("a.xml"));

        System.out.println(exec(info));
    }
}
