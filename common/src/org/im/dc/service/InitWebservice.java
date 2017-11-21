package org.im.dc.service;

import javax.jws.WebService;

@WebService
public interface InitWebservice {
    String getConfigVersion(int appVersion);
}
