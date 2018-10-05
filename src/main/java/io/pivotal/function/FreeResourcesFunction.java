package io.pivotal.function;

import java.util.Map;

import com.gemstone.gemfire.cache.Cache;
import com.gemstone.gemfire.cache.CacheFactory;
import com.gemstone.gemfire.cache.Region;
import com.gemstone.gemfire.cache.execute.FunctionContext;
import com.gemstone.gemfire.cache.execute.RegionFunctionContext;
import com.gemstone.gemfire.cache.partition.PartitionRegionHelper;
import com.gemstone.gemfire.distributed.DistributedLockService;
import com.gemstone.gemfire.distributed.internal.locks.DLockService;
import com.gemstone.gemfire.distributed.internal.locks.DistributedLockStats;

public class FreeResourcesFunction extends FunctionBase {

	private static final long serialVersionUID = 1292187788913405545L;

	@Override
	public void execute(FunctionContext fc) {
		logInfo("FreeResourcesFunction execution started on the SharedContext region");
		RegionFunctionContext rc = (RegionFunctionContext) fc;
		
		Cache cache = CacheFactory.getAnyInstance();
		String memberId = cache.getDistributedSystem().getMemberId();

		String answer = "";
		int tokensFreed = 0;
		int tokensStart = 0;
		if (PartitionRegionHelper.isPartitionedRegion(rc.getDataSet())) {
			Region sharedContextRegion = PartitionRegionHelper.getLocalDataForContext(rc);
			DistributedLockService dLS = getLockService(sharedContextRegion);
			Map<String, DLockService> services = DLockService.snapshotAllServices();
			for (DLockService service : services.values()) {
				String serviceName = service.getName();
				
				// only process "LockService" names because that is what they are creating 
				if (!serviceName.endsWith("LockService")) {
					continue;
				}
				
				try {
					logInfo("Freeing resources for service named: " + service.getName());
					DistributedLockStats dLockStats = service.getStats();
					tokensStart = dLockStats.getTokens();
					logInfo("Token Count at start: " + tokensStart);

					service.freeResources(null);
					tokensFreed = tokensStart - dLockStats.getTokens(); 
					answer = answer + memberId + ":" + service.getName() + ":" +  tokensFreed + " | ";
					logInfo("Token Count at end: " + dLockStats.getTokens());
				} catch (Exception e) {
					logInfo("Error processing service " + service.getName() + "\n" + e.getMessage());
					e.printStackTrace();
				}
			}
		} else {
			logInfo("SharedContext Region is not partitioned");
			fc.getResultSender().lastResult(false);
		}

		logInfo("FreeResourcesFunction execution ended");
		fc.getResultSender().lastResult(answer);
	}
}
