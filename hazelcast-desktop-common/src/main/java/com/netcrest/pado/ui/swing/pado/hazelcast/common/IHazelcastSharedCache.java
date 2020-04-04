package com.netcrest.pado.ui.swing.pado.hazelcast.common;

import java.util.TreeSet;

import org.hazelcast.addon.hql.HqlQuery;

import com.netcrest.pado.ui.swing.ISharedCache;

@SuppressWarnings("rawtypes")
public interface IHazelcastSharedCache extends ISharedCache {
	public TreeSet<IMapItem> getMapSet();
	
	public HqlQuery getHqlQueryInstance();
}
