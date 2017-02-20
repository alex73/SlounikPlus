package org.im.dc.server;

/**
 * Executor for some SQL statements inside transaction.
 */
@FunctionalInterface
public interface DbExecutorResult<T> {
    public abstract T run(Db.Api Api) throws Exception;
}
