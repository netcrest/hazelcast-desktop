package com.netcrest.pado.ui.swing.pado.hazelcast.info;

import com.netcrest.pado.ui.swing.pado.hazelcast.common.IMapItem;

public class ItemSelectionInfo
{
	private String gridId;
	private IMapItem mapItem;

	public ItemSelectionInfo(String gridId, IMapItem item)
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

	public IMapItem getItem()
	{
		return mapItem;
	}

	public void setItem(IMapItem item)
	{
		this.mapItem = item;
	}
}
