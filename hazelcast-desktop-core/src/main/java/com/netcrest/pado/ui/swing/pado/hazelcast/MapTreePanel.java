package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import com.netcrest.pado.ui.swing.pado.hazelcast.common.HazelcastSharedCache;
import com.netcrest.pado.ui.swing.pado.hazelcast.common.IMapItem;
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

	private JCheckBox chckbxShowHidden;
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
		gbl_checkBoxPanel.columnWidths = new int[] { 69, 0 };
		gbl_checkBoxPanel.rowHeights = new int[] { 23, 0 };
		gbl_checkBoxPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_checkBoxPanel.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
		checkBoxPanel.setLayout(gbl_checkBoxPanel);

		chckbxShowHidden = new JCheckBox("Show Hidden");
		chckbxShowHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				refresh();
			}
		});
		GridBagConstraints gbc_chckbxShowHidden = new GridBagConstraints();
		gbc_chckbxShowHidden.anchor = GridBagConstraints.NORTHWEST;
		gbc_chckbxShowHidden.gridx = 0;
		gbc_chckbxShowHidden.gridy = 0;
		checkBoxPanel.add(chckbxShowHidden, gbc_chckbxShowHidden);

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
		IMapItem item = ((HazelcastMapTreeModel) physicalPathInfoTree.getModel()).getItem(treeNode);
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
		boolean showNoHiddenPaths = chckbxShowHidden.isSelected();
		HazelcastSharedCache.getSharedCache().refresh();
		TreeSet<IMapItem> itemSet = HazelcastSharedCache.getSharedCache().getMapSet();
		itemSet = new TreeSet<IMapItem>(itemSet);
		physicalPathInfoTree.reset(itemSet, showNoHiddenPaths);
	}

	public void clearTreeSelection()
	{
		physicalPathInfoTree.clearSelection();
	}

	public IMapItem getSelectedItem()
	{
		return physicalPathInfoTree.getSelectedItem();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		HashMap map = new HashMap(2);
		map.put("showHidden", chckbxShowHidden.isSelected());
		out.writeObject(map);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		HashMap map = (HashMap) in.readObject();
		Boolean showHidden = (Boolean)map.get("showHidden");
		showHidden = showHidden != null && showHidden;
		chckbxShowHidden.setSelected(showHidden);
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
