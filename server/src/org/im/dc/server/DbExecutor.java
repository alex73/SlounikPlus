package org.im.dc.server;

/**
 * Executor for some SQL statements inside transaction.
 */
@FunctionalInterface
public interface DbExecutor<T> {
    public abstract T run(Db.Api Api);
}
