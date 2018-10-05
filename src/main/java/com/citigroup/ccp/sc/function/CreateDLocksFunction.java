package com.citigroup.ccp.sc.function;

import com.citigroup.ccp.cache.FunctionBase;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.FunctionException;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.gemstone.gemfire.distributed.internal.locks.DLockService;
import com.gemstone.gemfire.distributed.internal.locks.DistributedLockStats;

public class CreateDLocksFunction extends FunctionBase {

	private static final long serialVersionUID = 1292187788913405545L;

	@Override
	public void execute(FunctionContext fc) {
		logInfo("CreateDLocksFunction execution started on the SharedContext region");
		RegionFunctionContext rc = (RegionFunctionContext) fc;
		if (PartitionRegionHelper.isPartitionedRegion(rc.getDataSet())) {
			Region region = PartitionRegionHelper.getLocalDataForContext(rc);
			DistributedLockService dLS = getLockService(region);
			String serviceName = region.getFullPath() + "LockService";

			try {
				for (int i = 0; i < 10000; i++) {
					String key = Integer.toString(i);
					region.put(key, Integer.toString(i));

					boolean locked = dLS.lock(key, 1000L, 1000L);

					if (!locked) {
						throw new FunctionException("CreateDLocksFunction could not grab lock for the key: " + key);
					}

					if (locked) {
						dLS.unlock(key);
					}
				}

				DLockService dLockService = (DLockService) dLS;
				DistributedLockStats dLockStats = dLockService.getStats();
				logInfo("Token Count at end: " + dLockStats.getTokens());

			} catch (Exception e) {
				logInfo("CreateDLocksFunction error: " + e.getMessage() + "\n");
				e.printStackTrace();
			}

			logInfo("CreateDLocksFunction execution ended");
			fc.getResultSender().lastResult(true);
		}
	}
}
