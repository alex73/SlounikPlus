package org.im.dc.client.ui;

import org.im.dc.service.dto.ArticleFull;

public interface IArticleUpdatedListener {
    void onArticleUpdated(ArticleFull article);
}
