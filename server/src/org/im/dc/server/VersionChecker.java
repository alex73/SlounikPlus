package org.im.dc.server;

import org.apache.commons.lang3.StringUtils;
import org.im.dc.service.AppConst;
import org.im.dc.service.dto.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks versions against application version and config version.
 */
public class VersionChecker {
    private static final Logger LOG = LoggerFactory.getLogger(VersionChecker.class);

    public static void check(Header header) throws Exception {
        if (header.appVersion != AppConst.APP_VERSION) {
            LOG.warn("<< check: version required " + AppConst.APP_VERSION + " but requested " + header.appVersion);
            throw new RuntimeException("Wrong app version");
        }
        if (!StringUtils.equals(Config.getConfig().getVersion(), header.configVersion)) {
            LOG.warn("<< check: config version required " + Config.getConfig().getVersion() + " but requested "
                    + header.configVersion);
            throw new RuntimeException("Wrong config version");
        }
    }
}
