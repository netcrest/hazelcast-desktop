package com.netcrest.pado.ui.swing.pado.hazelcast.common;

public class HazelcastSharedCache {
	private final static String PROPERTY_SHARED_CACHE_CLASS = "pado.sharedCache.class";

	private static IHazelcastSharedCache sharedCache;
	
	static {
		try {
			String sharedCacheClassName = System.getProperty(PROPERTY_SHARED_CACHE_CLASS,
					"com.netcrest.pado.ui.swing.pado.hazelcast.v3.HazelcastSharedCacheV3");
			Class<?> cls = Class.forName(sharedCacheClassName);
			sharedCache = (IHazelcastSharedCache) cls.newInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static IHazelcastSharedCache getSharedCache() {
		return sharedCache;
	}
}