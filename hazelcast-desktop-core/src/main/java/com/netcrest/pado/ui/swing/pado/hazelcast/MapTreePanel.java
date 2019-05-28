package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.tree.TreeNode;

import com.netcrest.commandspace.CommandDescriptor;
import com.netcrest.commandspace.CommandPost;
import com.netcrest.commandspace.ICommandProvider;
import com.netcrest.commandspace.ReturnDataObject;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.ui.swing.GridFrame;
import com.netcrest.pado.ui.swing.pado.hazelcast.info.ItemSelectionInfo;
import com.netcrest.ui.swing.util.SwingUtil;

@SuppressWarnings({ "static-access", "rawtypes" })
public class MapTreePanel extends JPanel implements Externalizable
{
	private static final long serialVersionUID = 1L;

	private static CommandDescriptor commandDescriptors[] = {
			new CommandDescriptor(ICommandNames.CS_PADO_INFO.COMMAND_onPadoInfo,
					"Fires PadoInfo update notifications to all workspaces upon refresh of locators."),
			new CommandDescriptor(ICommandNames.CS_PADO_INFO.COMMAND_onMapItem,
					"Fires ItemSelectionInfo selection notifications to the active worksheet."),
			new CommandDescriptor(ICommandNames.CS_REFRESH.COMMAND_onRefresh,
					"Listens on refresh notifications and refreshes itself by reconnecting to Pado as necessary.") };

	private static boolean isVirtualPathPanelEnbled = PadoUtil.getBoolean("virtualPathPanel.enabled", false);

	private CommandPost commandPost = new CommandPost();

	private TreeNode currentTreeNode;

	private JCheckBox chckbxReplicated;
	private JCheckBox chckbxNoHidden;
	private JCheckBox chckbxAll;
	private JCheckBox chckbxPartitioned;
	private MapTreeInnerPanel physicalPathTreeInfoInnerPanel;
	private MapTree physicalPathInfoTree;
	private JPanel centerPanel;

