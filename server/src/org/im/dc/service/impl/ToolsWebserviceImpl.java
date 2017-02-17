package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;

import javax.jws.WebService;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.bind.JAXBContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.gen.article.Root;
import org.im.dc.gen.config.State;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.AppConst;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;

@WebService(endpointInterface = "org.im.dc.service.ToolsWebservice")
public class ToolsWebserviceImpl implements ToolsWebservice {
    private void check(Header header) {
        if (header.appVersion != AppConst.APP_VERSION) {
            throw new RuntimeException("Wrong app version");
        }
        if (!Config.checkUser(header.user, header.pass)) {
            throw new RuntimeException("Unknown user");
        }
    }

    @Override
    public InitialData getInitialData(Header header) {
        check(header);

        InitialData result = new InitialData();
        result.articleSchema = Config.articleSchemaSource;
        for (State st : Config.getConfig().getStates().getState()) {
            result.states.add(st.getId());
        }
        return result;
    }

    @Override
    public void getStatistics() {
    }

    @Override
    public void validateAll() {
    }

    @Override
    public void reassignUsers() {
    }

    @Override
    public void addWords() {
    }

    static final JAXBContext XML_CONTEXT;

    static {
        try {
            XML_CONTEXT = JAXBContext.newInstance(Root.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    @Override
    public String printPreview(Header header, int articleId) throws Exception {
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            return null;
        }

        Validator validator = Config.articleSchema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(rec.getXml())));
        Root art = (Root) XML_CONTEXT.createUnmarshaller().unmarshal(new ByteArrayInputStream(rec.getXml()));

        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("words", rec.getWords(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", art, ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/output.js", context);

        return (String) context.getAttribute("out", ScriptContext.ENGINE_SCOPE);
    }
}
