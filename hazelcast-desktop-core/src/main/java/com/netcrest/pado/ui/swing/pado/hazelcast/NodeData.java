package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.io.*;

public class NodeData implements Serializable
{
  private static final long serialVersionUID = 1001L;

  public final static int MESSAGE_TREE_NODE = TransferableTreeNode.MESSAGE_TREE_NODE;
  public final static int RULE_BEAN_TREE_NODE = TransferableTreeNode.RULE_BEAN_TREE_NODE;

  int nodeType;
  Object data;

  public NodeData(int nodeType, Object data)
  {
    this.nodeType = nodeType;
    this.data = data;
  }
}