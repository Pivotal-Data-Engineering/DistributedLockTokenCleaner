package io.pivotal.function;

import java.util.Properties;

import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Declarable;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionAdapter;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.gemstone.gemfire.distributed.internal.InternalDistributedSystem;

public abstract class FunctionBase extends FunctionAdapter implements Declarable {

    private static InternalDistributedSystem ds = InternalDistributedSystem.getAnyInstance();

    protected void logDebug(String message) {
        if (ds != null && ds.getLogWriter().fineEnabled()) {
            ds.getLogWriter().fine(message);
        }
    }

    protected void logInfo(String message) {
        if (ds != null && ds.getLogWriter().infoEnabled()) {
            ds.getLogWriter().info(message);
        }
    }

    protected void logError(String message) {
        if (ds != null && ds.getLogWriter().errorEnabled()) {
            ds.getLogWriter().error(message);
        }
    }

    protected void logError(String message, Throwable t) {
        if (ds != null && ds.getLogWriter().errorEnabled()) {
            ds.getLogWriter().error(message, t);
        }
    }

    protected DistributedLockService getLockService(Region region) {
        String serviceName = region.getFullPath() + "LockService";
        logDebug("getLockService " + serviceName);
        DistributedLockService dLS = DistributedLockService.getServiceNamed(serviceName);
        if (dLS == null) {
            dLS = DistributedLockService.create(serviceName, region.getCache().getDistributedSystem());
            logDebug("getLockService after create " + dLS);
        }
        return dLS;
    }
    
    protected void validatePdxReadSerialized() {
        if (!CacheFactory.getInstance(ds).getPdxReadSerialized()) {
            throw new FunctionException("PdxReadSerialized is disabled");
        }
    }

    public String getId() {
        return getClass().getSimpleName();
    }

    public void init(Properties arg0) {

    }
}
