package org.im.dc.service.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

import javax.jws.WebService;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;

import org.im.dc.gen.config.Permission;
import org.im.dc.gen.config.State;
import org.im.dc.gen.config.User;
import org.im.dc.server.Config;
import org.im.dc.server.Db;
import org.im.dc.server.db.RecArticle;
import org.im.dc.server.js.JsDomWrapper;
import org.im.dc.server.js.JsProcessing;
import org.im.dc.service.AppConst;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.Header;
import org.im.dc.service.dto.InitialData;

@WebService(endpointInterface = "org.im.dc.service.ToolsWebservice")
public class ToolsWebserviceImpl implements ToolsWebservice {
    private void check(Header header, Permission... perms) {
        if (header.appVersion != AppConst.APP_VERSION) {
            throw new RuntimeException("Wrong app version");
        }
        if (!Config.checkUser(header.user, header.pass)) {
            throw new RuntimeException("Unknown user");
        }
        for (Permission p : perms) {
            if (!Config.checkPerm(header.user, p)) {
                throw new RuntimeException("No permission for this operation");
            }
        }
    }

    @Override
    public InitialData getInitialData(Header header) {
        check(header);

        InitialData result = new InitialData();
        result.articleSchema = Config.articleSchemaSource;
        result.states = new ArrayList<>();
        for (State st : Config.getConfig().getStates().getState()) {
            result.states.add(st.getId());
        }
        result.allUsers = new ArrayList<>();
        for (User u : Config.getConfig().getUsers().getUser()) {
            result.allUsers.add(u.getName());
        }
        result.currentUserPermissions = Config.getUserPermissions(header.user);
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

    @Override
    public String printPreview(Header header, int articleId) throws Exception {
        check(header);

        RecArticle rec = Db.execAndReturn((api) -> api.getArticleMapper().selectArticle(articleId));
        if (rec == null) {
            return null;
        }

        Validator validator = Config.articleSchema.newValidator();
        validator.validate(new StreamSource(new ByteArrayInputStream(rec.getXml())));

        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("words", rec.getWords(), ScriptContext.ENGINE_SCOPE);
        context.setAttribute("article", new JsDomWrapper(rec.getXml()), ScriptContext.ENGINE_SCOPE);
        JsProcessing.exec("config/output.js", context);

        return (String) context.getAttribute("out", ScriptContext.ENGINE_SCOPE);
    }
}
