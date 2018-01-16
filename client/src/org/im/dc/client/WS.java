package org.im.dc.client;

import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;

import org.im.dc.git.services.ArticleWebserviceGitImpl;
import org.im.dc.git.services.InitWebserviceGitImpl;
import org.im.dc.git.services.ToolsWebserviceGitImpl;
import org.im.dc.service.AppConst;
import org.im.dc.service.ArticleWebservice;
import org.im.dc.service.InitWebservice;
import org.im.dc.service.ToolsWebservice;
import org.im.dc.service.dto.Header;

public class WS {
    private static ArticleWebservice articleService;
    private static ToolsWebservice toolsWebservice;
    private static InitWebservice initWebservice;
    public static Header header;

    public static void initWS(String urlPrefix, String user, String pass) throws Exception {
        Service service;

        disableSSL();

        service = Service.create(new URL(urlPrefix + "/articles?wsdl"),
                new QName("http://impl.service.dc.im.org/", "ArticleWebserviceImplService"));
        articleService = service.getPort(ArticleWebservice.class);
        setupCompression((BindingProvider) articleService);

        service = Service.create(new URL(urlPrefix + "/tools?wsdl"),
                new QName("http://impl.service.dc.im.org/", "ToolsWebserviceImplService"));
        toolsWebservice = service.getPort(ToolsWebservice.class);
        setupCompression((BindingProvider) toolsWebservice);

        service = Service.create(new URL(urlPrefix + "/init?wsdl"),
                new QName("http://impl.service.dc.im.org/", "InitWebserviceImplService"));
        initWebservice = service.getPort(InitWebservice.class);
        setupCompression((BindingProvider) initWebservice);

        header = new Header();
        header.user = user;
        header.pass = pass;
        header.appVersion = AppConst.APP_VERSION;
    }

    public static void initGit(String url, String user) throws Exception {
        header = new Header();
        header.user = user;
        header.appVersion = AppConst.APP_VERSION;

        GitProc.INSTANCE = new GitProc(GitProc.REPO_DIR, url);
        articleService = new ArticleWebserviceGitImpl();
        toolsWebservice = new ToolsWebserviceGitImpl();
        initWebservice = new InitWebserviceGitImpl();
    }

    private static void setupCompression(BindingProvider provider) {
        Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
        httpHeaders.put("Accept-Encoding", Collections.singletonList("gzip"));
        //httpHeaders.put("Content-Encoding", Collections.singletonList("gzip"));
        Map<String, Object> reqContext = provider.getRequestContext();
        reqContext.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
    }

    private static void disableSSL() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    X509Certificate[] myTrustedAnchors = new X509Certificate[0];
                    return myTrustedAnchors;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            } };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception e) {
        }
    }

    public static ArticleWebservice getArticleService() {
        return articleService;
    }

    public static ToolsWebservice getToolsWebservice() {
        return toolsWebservice;
    }

    public static InitWebservice getInitWebservice() {
        return initWebservice;
    }
}
