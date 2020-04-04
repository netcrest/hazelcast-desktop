/*
 * Copyright (c) 2019 Netcrest Technologies, LLC.
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

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.netcrest.pado.ui.swing.pado.hazelcast.common.IMapItem;

/**
 * Displays a DOM document in a tree control.
 */
public class MapTree extends JTree implements DragGestureListener, DragSourceListener, DropTargetListener
{
	private static final long serialVersionUID = 1L;

	private DropTarget dropTarget;

	//
	// Constructors
	//

	/** Default constructor. */
	public MapTree()
	{
		super(new HazelcastMapTreeModel());
		setEditable(false);

		// set tree properties
		setRootVisible(false);

		DragSource dragSource = DragSource.getDefaultDragSource();

		dragSource.createDefaultDragGestureRecognizer(this, // component where
															// drag originates
				DnDConstants.ACTION_COPY_OR_MOVE, // actions
				this); // drag gesture recognizer
		dropTarget = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true);

		reset();

	} // <init>()

	private IMapItem getItem(Object treeNode)
	{
		IMapItem  item = (IMapItem ) ((HazelcastMapTreeModel) getModel()).getItem(treeNode);
		if (item == null) {
			return null;
		}
		return item;
	}
	
	
	/** Resets the tree. */
	public void reset()
	{
		((HazelcastMapTreeModel) getModel()).reset();
		expandRow(0);
	}

	public void reset(TreeSet<IMapItem> itemSet, boolean showNoHiddenMaps)
	{
		((HazelcastMapTreeModel) getModel()).reset(itemSet, showNoHiddenMaps);
		expandRow(0);
	}

	public String getSelectedNodeName()
	{
		IMapItem item = getSelectedItem();
		if (item == null) {
			return null;
		}
		return item.getFullPath();
	}

	public String getSelectedTreeNodeGridId()
	{
//		DefaultMutableTreeNode treeNode = getSelectedTreeNode();
//		if (treeNode == null) {
//			return null;
//		}
//		TreeNode nodes[] = treeNode.getPath();
//		if (nodes.length <= 2) {
//			return null;
//		}
//
//		// TODO: Make GridInfo and CacheInfo part of the nodes
//		String name = nodes[2].toString();
//		String gridId = name.substring(0, name.indexOf('(')).trim();
//		return gridId;
		return "hazelcast";
	}

	public IMapItem getSelectedItem()
	{
		TreePath path = this.getLeadSelectionPath();
		if (path == null) {
			return null;
		}
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		return getItem(treeNode);
	}

	public DefaultMutableTreeNode getSelectedTreeNode()
	{
		TreePath path = this.getSelectionPath();
		if (path == null) {
			return null;
		}
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		return treeNode;
	}

	public void expandAll()
	{
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

	public void collapseAll()
	{
//		for (int i = 0; i < getRowCount(); i++) {
//			collapseRow(i);
//		}
//		
		for (int i = getRowCount() - 1; i >= 1; i--) {
			collapseRow(i);
		}
	}

	public void dragGestureRecognized2(DragGestureEvent e)
	{
		/*
		 * // drag anything ... e.startDrag(DragSource.DefaultCopyDrop, //
		 * cursor new StringSelection(getSelectedNode()), // transferable this);
		 * // drag source listener
		 */
		JTree tree = (JTree) e.getComponent();
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			// Nothing selected, nothing to drag
			tree.getToolkit().beep();
		} else {
			DefaultMutableTreeNode selection = (DefaultMutableTreeNode) path.getLastPathComponent();

			TransferableTreeNode node = new TransferableTreeNode(selection, TransferableTreeNode.RULE_BEAN_TREE_NODE);
			e.startDrag(DragSource.DefaultCopyNoDrop, node, this);
		}
	}

	public void dragGestureRecognized(DragGestureEvent e)
	{
		/*
		 * // drag anything ... e.startDrag(DragSource.DefaultCopyDrop, //
		 * cursor new StringSelection(getSelectedNode()), // transferable this);
		 * // drag source listener
		 */
		JTree tree = (JTree) e.getComponent();
		TreePath path = tree.getSelectionPath();
		if (path == null) {
			// Nothing selected, nothing to drag
			tree.getToolkit().beep();
		} else {
			DefaultMutableTreeNode selection = (DefaultMutableTreeNode) path.getLastPathComponent();

			TransferableTreeNode node = new TransferableTreeNode(selection, TransferableTreeNode.RULE_BEAN_TREE_NODE);
			e.startDrag(DragSource.DefaultCopyNoDrop, node, this);
		}
	}

	public void dragDropEnd(DragSourceDropEvent e)
	{
	}

	public void dragEnter(DragSourceDragEvent e)
	{
	}

	public void dragExit(DragSourceEvent e)
	{
	}

	public void dragOver(DragSourceDragEvent e)
	{
	}

	public void dropActionChanged(DragSourceDragEvent e)
	{
	}

	public void drop(DropTargetDropEvent e)
	{
		try {
			DataFlavor stringFlavor = DataFlavor.stringFlavor;
			Transferable tr = e.getTransferable();

			if (e.isDataFlavorSupported(stringFlavor)) {
				Point location = e.getLocation();

				TreePath treePath = this.getPathForLocation(location.x, location.y);
				if (treePath == null) {
					e.rejectDrop();
					return;
				}

				if (tr.isDataFlavorSupported(TransferableTreeNode.DEFAULT_MUTABLE_TREE_NODE_FLAVOR)) {
					Object obj = tr.getTransferData(TransferableTreeNode.DEFAULT_MUTABLE_TREE_NODE_FLAVOR);
					if ((obj instanceof NodeData) == false) {
						e.rejectDrop();
						return;
					}
					NodeData nodeData = (NodeData) obj;
					if (nodeData.nodeType == NodeData.MESSAGE_TREE_NODE) {
						if ((nodeData.data instanceof NodeID) == false) {
							e.rejectDrop();
							return;
						}
					} else {
						// if ((nodeData.data instanceof TaskID) == false) {
						// e.rejectDrop();
						// return;
						// }
					}

					e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					e.dropComplete(true);
				} else {
					e.rejectDrop();
					return;
				}
				/*
				 * String nodeName = (String)tr.getTransferData(stringFlavor);
				 * e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				 * this.setValueAt(nodeName, row, col); e.dropComplete(true);
				 */
			} else {
				e.rejectDrop();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (UnsupportedFlavorException ex) {
			ex.printStackTrace();
		}
	}

	public void dragEnter(DropTargetDragEvent e)
	{
	}

	public void dragExit(DropTargetEvent e)
	{
	}

	public void dragOver(DropTargetDragEvent e)
	{
	}

	public void dropActionChanged(DropTargetDragEvent e)
	{
	}
}
