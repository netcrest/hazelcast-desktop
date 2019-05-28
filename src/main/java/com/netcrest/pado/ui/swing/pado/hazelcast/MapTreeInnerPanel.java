package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.netcrest.pado.ui.swing.beans.IconButton;

public class MapTreeInnerPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private MapTree mapTree;
	private JButton refreshButton;
	
	/**
	 * Create the panel.
	 */
	public MapTreeInnerPanel()
	{
		setLayout(new BorderLayout(0, 0));
		mapTree = new MapTree();
		JScrollPane scrollPane = new JScrollPane(mapTree);
		add(scrollPane, BorderLayout.CENTER);
		
		JPanel controlPanel = new JPanel();
		add(controlPanel, BorderLayout.NORTH);
		controlPanel.setLayout(new BorderLayout(10, 0));
		
		JPanel treeControlPanel = new JPanel();
		controlPanel.add(treeControlPanel, BorderLayout.CENTER);
		treeControlPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 2));
		
		IconButton expandAllButton = new IconButton();
		expandAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				expandAll();
			}
		});
		expandAllButton.setToolTipText("Expand all");
		expandAllButton.setIcon(new ImageIcon(MapTreeInnerPanel.class.getResource("/images/grid/expand-medium-gold.png")));
		treeControlPanel.add(expandAllButton);
		
		IconButton collapseAllButton = new IconButton();
		collapseAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				collapseAll();
			}
		});
		collapseAllButton.setToolTipText("Collapse all");
		collapseAllButton.setIcon(new ImageIcon(MapTreeInnerPanel.class.getResource("/images/grid/collapse-medium-gold.png")));
		treeControlPanel.add(collapseAllButton);
		
		refreshButton = new JButton("");
		controlPanel.add(refreshButton, BorderLayout.EAST);
		refreshButton.setIcon(new ImageIcon(MapTreeInnerPanel.class.getResource("/images/Refresh16.gif")));
		refreshButton.setToolTipText("Refresh by contacting the grid");
		refreshButton.setMargin(new Insets(0, 0, 0, 0));
		refreshButton.setIconTextGap(0);

	}
	
	public void addRefreshActionListener(ActionListener actionListener)
	{
		refreshButton.addActionListener(actionListener);
	}
	
	public void removeRefreshActionListener(ActionListener actionListener)
	{
		refreshButton.removeActionListener(actionListener);
	}
	
	public MapTree getMapTree()
	{
		return mapTree;
	}

	public void expandAll()
	{
		mapTree.expandAll();
	}
	
	public void collapseAll()
	{
		mapTree.collapseAll();
	}
}
