package org.im.dc.client.remote;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.im.dc.client.WS;
import org.im.dc.service.dto.ArticleFullInfo;
import org.junit.Test;

public class RemotePerformanceTest {
    @Test
    void ordered() throws Exception {
        for (int i = 0; i < 100; i++) {
            ArticleFullInfo a = WS.getArticleService().getArticleFullInfo(WS.header, 1);
            // System.out.println(a.words);
        }
    }

    @Test
    void parallel() throws Exception {
        ExecutorService EXEC = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 100; i++) {
            EXEC.execute(() -> {
                try {
                    ArticleFullInfo a = WS.getArticleService().getArticleFullInfo(WS.header, 1);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
        EXEC.shutdown();
        EXEC.awaitTermination(10, TimeUnit.MINUTES);
    }
}
