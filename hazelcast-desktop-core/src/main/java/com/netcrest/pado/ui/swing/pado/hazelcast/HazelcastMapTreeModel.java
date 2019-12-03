/*
 * Copyright (c) 2000 Netcrest Technologies, LLC.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Netcrest Technologies, LLC. Your use of this software is
 * strictly bounded to the terms of the license agreement you
 * established with Netcrest Technologies, LLC. Redistribution
 * and use in source and binary forms, with or without modification,
 * are strictly enforced by such a license agreement,
 * which you must obtain from Netcrest Technologies, LLC prior
 * to your action.
 */
package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import com.netcrest.pado.ui.swing.pado.hazelcast.common.HazelcastSharedCache;
import com.netcrest.pado.ui.swing.pado.hazelcast.common.IMapItem;

public class HazelcastMapTreeModel extends DefaultTreeModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private TreeSet<IMapItem> itemSet;
	boolean showTemporalOnly = false;
	private boolean showHiddenItems = false;

	/** Node Map. */
	/**
	 * key = TreeNode, value = MapItem.
	 */
	private HashMap<TreeNode, IMapItem> itemMap = new HashMap<TreeNode, IMapItem>();

	/**
	 * key = MapItem, value = TreeNode.
	 */
	private HashMap<IMapItem, TreeNode> treeNodeMap = new HashMap<IMapItem, TreeNode>();

	/** Constructs a model from the cache root. */
	public HazelcastMapTreeModel() {
		super(new DefaultMutableTreeNode());
		reset();
	}

	//
	// Public methods
	//

	public void reset() {
		reset(HazelcastSharedCache.getSharedCache().getMapSet(), showHiddenItems);
	}

	public void reset(TreeSet<IMapItem> itemSet, boolean showHiddenItems) {
		this.showHiddenItems = showHiddenItems;
		this.itemSet = itemSet;
		clear();
		try {
			buildTree();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		fireTreeStructureChanged(this, new Object[] { getRoot() }, new int[0], new Object[0]);

	}

	public void setShowHiddenMapItems(boolean showHiddenMapItems) {
		this.showHiddenItems = showHiddenMapItems;
	}

	public boolean isShowHiddenMapItems() {
		return showHiddenItems;
	}

	public void setShowTemporalOnly(boolean showTemporalOnly) {
		this.showTemporalOnly = showTemporalOnly;
	}

	public boolean isShowTemporalOnly() {
		return showTemporalOnly;
	}

	public synchronized void clear() {
		// clear tree and re-populate
		((DefaultMutableTreeNode) getRoot()).removeAllChildren();
		itemMap.clear();
		treeNodeMap.clear();
		reload();
	}

	/** get the org.w3c.Node for a MutableTreeNode. */
	public IMapItem getItem(Object treeNode) {
		return itemMap.get(treeNode);
	}

	public MutableTreeNode getTreeNode(IMapItem mapItem) {
		return (MutableTreeNode) treeNodeMap.get(mapItem);
	}

	/**
	 * Return the first tree node in parent that matches the childNode's node name.
	 * It returns null if not found.
	 */
	public MutableTreeNode findTreeNode(IMapItem childItem, MutableTreeNode parent) {
		IMapItem parentItem = getItem(parent);
		if (parentItem != null) {
			TreeSet<IMapItem> parentMapItemSet = parentItem.getChildSet(false);
			for (IMapItem item : parentMapItemSet) {
				if (childItem.getName().equals(item.getName())) {
					return getTreeNode(item);
				}
			}
		}
		return null;
	}

	//
	// Private methods
	//

	/** Builds the tree. */
	private void buildTree() {
		// iterate over children of this node

		MutableTreeNode root = (MutableTreeNode) getRoot();
		IMapItem rootItem = itemSet.first();
		TreeSet<IMapItem> rootItemSet = rootItem.getChildSet(false);
		if (rootItemSet == null) {
			return;
		}
		MutableTreeNode rootTreeNode = insertRootNode(rootItem, root);
		for (IMapItem item : rootItemSet) {
			addItemNode(item, rootTreeNode);
		}

	} // buildTree()

	private MutableTreeNode insertNodeOnly(IMapItem childItem, MutableTreeNode parentTreeNode) {
		// If there is a node in parent which has the same node name
		// as childNode then return that node's tree node.
		MutableTreeNode treeNode = findTreeNode(childItem, parentTreeNode);
		if (treeNode != null) {
			return treeNode;
		}

		// Did not find a matching node. Let's insert childNode.
		treeNode = addTreeNode(childItem, parentTreeNode, true);
		treeNodeMap.put(childItem, treeNode);
		return treeNode;
	}

	/**
	 * @param treeOnly If true, adds childNode to the tree only; otherwise, adds
	 *                 childNode to the tree and the parent XML node.
	 */
	private MutableTreeNode addChildNode(IMapItem childItem, MutableTreeNode parentTreeNode,
			boolean treeOnly) {
		MutableTreeNode root = (MutableTreeNode) getRoot();
		MutableTreeNode newTreeNode = null;
		if (childItem == null) {
			newTreeNode = insertRootNode(childItem, root);
		} else {
			newTreeNode = addItemNode(childItem, parentTreeNode);
		}
		return newTreeNode;
	}

	/**
	 * Adds childNode to both the parent tree node and parent XML node.
	 * 
	 * @return Returns the newly created tree node that represents childNode.
	 */
	public MutableTreeNode addChildNode(IMapItem childItem, MutableTreeNode parentTreeNode) {
		return addChildNode(childItem, parentTreeNode, false);
	}

	/**
	 * Recursively adds node and treeNode to the maps. It assumes node and treeNode
	 * are fully associated such that their children can be mapped one to one.
	 */
	private void addNodeToMaps(IMapItem mapItem, MutableTreeNode treeNode) {
		itemMap.put(treeNode, mapItem);
		treeNodeMap.put(mapItem, treeNode);
		Set<IMapItem> mapItemSet = mapItem.getChildSet(false);
		int i = 0;
		for (IMapItem item2 : mapItemSet) {
			MutableTreeNode childTreeNode = (MutableTreeNode) treeNode.getChildAt(i++);
			addNodeToMaps(item2, childTreeNode);
		}
	}

	/**
	 * Removes the specified node. It returns the corresponding tree node.
	 * 
	 * @param node The node to remove.
	 */
	private void removeNode(IMapItem mapItem, ArrayList nodeArray) {
		if (mapItem == null) {
			return;
		}
		MutableTreeNode treeNode = (MutableTreeNode) treeNodeMap.get(mapItem);
		if (treeNode == null) {
			return;
		}
		this.removeNodeFromParent(treeNode);
		removeNodeFromMaps(mapItem, nodeArray);
	}

	public void removeNode(IMapItem mapItem) {
		removeNode(mapItem, null);
	}

	public ArrayList removeNodeAndGetPairs(IMapItem item) {
		ArrayList nodeArray = new ArrayList(14);
		removeNode(item, nodeArray);
		return nodeArray;
	}

	public IMapItem getItem(TreeNode treeNode) {
		return itemMap.get(treeNode);
	}

	/**
	 * Removes the specified node from the nodeMap and treeNodeMap. It recusively
	 * removes all of the children.
	 */
	private void removeNodeFromMaps(IMapItem item, ArrayList nodeArray) {
		TreeNode treeNode = (TreeNode) treeNodeMap.get(item);
		treeNodeMap.remove(item);
		itemMap.remove(treeNode);
		if (nodeArray != null) {
			nodeArray.add(new NodePair(item, treeNode));
		}
		Set<IMapItem> itemSet = item.getChildSet(false);
		for (IMapItem item2 : itemSet) {
			removeNodeFromMaps(item2, nodeArray);
		}
	}

	private boolean hasChildNode(IMapItem childItem, IMapItem parentItem) {
		if (childItem == null || parentItem == null) {
			return false;
		}
		Set<IMapItem> childSet = parentItem.getChildSet(false);

		for (IMapItem mapItem : childSet) {
			if (mapItem == childItem) {
				return true;
			}
		}
		return false;
	}

	private String getItemNameForTree(IMapItem mapItem) {
		if (mapItem == null) {
			return "/"; // root node
		}

		return mapItem.getName();
	}

	private MutableTreeNode insertBeforeTreeNode(IMapItem childItem,
			IMapItem refChildItem, MutableTreeNode parentChildTreeNode, boolean treeOnly) {
		String childTreeNodeName = getItemNameForTree(childItem);
		MutableTreeNode childTreeNode = new DefaultMutableTreeNode(childTreeNodeName);
		MutableTreeNode refChildTreeNode = (MutableTreeNode) treeNodeMap.get(refChildItem);
		insertNodeInto(childTreeNode, parentChildTreeNode, parentChildTreeNode.getIndex(refChildTreeNode));
		itemMap.put(childTreeNode, childItem);
		treeNodeMap.put(childItem, childTreeNode);
		return childTreeNode;
	}

	/**
	 * Inserts node before refTreeNode. If node is Attr then it is appended as
	 * refTreeNode's attribute.
	 */
	private MutableTreeNode insertBeforeTreeNode(IMapItem childItem,
			MutableTreeNode refChildTreeNode, boolean treeOnly) {
		String childTreeNodeName = getItemNameForTree(childItem);
		MutableTreeNode childTreeNode = new DefaultMutableTreeNode(childTreeNodeName);
		MutableTreeNode parentTreeNode = (MutableTreeNode) refChildTreeNode.getParent();
		insertNodeInto(childTreeNode, parentTreeNode, parentTreeNode.getIndex(refChildTreeNode));
		itemMap.put(childTreeNode, childItem);
		treeNodeMap.put(childItem, childTreeNode);
		return childTreeNode;
	}

	/**
	 * Adds a new tree node as the last child in the specified parentTreeNode.
	 */
	private MutableTreeNode addTreeNode(IMapItem childItem, MutableTreeNode parentTreeNode,
			boolean treeOnly) {
		String childTreeNodeName = getItemNameForTree(childItem);
		MutableTreeNode childTreeNode = new DefaultMutableTreeNode(childTreeNodeName);
		insertNodeInto(childTreeNode, parentTreeNode, parentTreeNode.getChildCount());
		itemMap.put(childTreeNode, childItem);
		treeNodeMap.put(childItem, childTreeNode);
		return childTreeNode;
	}

	/** Inserts the document node. */
	private MutableTreeNode insertRootNode(IMapItem childItem, MutableTreeNode parentTreeNode) {
		MutableTreeNode treeNode = addTreeNode(childItem, parentTreeNode, true);
		return treeNode;
	}

	/**
	 * Adds the element node as the child of the parent tree node.
	 */
	private MutableTreeNode addItemNode(IMapItem childItem, MutableTreeNode parentTreeNode) {
		// Do not show hidden MapItems if showHiddenMapItems == false
		if (isShowHiddenMapItems() == false && childItem.getName().startsWith("__")) {
			return null;
		}

		MutableTreeNode treeNode = addTreeNode(childItem, parentTreeNode, true);

		// gather up attributes and children nodes
		Set<IMapItem> childItemSet = childItem.getChildSet(false);
		if (childItemSet != null) {
			for (IMapItem item : childItemSet) {
				addItemNode(item, treeNode);
			}
		}
		return treeNode;

	}

	public static class NodePair {
		IMapItem mapItem;
		TreeNode treeNode;

		public NodePair(IMapItem mapItem, TreeNode treeNode) {
			this.mapItem = mapItem;
			this.treeNode = treeNode;
		}
	}

}