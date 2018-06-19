package org.im.dc.server;

/**
 * Executor for some SQL statements inside transaction.
 */
@FunctionalInterface
public interface DbExecutor {
    public abstract void run(Db.Api Api) throws Exception;
}
