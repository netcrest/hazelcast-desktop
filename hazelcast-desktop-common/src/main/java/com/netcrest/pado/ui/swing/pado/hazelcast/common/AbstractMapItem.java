package com.netcrest.pado.ui.swing.pado.hazelcast.common;

import java.util.TreeSet;

public abstract class AbstractMapItem implements IMapItem {
	protected String mapPath;
	protected String name;

	protected TreeSet<IMapItem> childSet;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getFullPath() {
		return mapPath;
	}

	@Override
	public abstract String getMapName();

	@Override
	public TreeSet<IMapItem> getChildSet(boolean recursive) {
		if (recursive) {
			// TODO:
			return childSet;
		} else {
			return childSet;
		}
	}
	
	@Override
	public TreeSet<IMapItem> getChildSet() {
		return getChildSet(false);
	}
	
	@Override
	public void setChildSet(TreeSet<IMapItem> childSet) {
		this.childSet = childSet;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int compareTo(Object o) {
		if (o == null) {
			return 1;
		}
		return mapPath.compareTo(((AbstractMapItem) o).mapPath);
	}
}