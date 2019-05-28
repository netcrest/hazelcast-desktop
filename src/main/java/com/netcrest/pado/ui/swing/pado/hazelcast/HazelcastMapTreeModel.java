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

public class HazelcastMapTreeModel extends DefaultTreeModel implements Serializable {
	private static final long serialVersionUID = 1L;

	private TreeSet<HazelcastSharedCache.MapItem> itemSet;
	boolean showTemporalOnly = false;
	private boolean showHiddenItems = false;

	/** Node Map. */
	/**
	 * key = TreeNode, value = Region.
	 */
	private HashMap<TreeNode, HazelcastSharedCache.MapItem> itemMap = new HashMap<TreeNode, HazelcastSharedCache.MapItem>();

	/**
	 * key = Region, value = TreeNode.
	 */
	private HashMap<HazelcastSharedCache.MapItem, TreeNode> treeNodeMap = new HashMap<HazelcastSharedCache.MapItem, TreeNode>();

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

	public void reset(TreeSet<HazelcastSharedCache.MapItem> itemSet, boolean showHiddenItems) {
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

	public void setShowHiddenRegions(boolean showHiddenRegions) {
		this.showHiddenItems = showHiddenRegions;
	}

	public boolean isShowHiddenRegions() {
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
	public HazelcastSharedCache.MapItem getItem(Object treeNode) {
		return itemMap.get(treeNode);
	}

	public MutableTreeNode getTreeNode(HazelcastSharedCache.MapItem region) {
		return (MutableTreeNode) treeNodeMap.get(region);
	}

	/**
	 * Return the first tree node in parent that matches the childNode's node name.
	 * It returns null if not found.
	 */
	public MutableTreeNode findTreeNode(HazelcastSharedCache.MapItem childItem, MutableTreeNode parent) {
		HazelcastSharedCache.MapItem parentItem = getItem(parent);
		if (parentItem != null) {
			TreeSet<HazelcastSharedCache.MapItem> parentRegionSet = parentItem.getChildSet(false);
			for (HazelcastSharedCache.MapItem item : parentRegionSet) {
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
		HazelcastSharedCache.MapItem rootItem = itemSet.first();
		TreeSet<HazelcastSharedCache.MapItem> rootItemSet = rootItem.getChildSet(false);
		MutableTreeNode rootTreeNode = insertRootNode(rootItem, root);

		for (HazelcastSharedCache.MapItem item : rootItemSet) {
			addItemNode(item, rootTreeNode);
		}

	} // buildTree()

	private MutableTreeNode insertNodeOnly(HazelcastSharedCache.MapItem childRegion, MutableTreeNode parentTreeNode) {
		// If there is a node in parent which has the same node name
		// as childNode then return that node's tree node.
		MutableTreeNode treeNode = findTreeNode(childRegion, parentTreeNode);
		if (treeNode != null) {
			return treeNode;
		}

		// Did not find a matching node. Let's insert childNode.
		treeNode = addTreeNode(childRegion, parentTreeNode, true);
		treeNodeMap.put(childRegion, treeNode);
		return treeNode;
	}

	/**
	 * @param treeOnly If true, adds childNode to the tree only; otherwise, adds
	 *                 childNode to the tree and the parent XML node.
	 */
	private MutableTreeNode addChildNode(HazelcastSharedCache.MapItem childRegion, MutableTreeNode parentTreeNode,
			boolean treeOnly) {
		MutableTreeNode root = (MutableTreeNode) getRoot();
		MutableTreeNode newTreeNode = null;
		if (childRegion == null) {
			newTreeNode = insertRootNode(childRegion, root);
		} else {
			newTreeNode = addItemNode(childRegion, parentTreeNode);
		}
		return newTreeNode;
	}

	/**
	 * Adds childNode to both the parent tree node and parent XML node.
	 * 
	 * @return Returns the newly created tree node that represents childNode.
	 */
	public MutableTreeNode addChildNode(HazelcastSharedCache.MapItem childRegion, MutableTreeNode parentTreeNode) {
		return addChildNode(childRegion, parentTreeNode, false);
	}

	/**
	 * Recursively adds node and treeNode to the maps. It assumes node and treeNode
	 * are fully associated such that their children can be mapped one to one.
	 */
	private void addNodeToMaps(HazelcastSharedCache.MapItem item, MutableTreeNode treeNode) {
		itemMap.put(treeNode, item);
		treeNodeMap.put(item, treeNode);
		Set<HazelcastSharedCache.MapItem> regionSet = item.getChildSet(false);
		int i = 0;
		for (HazelcastSharedCache.MapItem region2 : regionSet) {
			MutableTreeNode childTreeNode = (MutableTreeNode) treeNode.getChildAt(i++);
			addNodeToMaps(region2, childTreeNode);
		}
	}

	/**
	 * Removes the specified node. It returns the corresponding tree node.
	 * 
	 * @param node The node to remove.
	 */
	private void removeNode(HazelcastSharedCache.MapItem region, ArrayList nodeArray) {
		if (region == null) {
			return;
		}
		MutableTreeNode treeNode = (MutableTreeNode) treeNodeMap.get(region);
		if (treeNode == null) {
			return;
		}
		this.removeNodeFromParent(treeNode);
		removeNodeFromMaps(region, nodeArray);
	}

	public void removeNode(HazelcastSharedCache.MapItem region) {
		removeNode(region, null);
	}

	public ArrayList removeNodeAndGetPairs(HazelcastSharedCache.MapItem item) {
		ArrayList nodeArray = new ArrayList(14);
		removeNode(item, nodeArray);
		return nodeArray;
	}

	public HazelcastSharedCache.MapItem getItem(TreeNode treeNode) {
		return itemMap.get(treeNode);
	}

	/**
	 * Removes the specified node from the nodeMap and treeNodeMap. It recusively
	 * removes all of the children.
	 */
	private void removeNodeFromMaps(HazelcastSharedCache.MapItem item, ArrayList nodeArray) {
		TreeNode treeNode = (TreeNode) treeNodeMap.get(item);
		treeNodeMap.remove(item);
		itemMap.remove(treeNode);
		if (nodeArray != null) {
			nodeArray.add(new NodePair(item, treeNode));
		}
		Set<HazelcastSharedCache.MapItem> itemSet = item.getChildSet(false);
		for (HazelcastSharedCache.MapItem item2 : itemSet) {
			removeNodeFromMaps(item2, nodeArray);
		}
	}

	private boolean hasChildNode(HazelcastSharedCache.MapItem childItem, HazelcastSharedCache.MapItem parentItem) {
		if (childItem == null || parentItem == null) {
			return false;
		}
		Set<HazelcastSharedCache.MapItem> regionSet = parentItem.getChildSet(false);

		for (HazelcastSharedCache.MapItem region : regionSet) {
			if (region == childItem) {
				return true;
			}
		}
		return false;
	}

	private String getItemNameForTree(HazelcastSharedCache.MapItem region) {
		if (region == null) {
			return "/"; // root node
		}

		return region.getName();
	}

	private MutableTreeNode insertBeforeTreeNode(HazelcastSharedCache.MapItem childRegion,
			HazelcastSharedCache.MapItem refChildRegion, MutableTreeNode parentChildTreeNode, boolean treeOnly) {
		String childTreeNodeName = getItemNameForTree(childRegion);
		MutableTreeNode childTreeNode = new DefaultMutableTreeNode(childTreeNodeName);
		MutableTreeNode refChildTreeNode = (MutableTreeNode) treeNodeMap.get(refChildRegion);
		insertNodeInto(childTreeNode, parentChildTreeNode, parentChildTreeNode.getIndex(refChildTreeNode));
		itemMap.put(childTreeNode, childRegion);
		treeNodeMap.put(childRegion, childTreeNode);
		return childTreeNode;
	}

	/**
	 * Inserts node before refTreeNode. If node is Attr then it is appended as
	 * refTreeNode's attribute.
	 */
	private MutableTreeNode insertBeforeTreeNode(HazelcastSharedCache.MapItem childItem,
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
	private MutableTreeNode addTreeNode(HazelcastSharedCache.MapItem childRegion, MutableTreeNode parentTreeNode,
			boolean treeOnly) {
		String childTreeNodeName = getItemNameForTree(childRegion);
		MutableTreeNode childTreeNode = new DefaultMutableTreeNode(childTreeNodeName);
		insertNodeInto(childTreeNode, parentTreeNode, parentTreeNode.getChildCount());
		itemMap.put(childTreeNode, childRegion);
		treeNodeMap.put(childRegion, childTreeNode);
		return childTreeNode;
	}

	/** Inserts the document node. */
	private MutableTreeNode insertRootNode(HazelcastSharedCache.MapItem childRegion, MutableTreeNode parentTreeNode) {
		MutableTreeNode treeNode = addTreeNode(childRegion, parentTreeNode, true);
		return treeNode;
	}

	/**
	 * Adds the element node as the child of the parent tree node.
	 */
	private MutableTreeNode addItemNode(HazelcastSharedCache.MapItem childItem, MutableTreeNode parentTreeNode) {
		// Do not show hidden regions if showHiddenRegions == false
		if (isShowHiddenRegions() == false && childItem.getName().startsWith("__")) {
			return null;
		}

		MutableTreeNode treeNode = addTreeNode(childItem, parentTreeNode, true);

		// gather up attributes and children nodes
		Set<HazelcastSharedCache.MapItem> childItemSet = childItem.getChildSet(false);
		if (childItemSet != null) {
			for (HazelcastSharedCache.MapItem item : childItemSet) {
				addItemNode(item, treeNode);
			}
		}
		return treeNode;

	}

	public static class NodePair {
		HazelcastSharedCache.MapItem region;
		TreeNode treeNode;

		public NodePair(HazelcastSharedCache.MapItem region, TreeNode treeNode) {
			this.region = region;
			this.treeNode = treeNode;
		}
	}

}