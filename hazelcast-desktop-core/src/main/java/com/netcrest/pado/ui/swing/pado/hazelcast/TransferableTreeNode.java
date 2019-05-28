package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.datatransfer.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;
import java.io.*;
import javax.swing.*;
import javax.swing.tree.*;

public class TransferableTreeNode implements Transferable
{
  final static int TREE = 0;
  final static int STRING = 1;
  final static int PLAIN_TEXT = 2;

  public final static int MESSAGE_TREE_NODE = 0;
  public final static int RULE_BEAN_TREE_NODE = 1;

  public final static DataFlavor DEFAULT_MUTABLE_TREE_NODE_FLAVOR =
        new DataFlavor(DefaultMutableTreeNode.class, "Default Mutable Tree Node");

  static DataFlavor flavors[] = {DEFAULT_MUTABLE_TREE_NODE_FLAVOR,
                    DataFlavor.stringFlavor,
                    DataFlavor.plainTextFlavor};

  private DefaultMutableTreeNode data;
  private NodeID        nodeID;
  private int treeNodeType;

  public TransferableTreeNode(DefaultMutableTreeNode data, int treeNodeType)
  {
    this.data = data;
    this.treeNodeType = treeNodeType;
  }

  public TransferableTreeNode(NodeID data, int treeNodeType)
  {
    this.nodeID = data;
    this.treeNodeType = treeNodeType;
  }

  public DataFlavor[] getTransferDataFlavors()
  {
    return flavors;
  }

  public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException
  {
    Object retObject;

    if (flavor.equals(flavors[TREE])) {

        //todo -- hack!//
        Object userObject = null;
        if (data != null) {
            userObject = data.getUserObject();
        } else {
            userObject = nodeID;
        }
        if (userObject == null) {
            retObject = new NodeData(treeNodeType, data);
        } else {
            retObject = new NodeData(treeNodeType, userObject);
        }
    } else if (flavor.equals(flavors[STRING])) {
        Object userObject = data.getUserObject();
        if (userObject == null) {
            retObject = data.toString();
        } else {
            retObject = userObject.toString();
        }
    } else if (flavor.equals(flavors[PLAIN_TEXT])) {
        Object userObject = data.getUserObject();
        String str;
        if (userObject == null) {
            str = data.toString();
        } else {
            str = userObject.toString();
        }
        retObject = new ByteArrayInputStream(str.getBytes("Unicode"));
    } else {
        throw new UnsupportedFlavorException(flavor);
    }
    return retObject;
  }

  public boolean isDataFlavorSupported(DataFlavor flavor)
  {
    for (int i = 0, n = flavors.length; i < n; i++) {
        if (flavor.equals(flavors[i])) {
            return true;
        }
    }
    return false;
  }
}