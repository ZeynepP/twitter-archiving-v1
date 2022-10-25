package fr.ina.dlweb.dowser.ws.twitter.model.response;


import java.util.List;
import java.util.Map;

public class CacheInfoResponse {
	
	private List<CacheInfo> cache_infos;
	
	public CacheInfoResponse() {
	}

	/**
	 * @return the cache_infos
	 */
	public List<CacheInfo> getCache_infos() {
		return cache_infos;
	}

	/**
	 * @param cache_infos the cache_infos to set
	 */
	public void setCache_infos(List<CacheInfo> cache_infos) {
		this.cache_infos = cache_infos;
	}


	public static class CacheInfo {
	
		private String cluster;
		private String type;
		private Map<String, String> cacheData;
		private long cacheSize;
		private Stats stats;
		
		
		
		public CacheInfo() {
			super();
		}
		
		/**
		 * @return the cluster
		 */
		public String getCluster() {
			return cluster;
		}
	
		/**
		 * @return the type
		 */
		public String getType() {
			return type;
		}
	
		/**
		 * @return the cacheData
		 */
		public Map<String, String> getCacheData() {
			return cacheData;
		}
	
		/**
		 * @return the stats
		 */
		public Stats getStats() {
			return stats;
		}
	
		/**
		 * @param cluster the cluster to set
		 */
		public CacheInfo setCluster(String cluster) {
			this.cluster = cluster;
			return this;
		}
	
		/**
		 * @param type the type to set
		 */
		public CacheInfo setType(String type) {
			this.type = type;
			return this;
		}
	
		/**
		 * @param cacheData the cacheData to set
		 */
		public CacheInfo setCacheData(Map<String, String> cacheData) {
			this.cacheData = cacheData;
			return this;
		}
	
		/**
		 * @param stats the stats to set
		 */
		public CacheInfo setStats(Stats stats) {
			this.stats = stats;
			return this;
		}

		public long getCacheSize() {
			return cacheSize;
		}

		public void setCacheSize(long cacheSize) {
			this.cacheSize = cacheSize;
		}
	}

	public static class Stats {
		
		private long requestCount;
		private long hitCount;
		private double hitRate;
		private long missCount;
		private double missRate;
		private long loadCount;
		private long loadSuccessCount;
		private long loadExceptionCount;
		private double loadExceptionRate;
		private long totalLoadTime;
		private double avgLoadPenalty;
		private long evictionCount;
		

		public Stats() {
			super();
		}

		/**
		 * @return the requestCount
		 */
		public long getRequestCount() {
			return requestCount;
		}

		/**
		 * @return the hitCount
		 */
		public long getHitCount() {
			return hitCount;
		}

		/**
		 * @return the hitRate
		 */
		public double getHitRate() {
			return hitRate;
		}

		/**
		 * @return the missCount
		 */
		public long getMissCount() {
			return missCount;
		}

		/**
		 * @return the missRate
		 */
		public double getMissRate() {
			return missRate;
		}

		/**
		 * @return the loadCount
		 */
		public long getLoadCount() {
			return loadCount;
		}

		/**
		 * @return the loadSuccessCount
		 */
		public long getLoadSuccessCount() {
			return loadSuccessCount;
		}

		/**
		 * @return the loadExceptionCount
		 */
		public long getLoadExceptionCount() {
			return loadExceptionCount;
		}

		/**
		 * @return the loadExceptionRate
		 */
		public double getLoadExceptionRate() {
			return loadExceptionRate;
		}

		/**
		 * @return the totalLoadTime
		 */
		public long getTotalLoadTime() {
			return totalLoadTime;
		}

		/**
		 * @return the avgLoadPenalty
		 */
		public double getAvgLoadPenalty() {
			return avgLoadPenalty;
		}

		/**
		 * @return the evictionCount
		 */
		public long getEvictionCount() {
			return evictionCount;
		}

		/**
		 * @param requestCount the requestCount to set
		 */
		public Stats setRequestCount(long requestCount) {
			this.requestCount = requestCount;
			return this;
		}

		/**
		 * @param hitCount the hitCount to set
		 */
		public Stats setHitCount(long hitCount) {
			this.hitCount = hitCount;
			return this;
		}

		/**
		 * @param hitRate the hitRate to set
		 */
		public Stats setHitRate(double hitRate) {
			this.hitRate = hitRate;
			return this;
		}

		/**
		 * @param missCount the missCount to set
		 */
		public Stats setMissCount(long missCount) {
			this.missCount = missCount;
			return this;
		}

		/**
		 * @param missRate the missRate to set
		 */
		public Stats setMissRate(double missRate) {
			this.missRate = missRate;
			return this;
		}

		/**
		 * @param loadCount the loadCount to set
		 */
		public Stats setLoadCount(long loadCount) {
			this.loadCount = loadCount;
			return this;
		}

		/**
		 * @param loadSuccessCount the loadSuccessCount to set
		 */
		public Stats setLoadSuccessCount(long loadSuccessCount) {
			this.loadSuccessCount = loadSuccessCount;
			return this;
		}

		/**
		 * @param loadExceptionCount the loadExceptionCount to set
		 */
		public Stats setLoadExceptionCount(long loadExceptionCount) {
			this.loadExceptionCount = loadExceptionCount;
			return this;
		}

		/**
		 * @param loadExceptionRate the loadExceptionRate to set
		 */
		public Stats setLoadExceptionRate(double loadExceptionRate) {
			this.loadExceptionRate = loadExceptionRate;
			return this;
		}

		/**
		 * @param totalLoadTime the totalLoadTime to set
		 */
		public Stats setTotalLoadTime(long totalLoadTime) {
			this.totalLoadTime = totalLoadTime;
			return this;
		}

		/**
		 * @param avgLoadPenalty the avgLoadPenalty to set
		 */
		public Stats setAvgLoadPenalty(double avgLoadPenalty) {
			this.avgLoadPenalty = avgLoadPenalty;
			return this;
		}

		/**
		 * @param evictionCount the evictionCount to set
		 */
		public Stats setEvictionCount(long evictionCount) {
			this.evictionCount = evictionCount;
			return this;
		}		

	}
	
}
