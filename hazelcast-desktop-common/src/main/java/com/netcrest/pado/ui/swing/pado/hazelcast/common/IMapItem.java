package com.netcrest.pado.ui.swing.pado.hazelcast.common;

import java.util.TreeSet;

@SuppressWarnings("rawtypes")
public interface IMapItem extends Comparable {
	public String getName();
	
	public String getFullPath();

	public String getMapName();

	public TreeSet<IMapItem> getChildSet(boolean recursive);
	
	public TreeSet<IMapItem> getChildSet();
	
	public void setChildSet(TreeSet<IMapItem> childSet);
}
