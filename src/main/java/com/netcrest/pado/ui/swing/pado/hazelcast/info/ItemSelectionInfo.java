package com.netcrest.pado.ui.swing.pado.hazelcast.info;

import com.netcrest.pado.ui.swing.pado.hazelcast.HazelcastSharedCache;

public class ItemSelectionInfo
{
	private String gridId;
	private HazelcastSharedCache.MapItem mapItem;

	public ItemSelectionInfo(String gridId, HazelcastSharedCache.MapItem item)
	{
		this.gridId = gridId;
		this.mapItem = item;
	}

	public String getGridId()
	{
		return gridId;
	}

	public void setGridId(String gridId)
	{
		this.gridId = gridId;
	}

	public HazelcastSharedCache.MapItem getItem()
	{
		return mapItem;
	}

	public void setItem(HazelcastSharedCache.MapItem item)
	{
		this.mapItem = item;
	}
}
