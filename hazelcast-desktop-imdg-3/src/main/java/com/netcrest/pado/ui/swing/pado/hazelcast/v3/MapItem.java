package com.netcrest.pado.ui.swing.pado.hazelcast.v3;

import java.util.TreeSet;

import com.hazelcast.core.IMap;
import com.netcrest.pado.ui.swing.pado.hazelcast.common.AbstractMapItem;
import com.netcrest.pado.ui.swing.pado.hazelcast.common.IMapItem;

@SuppressWarnings("rawtypes")
public class MapItem extends AbstractMapItem {

	IMap map;
	
	public MapItem(String name, String mapPath, IMap map, TreeSet<IMapItem> childSet) {
		this.name = name;
		this.mapPath = mapPath;
		this.map = map;
		setChildSet(childSet);
	}
	
	@Override
	public String getMapName() {
		if (map == null) {
			return null;
		} else {
			return map.getName();
		}
	}

}
