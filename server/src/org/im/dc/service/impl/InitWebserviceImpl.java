package org.im.dc.service.impl;

import javax.jws.WebService;

import org.im.dc.server.Config;
import org.im.dc.service.AppConst;
import org.im.dc.service.InitWebservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(endpointInterface = "org.im.dc.service.InitWebservice")
public class InitWebserviceImpl implements InitWebservice {
    private static final Logger LOG = LoggerFactory.getLogger(InitWebserviceImpl.class);

    @Override
    public String getConfigVersion(int appVersion) {
        LOG.info(">> getConfigVersion(" + appVersion + ")");
        if (appVersion != AppConst.APP_VERSION) {
            LOG.warn(
                    "<< InitWebserviceImpl: version required " + AppConst.APP_VERSION + " but requested " + appVersion);
            throw new RuntimeException("Wrong app version");
        }
        String r = Config.getConfig().getVersion();
        LOG.info("<< getInitialData");
        return r;
    }
}
