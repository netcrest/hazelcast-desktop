package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.io.*;
import java.util.Vector;
import com.netcrest.persist.*;

/**
 * NodeID is used strictly for dnd-transferring a message ID and node ID
 * from DOMTree to RulePanel.
 */
public class NodeID implements Externalizable
{
  private static final long serialVersionUID = 1001L;

  public String msgName;
  public String nodeName;
  public String nodeNames[];
  public int nodeLevel = 0; // 0 is root

  public NodeID()
  {
    this(null, null);
  }

  public NodeID(String msgName, String nodeName)
  {
    this.msgName = msgName;
    this.nodeName = nodeName;
  }

  public NodeID(String nodeNames[])
  {
    this.nodeNames = nodeNames;
    if (nodeNames != null && nodeNames.length > 0) {
        this.msgName = nodeNames[0];
        this.nodeName = nodeNames[nodeNames.length - 1];
    }
    nodeLevel = nodeNames.length - 1;
  }

  public String toString()
  {
    if (nodeNames == null || nodeNames.length == 0) {
        if (nodeName == null) {
            return "";
        }
        return nodeName;
    }
    return nodeNames[nodeNames.length - 1];
  }

  /**
   * Returns the qualified name without the root node name.
   */
  public String getQualifiedName()
  {
    if (nodeNames == null) {
        if (nodeName == null) {
            return "";
        }
        return nodeName;
    }
    String id = "";
    for (int i = 1; i < nodeNames.length; i++) {
        id += nodeNames[i] + ".";
    }
    if (id.endsWith(".")) {
        id = id.substring(0, id.length() - 1);
        return "root." + id;
    }
    return "root";
  }

  public String getFullyQualifiedName2()
  {
    if (nodeNames == null) {
        if (nodeName == null) {
            return "";
        }
        return nodeName;
    }
    String id = "";
    for (int i = 0; i < nodeNames.length; i++) {
        id += nodeNames[i] + ".";
    }
    if (id.endsWith(".")) {
        id = id.substring(0, id.length() - 1);
    }
    return id;
  }

  public void writeExternal(ObjectOutput out) throws IOException
  {
    PersistenceMap map = new PersistenceMap(2);
    if (msgName != null) {
        map.put("msgName", msgName);
    }
    if (nodeName != null) {
        map.put("nodeName", nodeName);
    }
    if (nodeNames != null) {
        map.put("nodeNames", nodeNames);
    }
    out.writeObject(map);
  }

  /**
   * Reads the state properties from the input stream.
   * @param in  The object input stream.
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
  {
    PersistenceMap map = (PersistenceMap)in.readObject();
    msgName = (String)map.get("msgName");
    nodeName = (String)map.get("nodeName");
    nodeNames = (String[])map.get("nodeNames");
    if (nodeNames != null) {
        nodeLevel = nodeNames.length - 1;
    }
  }
}