	/**
	 * Create the panel.
	 */
	public MapTreePanel()
	{
		preInit();
		initUI();

		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				postInit();
			}
		});
	}

	private void preInit()
	{
		PadoInfoTreePanelCommandProvider commandProvider = new PadoInfoTreePanelCommandProvider();
		commandPost.setInternTopicEnabled(true);
		commandPost.addCommandProvider(commandProvider);
		commandPost.addCommandProvider(ICommandNames.CS_REFRESH.TOPIC, commandProvider);
	}

	private void postInit()
	{
		refresh();
	}

	private void initUI()
	{
		setBorder(new EmptyBorder(2, 2, 2, 2));
		setName(MapTreePanel.class.getSimpleName());
		setLayout(new BorderLayout(0, 4));

		JPanel checkBoxPanel = new JPanel();
		add(checkBoxPanel, BorderLayout.NORTH);
		checkBoxPanel
				.setBorder(new TitledBorder(null, "Path Type", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_checkBoxPanel = new GridBagLayout();
		gbl_checkBoxPanel.columnWidths = new int[] { 0, 69, 0 };
		gbl_checkBoxPanel.rowHeights = new int[] { 23, 0, 0 };
		gbl_checkBoxPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_checkBoxPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		checkBoxPanel.setLayout(gbl_checkBoxPanel);

		chckbxAll = new JCheckBox("All");
		chckbxAll.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				if (chckbxAll.isSelected()) {
					chckbxReplicated.setSelected(false);
					chckbxNoHidden.setSelected(true);
					chckbxPartitioned.setSelected(false);
					refresh();
				}
			}
		});
		GridBagConstraints gbc_chckbxAll = new GridBagConstraints();
		gbc_chckbxAll.anchor = GridBagConstraints.WEST;
		gbc_chckbxAll.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAll.gridx = 0;
		gbc_chckbxAll.gridy = 0;
		checkBoxPanel.add(chckbxAll, gbc_chckbxAll);

		chckbxReplicated = new JCheckBox("Replicated");
		chckbxReplicated.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});

		chckbxNoHidden = new JCheckBox("No Hidden");
		chckbxNoHidden.setSelected(true);
		chckbxNoHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});
		GridBagConstraints gbc_chckbxNoHidden = new GridBagConstraints();
		gbc_chckbxNoHidden.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxNoHidden.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxNoHidden.gridx = 1;
		gbc_chckbxNoHidden.gridy = 0;
		checkBoxPanel.add(chckbxNoHidden, gbc_chckbxNoHidden);
		GridBagConstraints gbc_chckbxReplicated = new GridBagConstraints();
		gbc_chckbxReplicated.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxReplicated.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxReplicated.gridx = 0;
		gbc_chckbxReplicated.gridy = 1;
		checkBoxPanel.add(chckbxReplicated, gbc_chckbxReplicated);

		chckbxPartitioned = new JCheckBox("Partitioned");
		chckbxPartitioned.setSelected(true);
		chckbxPartitioned.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});
		GridBagConstraints gbc_chckbxPartitioned = new GridBagConstraints();
		gbc_chckbxPartitioned.gridx = 1;
		gbc_chckbxPartitioned.gridy = 1;
		checkBoxPanel.add(chckbxPartitioned, gbc_chckbxPartitioned);

		centerPanel = new JPanel();
		add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout(0, 0));

		// JPanel ppTreePanel = new JPanel();
		// ppTreePanel.setBorder(new TitledBorder(null, "Physical Path",
		// TitledBorder.LEADING, TitledBorder.TOP, null,
		// null));
		// splitPane.setLeftComponent(ppTreePanel);
		// ppTreePanel.setLayout(new BorderLayout(0, 0));

		physicalPathTreeInfoInnerPanel = new MapTreeInnerPanel();
		physicalPathTreeInfoInnerPanel.setBorder(new TitledBorder(null, "Physical Path", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		physicalPathTreeInfoInnerPanel.getMapTree().setToolTipText("Click mouse or hit 'Enter' to select");
		physicalPathTreeInfoInnerPanel.getMapTree().addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e)
			{
				nodeSelected();
			}
		});
		physicalPathTreeInfoInnerPanel.getMapTree().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e)
			{
				// If the same tree node is selected then reselect so that
				// the listeners can refresh.
				// if (currentTreeNode ==
				// padoInfoTree.getLastSelectedPathComponent()) {
				nodeSelected();
				// }
			}
		});
		physicalPathTreeInfoInnerPanel.addRefreshActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				SwingUtilities.invokeLater(new Runnable() {
					public void run()
					{
						refreshGrids();
					}
				});

			}
		});
		physicalPathInfoTree = physicalPathTreeInfoInnerPanel.getMapTree();
		// padoInfoTree.addTreeSelectionListener(new TreeSelectionListener() {
		// public void valueChanged(TreeSelectionEvent e)
		// {
		// nodeSelected();
		// }
		// });
		physicalPathInfoTree.setEditable(false);

		centerPanel.add(physicalPathTreeInfoInnerPanel, BorderLayout.CENTER);
	}
	
	private void nodeSelected()
	{
		TreeNode treeNode = (TreeNode) physicalPathInfoTree.getLastSelectedPathComponent();
		HazelcastSharedCache.MapItem item = ((HazelcastMapTreeModel) physicalPathInfoTree.getModel()).getItem(treeNode);
		commandPost.execCommand(ICommandNames.BUS_ACTIVE_WORKSHEET, ICommandNames.CS_PADO_INFO.TOPIC,
				ICommandNames.CS_PADO_INFO.COMMAND_onMapItem,
				new ItemSelectionInfo(physicalPathInfoTree.getSelectedTreeNodeGridId(), item));
	}

	private void refreshGrids()
	{
		try {
			HazelcastSharedCache.getSharedCache().refresh();
			refresh();

//			commandPost.execCommand(ICommandNames.BUS_ALL_WORKSPACES, ICommandNames.CS_PADO_INFO.TOPIC,
//					ICommandNames.CS_PADO_INFO.COMMAND_onPadoInfo, padoInfo);
		} catch (Exception e) {
			SwingUtil.showErrorMessageDialog(this, e);
			// TODO: go back to the previous setting

		}
	}

	private void refresh()
	{
		boolean showAllPaths = chckbxAll.isSelected();
		boolean showNoHiddenPaths = chckbxNoHidden.isSelected();
		HazelcastSharedCache.getSharedCache().refresh();
		TreeSet<HazelcastSharedCache.MapItem> itemSet = HazelcastSharedCache.getSharedCache().getMapSet();
		itemSet = new TreeSet<HazelcastSharedCache.MapItem>(itemSet);
		physicalPathInfoTree.reset(itemSet, showNoHiddenPaths);
	}

	public void clearTreeSelection()
	{
		physicalPathInfoTree.clearSelection();
	}

	public HazelcastSharedCache.MapItem getSelectedItem()
	{
		return physicalPathInfoTree.getSelectedItem();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		HashMap map = new HashMap(2);
		out.writeObject(map);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		HashMap map = (HashMap) in.readObject();
	}

	public class PadoInfoTreePanelCommandProvider implements ICommandProvider
	{
		public ReturnDataObject onRefresh(String topic, String sourceInternTopic, Object dataObject)
		{
			ReturnDataObject retObj = new ReturnDataObject(this, null);
			refreshGrids();
			return retObj;
		}

		public CommandDescriptor[] getCommandDescriptors()
		{
			return commandDescriptors;
		}
	}

	public static void main(String[] args) throws Exception
	{
		GridFrame.main(new String[] { MapTreePanel.class.getName() });
	}
}
