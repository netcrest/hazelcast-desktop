package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.hazelcast.addon.hql.HqlQuery;

import com.netcrest.commandspace.CommandDescriptor;
import com.netcrest.commandspace.CommandPost;
import com.netcrest.commandspace.ICommandProvider;
import com.netcrest.commandspace.ReturnDataObject;
import com.netcrest.pado.data.KeyMap;
import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.data.KeyTypeManager;
import com.netcrest.pado.index.service.IScrollableResultSet;
import com.netcrest.pado.info.PathInfo;
import com.netcrest.pado.info.ServerInfo;
import com.netcrest.pado.internal.util.StringUtil;
import com.netcrest.pado.ui.swing.GridFrame;
import com.netcrest.pado.ui.swing.beans.ButtonTabComponentAdd;
import com.netcrest.pado.ui.swing.beans.ButtonTabComponentRemove;
import com.netcrest.pado.ui.swing.pado.gemfire.LuceneInputPanel;
import com.netcrest.pado.ui.swing.pado.gemfire.OqlInputPanel;
import com.netcrest.pado.ui.swing.pado.gemfire.PadoInfoExplorer;
import com.netcrest.pado.ui.swing.pado.gemfire.ServerInputPanel;
import com.netcrest.pado.ui.swing.pado.gemfire.info.BucketDisplayInfo;
import com.netcrest.pado.ui.swing.pado.hazelcast.GridTableModel.ResultType;
import com.netcrest.pado.ui.swing.pado.hazelcast.info.ItemSelectionInfo;
import com.netcrest.pado.ui.swing.pado.hazelcast.query.QueryResultSet;
import com.netcrest.pado.ui.swing.table.ObjectTreeFrame;
import com.netcrest.pado.ui.swing.table.ObjectTreePanel;
import com.netcrest.ui.swing.table.RowNumberTable;
import com.netcrest.ui.swing.util.RowHeaderTable;
import com.netcrest.ui.swing.util.SwingUtil;
import com.netcrest.ui.swing.util.TableSorter;

/**
 * @author dpark
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked", "static-access" })
public class PageTablePanel extends JPanel implements Externalizable {
	private static final long serialVersionUID = 1L;

	private static CommandDescriptor commandDescriptors[] = {
			new CommandDescriptor(ICommandNames.CS_PADO_INFO.COMMAND_onMapItem,
					"Listens on IMap notifications and refreshes itself.") };

	private PadoInfoExplorer explorer;
	private HashMap<String, ResultPanel> resultPanelMap = new HashMap(20);
	private int processedRow = -1;

	private JTabbedPane tpane;

	private ObjectTreeFrame objectTreeFrame = null;
	private QueryControlPanel queryControlPanel;

	private JSplitPane mainSplitPane;
	private JSplitPane splitPane;
	private OqlInputPanel oqlInputPanel;
	private LuceneInputPanel luceneInputPanel;
	private ServerInputPanel serverInputPanel;
	private JPanel resultPanel;
	private JPanel topControlPanel;
	private JPanel mainPanel;
	private JTabbedPane bottomTabbedPane;
	private ObjectTreePanel objectTreePanel;
	private JPanel inputPanel;
	private JTabbedPane inputTabbedPane;

	private CommandPost commandPost = new CommandPost();

	public PageTablePanel() {
		preInit();
		setLayout(new BorderLayout(0, 0));
		initAllUI();
		setTabComponentAdd(0);
	}

	protected void preInit() {
		PageTablePanelCommandProvider commandProvider = new PageTablePanelCommandProvider();
		commandPost.setInternTopicEnabled(true);
		commandPost.addCommandProvider(commandProvider);
		commandPost.addCommandProvider(ICommandNames.CS_PADO_INFO.TOPIC, commandProvider);
	}

	private void initOqlUI() {
		oqlInputPanel = new OqlInputPanel();
		inputTabbedPane.addTab("HQL", null, oqlInputPanel, null);
		oqlInputPanel.setMinimumSize(new Dimension(0, 0));
		oqlInputPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String queryString = e.getActionCommand();
					String resultName = getOqlResultName("hazelcast", queryString);
					executeWorkerQuery(resultName, "hazelcast", queryString);
				} catch (Throwable th) {
					SwingUtil.showErrorMessageDialog(PageTablePanel.this, th);
				}
			}
		});
	}

	private void initLuceneUI() {
		luceneInputPanel = new LuceneInputPanel();
		luceneInputPanel.setMinimumSize(new Dimension(0, 0));
		luceneInputPanel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String gridId = luceneInputPanel.getSelectedGridId();
				String regionPath = luceneInputPanel.getSelectedFullPath();
				String queryString = e.getActionCommand();
				String resultName = getLucentResultName(regionPath, queryString);
				executeWorkerQuery(resultName, gridId, queryString);
			}
		});
		// inputTabbedPane.addTab("Lucene", null, luceneInputPanel, null);
	}

	protected void initMainUI() {
		mainPanel = new JPanel();
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new BorderLayout(0, 0));

		mainSplitPane = new JSplitPane();
		mainSplitPane.setOneTouchExpandable(true);
		mainSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		mainPanel.add(mainSplitPane, BorderLayout.CENTER);
		splitPane = new JSplitPane();
		splitPane.setResizeWeight(1.0);
		inputPanel = new JPanel();
		inputPanel.setMinimumSize(new Dimension(0, 0));
		inputPanel.setLayout(new BorderLayout(0, 0));

		inputTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		inputTabbedPane.setMinimumSize(new Dimension(0, 0));
		inputPanel.add(inputTabbedPane, BorderLayout.CENTER);

		mainSplitPane.setLeftComponent(inputPanel);
		mainSplitPane.setRightComponent(splitPane);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		bottomTabbedPane = new JTabbedPane(JTabbedPane.TOP);
		bottomTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				bottomTabbedPaneChanged();
			}
		});
		bottomTabbedPane.setMinimumSize(new Dimension(0, 0));
		objectTreePanel = new ObjectTreePanel();
		bottomTabbedPane.addTab("Value", null, objectTreePanel, null);

		splitPane.setRightComponent(bottomTabbedPane);

		resultPanel = new JPanel();
		resultPanel.setMinimumSize(new Dimension(0, 0));
		splitPane.setLeftComponent(resultPanel);
		// add(centerPanel, BorderLayout.CENTER);
		resultPanel.setLayout(new BorderLayout(0, 0));
		tpane = new JTabbedPane();
		tpane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				updateSatellitePanels();
			}
		});
		resultPanel.add(tpane, BorderLayout.CENTER);
		tpane.add("", new JLabel(""));

		topControlPanel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) topControlPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		resultPanel.add(topControlPanel, BorderLayout.NORTH);

		queryControlPanel = new QueryControlPanel();
		queryControlPanel.addKeyTypeComboBoxItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					keyTypeSelected((QueryControlPanel.KeyTypeItem) e.getItem());
				}
			}
		});
		queryControlPanel.addPageSpinnerActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				page(queryControlPanel.getPage());
			}
		});
		queryControlPanel.addRefreshButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		topControlPanel.add(queryControlPanel);
		GridBagLayout gridBagLayout = (GridBagLayout) queryControlPanel.getLayout();
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		queryControlPanel.addPlayButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				play();
			}
		});
		queryControlPanel.addNextButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextPage();
			}
		});
		queryControlPanel.addPreviousButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				previousPage();
			}
		});
		queryControlPanel.addLastButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lastPage();
			}
		});
		queryControlPanel.addFirstButtonActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				firstPage();
			}
		});
		queryControlPanel.setBorder(new EmptyBorder(2, 2, 0, 2));
		splitPane.setDividerLocation(200);
		mainSplitPane.setDividerLocation(240);
	}

	private void initAllUI() {
		initMainUI();
		initOqlUI();
		// No Lucene. Use TemporalUI instead.
		initLuceneUI();
	}

	private ResultPanel getSelectedResultPanel() {
		Component component = tpane.getSelectedComponent();
		if (component instanceof ResultPanel == false) {
			return null;
		}
		return (ResultPanel) component;
	}

	private PageTableModel getSelectedTableModel() {
		ResultPanel resultPanel = getSelectedResultPanel();
		if (resultPanel == null) {
			return null;
		}
		return resultPanel.getTableModel();
	}

	private ResultPanel getResultPanel(String resultName) {
		return resultPanelMap.get(resultName);
	}

	private void setTabComponentRemove(int index) {
		tpane.setTabComponentAt(index, new ButtonTabComponentRemove(tpane));
	}

	protected void setTabComponentAdd(int index) {
		tpane.setTabComponentAt(index, new ButtonTabComponentAdd(tpane));
	}

	private String getLucentResultName(String regionPath, String queryString) {
		return "lucene:/" + regionPath + "/" + queryString;
	}

	private String getOqlResultName(String gridId, String queryString) {
		return "hql://" + gridId + "/" + queryString;
	}

	private void reset() {
		processedRow = -1;
	}

	private void rowSelected(RowHeaderTable table, boolean forceUpdate) {
		if (table == null) {
			return;
		}
		int row = table.getSelectedRow();
		if (row < 0 || row >= table.getRowCount() || (!forceUpdate && row == processedRow)) {
			return;
		}
		processedRow = row;

		TableSorter model = (TableSorter) table.getModel();
		PageTableModel tableModel = ((PageTableModel) model.getTableModel());
		Object value = tableModel.getUserData(model.modelIndex(row));
		setObjectValue(value);
	}

	private void bottomTabbedPaneChanged() {
		// Temporal list
		if (bottomTabbedPane.getSelectedIndex() == 1) {
			if (getSelectedResultPanel() != null) {
				rowSelected(getSelectedResultPanel().getTable(), true);
			}
		}
	}

	private void setObjectValue(Object value) {
		objectTreePanel.setObject(value);
		objectTreePanel.expandRow(1);
	}

	private void openObjectTreeTable(Object object) {
		if (object == null) {
			return;
		}
		if (objectTreeFrame == null) {
			objectTreeFrame = new ObjectTreeFrame();
			objectTreeFrame.setLocationRelativeTo((Frame) SwingUtilities.getAncestorOfClass(Frame.class, this));
			// SwingUtil.centerFrame((Frame)
			// SwingUtilities.getAncestorOfClass(Frame.class, this),
			// objectTreeFrame);
		}
		objectTreeFrame.setObject(object);
		objectTreeFrame.setState(Frame.NORMAL);
		objectTreeFrame.setVisible(true);
	}

	private void firstPage() {
		queryControlPanel.setPage(1);
		pageChanged();
	}

	private void lastPage() {
		PageTableModel tableModel = getSelectedTableModel();
		if (tableModel == null) {
			return;
		}
		queryControlPanel.setPage(((QueryResultSet)tableModel.getResultSet()).getLargestPageVisted());
		pageChanged();
	}

	private void previousPage() {
		int page = queryControlPanel.getPage() - 1;
		page(page);
	}

	private void nextPage() {
		int page = queryControlPanel.getPage() + 1;
		page(page);
		PageTableModel tableModel = getSelectedTableModel();
		if (tableModel == null) {
			return;
		}
		if (((QueryResultSet)tableModel.getResultSet()).isLastPage()) {
			queryControlPanel.setMaximumPage(((QueryResultSet)tableModel.getResultSet()).getLargestPageVisted());
		}
	}

	private void page(int page) {
		try {
			PageTableModel tableModel = getSelectedTableModel();
			if (tableModel == null) {
				return;
			}
			// if the fetch size has been changed then refresh (start from the beginning)
			if (tableModel.getResultSet() != null && tableModel.getResultSet().getFetchSize() != queryControlPanel.getFetchSize()) {
				refresh();
				return;
			}
			boolean pageChanged = tableModel.page(page, queryControlPanel.getFetchSize());
			ResultPanel resultPanel = getSelectedResultPanel();
			resultPanel.table.resetColumnCellRenderers();
			resultPanel.rowTable.setFirstRowNumber(tableModel.getFirstRowNumber());
			if (pageChanged) {
				queryControlPanel.setPage(tableModel.getPage());
			}
//			queryControlPanel.setMaximumPage(tableModel.getPageCount());
		} catch (Exception ex) {
			SwingUtil.showErrorMessageDialog(this, ex);
		}
	}

	private void pageChanged() {
		page(queryControlPanel.getPage());
	}

	private void play() {
		try {
			if (inputTabbedPane.getSelectedComponent() == oqlInputPanel) {
				String queryString = oqlInputPanel.getQuery();
				String gridId = oqlInputPanel.getSelectedGridId();
				executeWorkerQuery(getOqlResultName(gridId, queryString), gridId, queryString);
			} else if (inputTabbedPane.getSelectedComponent() == luceneInputPanel) {
				String regionPath = luceneInputPanel.getSelectedFullPath();
				String queryString = luceneInputPanel.getQuery();
				executeWorkerQuery(getLucentResultName(regionPath, queryString), luceneInputPanel.getSelectedGridId(),
						queryString);
			}

		} catch (Exception ex) {
			SwingUtil.showErrorMessageDialog(this, ex);
		}
	}

	private void refresh() {
		queryControlPanel.resetMaximumPage();
		ResultPanel resultPanel = getSelectedResultPanel();
		if (resultPanel == null) {
			return;
		}
		resultPanel.refresh();
	}

	private void keyTypeSelected(QueryControlPanel.KeyTypeItem keyTypeItem) {
		PageTableModel tableModel = getSelectedTableModel();
		if (tableModel == null) {
			return;
		}
		IScrollableResultSet resultSet = tableModel.getResultSet();
		try {
			tableModel.reset(resultSet.getStartIndex() + resultSet.getViewStartIndex() + 1, resultSet, null,
					keyTypeItem.getKeyType());
		} catch (Exception ex) {
			SwingUtil.showErrorMessageDialog(this, ex);
		}
	}

	private IScrollableResultSet executeOql(String resultName, String gridId, String queryString) {
		return new QueryResultSet(queryString, queryControlPanel.getFetchSize());
	}

	public void clear() {
		tpane.removeAll();
		tpane.add(new JLabel());
		setTabComponentAdd(0);
		updateSatellitePanels();
		resultPanelMap.clear();
	}

	private void updateObjectTreePanel() {
		ResultPanel resultPanel = getSelectedResultPanel();
		if (resultPanel != null) {
			RowHeaderTable table = resultPanel.getTable();
			if (table != null) {
				int row = table.getSelectedRow();
				if (row != -1) {
					rowSelected(table, false);
					return;
				}
			}
		}
		objectTreePanel.clear();
	}

	public void executeWorkerGridPath(String resultName, String gridId, String gridPath) {
		try {
			if (gridPath == null) {
				return;
			}

			ResultPanel resultPanel = getResultPanel(resultName);
			boolean newPanel = resultPanel == null;
			if (newPanel) {
				resultPanel = new ResultPanel(resultName, gridId, gridPath);
				resultPanelMap.put(resultName, resultPanel);
			}
			// if the panel already exists then select its tab and return
			// JTabbedPane allows only one same component.
			int index = tpane.getSelectedIndex();
			if (tpane.indexOfComponent(resultPanel) != -1) {
				tpane.setSelectedIndex(tpane.indexOfComponent(resultPanel));
				return;
			}

			setResultPanelAt(resultName, resultPanel, index);

		} catch (Exception ex) {
			SwingUtil.showErrorMessageDialog(this, ex);
		}
	}

	public void executeWorkerQuery(String resultName, String gridId, String queryString) {
		try {
			ResultPanel resultPanel = getResultPanel(resultName);
			boolean newPanel = resultPanel == null;
			if (newPanel) {
				if (inputTabbedPane.getSelectedComponent() == oqlInputPanel) {
					resultPanel = new ResultPanel(resultName, gridId, queryString);
				} else {
					// lucene
					String gridPath = luceneInputPanel.getSelectedFullPath();
					resultPanel = new ResultPanel(resultName, gridId, gridPath, queryString);
				}
				resultPanelMap.put(resultName, resultPanel);
			} else {
				if (inputTabbedPane.getSelectedComponent() == oqlInputPanel) {
					// oql
					resultPanel.setType(ResultPanel.OQL);
					resultPanel.setQueryString(queryString);
				} else {
					// lucene
					resultPanel.setType(ResultPanel.LUCENE);
					resultPanel.setQueryString(queryString);
					resultPanel.setRegionPath(luceneInputPanel.getSelectedFullPath());
				}
				resultPanel.refresh();
			}

			// unselect tree node
			if (getPadoInfoExplorer() != null) {
				getPadoInfoExplorer().firePropertyChange("querySelected", false, true);
			}

			// if the panel already exists then select its tab and return
			// JTabbedPane allows only one same component.
			int index = tpane.getSelectedIndex();
			if (tpane.indexOfComponent(resultPanel) != -1) {
				tpane.setSelectedIndex(tpane.indexOfComponent(resultPanel));
				return;
			}

			setResultPanelAt(resultName, resultPanel, index);

		} catch (Exception ex) {
			SwingUtil.showErrorMessageDialog(this, ex);
		}
	}

	public PadoInfoExplorer getPadoInfoExplorer() {
		if (explorer == null) {
			explorer = (PadoInfoExplorer) SwingUtilities.getAncestorOfClass(PadoInfoExplorer.class, this);
		}
		return explorer;
	}

	private void setResultPanelAt(String resultName, ResultPanel resultPanel, int index) {
		if (index >= 0) {
			tpane.remove(index);
		} else {
			index = 0;
		}

		reset();
		tpane.insertTab(resultName, null, resultPanel, null, index);
		setTabComponentRemove(index);

		// Add "+" tab
		// if (tpane.getTabCount() == 1) {
		// tpane.add("", new JLabel(""));
		// setTabComponentAdd(1);
		// }

		// workaround to the tab "+" button
		int lastIndex = tpane.getTabCount() - 1;
		Component comp = tpane.getComponentAt(lastIndex);
		if (comp instanceof JLabel == false) {
			tpane.add("", new JLabel(""));
			setTabComponentAdd(lastIndex + 1);
		}

		tpane.setSelectedIndex(index);
		updateSatellitePanels();

		this.validate();
	}

	private void updateSatellitePanels() {
		updateObjectTreePanel();
		updateControlPanel();
	}

	private void updateControlPanel() {
		ResultPanel resultPanel = getSelectedResultPanel();
		if (resultPanel == null) {
			if (queryControlPanel != null) {
				queryControlPanel.setTotalSize(0);
				queryControlPanel.setPage(1);
//				queryControlPanel.setMaximumPage(1);
			}
			return;
		}
		IScrollableResultSet rs = resultPanel.getResultSet();
		PageTableModel tableModel = resultPanel.getTableModel();
		if (rs == null) {
			if (tableModel == null) {
				queryControlPanel.setTotalSize(0);
				queryControlPanel.setPage(1);
//				queryControlPanel.setMaximumPage(1);
			} else {
				queryControlPanel.setTotalSize(tableModel.getRowCount());
				queryControlPanel.setPage(1);
//				queryControlPanel.setMaximumPage(1);
			}
			return;
		}
		queryControlPanel.setFetchSize(rs.getFetchSize());
		queryControlPanel.setTotalSize(rs.getTotalSize());
		queryControlPanel.setPage(rs.getSetNumber());
//		queryControlPanel.setMaximumPage(rs.getSetCount());

		KeyType[] keyTypes = null;
		if (tableModel != null) {
			if (tableModel.isKeyMap()) {
				Class[] classes = tableModel.getKeyTypeClasses();
				if (classes.length > 0) {
					keyTypes = KeyTypeManager.getAllRegisteredVersions(classes[0]);
				}
			}
		}
		queryControlPanel.setKeyTypes(keyTypes);
		queryControlPanel.setKeyTypeComboBoxVisible(tableModel != null && tableModel.isKeyMap());
	}

	private String getResultName(String gridId, HazelcastSharedCache.MapItem item) {
		if (item == null) {
			return "";
		} else if (gridId != null) {
			return "path://" + gridId + item.getFullPath();
		} else {
			return "path://" + item.getFullPath();
		}
	}

	class RemoteWorker extends SwingWorker<IScrollableResultSet, String> {
		String resultName;
		String gridId;
		String queryString;
		String args[];

		RemoteWorker(String resultName, String gridId, String queryString) {
			this.resultName = resultName;
			this.gridId = gridId;
			this.queryString = queryString;
		}

		protected IScrollableResultSet doInBackground() throws Exception {
			publish("Retrieving data...");
			return executeOql(resultName, gridId, queryString);
		}

		protected void process(List<String> chunks) {
			ResultPanel resultPanel = getResultPanel(resultName);
			if (resultPanel != null) {
				resultPanel.setProgress(chunks.get(0));
			}
		}

		protected void done() {
			try {
				ResultPanel resultPanel = getResultPanel(resultName);
				if (resultPanel == null) {
					return;
				}
				IScrollableResultSet resultSet = get();
				resultPanel.updateTable(resultSet);
				updateSatellitePanels();
				if (resultPanel.table != null) {
					resultPanel.table.resetColumnCellRenderers();
				}
			} catch (Exception ex) {
				SwingUtil.showErrorMessageDialog(PageTablePanel.this, ex);
			}
		}
	}

	public class ResultPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		final static byte REGION = 0;
		final static byte OQL = 1;
		final static byte LUCENE = 2;
		final static byte SERVER = 3;
		final static byte BUCKET_INFO = 4;
		final static byte BUCKET_ID = 5;
		final static byte TEMPORAL = 6;
		final static byte VIRTUAL_PATH = 7;

		private String gridId;
		private PathInfo regionInfo;
		private ServerInfo serverInfo;
		private BucketDisplayInfo bucketDisplayInfo;
		private int routingBucketId;
		private int bucketId;
		private String resultName;
		private String queryString;
		private String gridPath;
		private String fullPath;
		private String virtualPathQueryString;
		private String virtualPath;
		private KeyMap vpd;
		private String[] args;
		private byte type = REGION;
		private Date validAt;
		private Date asOf;

		private RowHeaderTable table;
		private PageTableModel tableModel;
		private JTextPane textPane;
		private JScrollPane scrollPane;
		private RowNumberTable rowTable;

		public ResultPanel(String resultName, String gridId, String queryString) throws Exception {
			this.resultName = resultName;
			this.gridId = gridId;
			this.queryString = queryString;
			HqlQuery hql = HazelcastSharedCache.getSharedCache().getHqlQueryInstance();
			this.gridPath = hql.compile(queryString).getMapName();
			this.type = OQL;

			initUI();
			refresh();
		}

		public ResultPanel(String resultName, String gridId, String regionPath, String queryString) throws Exception {
			this.gridId = gridId;
			this.resultName = resultName;
			this.gridPath = regionPath;
			this.queryString = queryString;

			this.type = LUCENE;

			initUI();
			refresh();
		}

		private void initUI() {
			setLayout(new BorderLayout(0, 0));
			textPane = new JTextPane();
			textPane.setEditable(false);
			scrollPane = new JScrollPane(textPane);
			add(scrollPane, BorderLayout.CENTER);
		}

		public void refresh() {
			refresh(this.gridId);
		}

		public void refresh(String gridId) {
			this.gridId = gridId;
			if (tableModel != null) {
				tableModel.clearAll();
			}
			queryControlPanel.resetMaximumPage();
			switch (type) {
			case OQL:
				(new RemoteWorker(resultName, gridId, queryString)).execute();
				break;
			case LUCENE:
				break;
			}
		}

		public String getGridId() {
			return gridId;
		}

		public PathInfo getRegionInfo() {
			return regionInfo;
		}

		public String getResultName() {
			return resultName;
		}

		public String getQueryString() {
			return queryString;
		}

		public void setQueryString(String queryString) {
			this.queryString = queryString;
		}

		public void setVirtualPathDefinition(KeyMap vpd) {
			this.vpd = vpd;
		}

		public KeyMap getVirtualPathDefinition() {
			return vpd;
		}

		public void setVirtualPath(String virtualPath) {
			this.virtualPath = virtualPath;
		}

		public String getVirtualPath() {
			return virtualPath;
		}

		public void setFullPath(String fullPath) {
			this.fullPath = fullPath;
		}

		public String getFullPath() {
			return fullPath;
		}

		public void setVirtualPathQueryString(String virtualPathQueryString) {
			this.virtualPathQueryString = virtualPathQueryString;
		}

		public String getVirtualPathQueryString() {
			return virtualPathQueryString;
		}

		public void setArgs(String... args) {
			this.args = args;
		}

		public String[] getArgs() {
			return args;
		}

		public String getRegionPath() {
			return gridPath;
		}

		public void setRegionPath(String regionPath) {
			this.gridPath = regionPath;
		}

		public Date getValidAt() {
			return validAt;
		}

		public void setValidAt(Date validAt) {
			this.validAt = validAt;
		}

		public Date getAsOf() {
			return asOf;
		}

		public void setAsOf(Date asOf) {
			this.asOf = asOf;
		}

		public byte getType() {
			return type;
		}

		public void setType(byte type) {
			this.type = type;
		}

		public void updateTable(IScrollableResultSet resultSet) throws Exception {
			if (resultSet == null || resultSet.getTotalSize() <= 0) {
				if (table != null) {
					table.removeAll();
					scrollPane.removeAll();
					scrollPane = new JScrollPane(textPane);
					removeAll();
					add(scrollPane, BorderLayout.CENTER);
				}
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						textPane.setText("No rows returned.");
					}
				});
				return;
			}

			table = createTable(resultSet, ResultType.KEY_VALUE);
			table.setCellSelectionEnabled(true);
			scrollPane.setViewportView(table);
			rowTable = new RowNumberTable(table);
			scrollPane.setRowHeaderView(rowTable);
			scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowTable.getTableHeader());

			TableSorter model = (TableSorter) table.getModel();
			tableModel = ((PageTableModel) model.getTableModel());

			reset();
		}

		private RowHeaderTable createEmptyTable() {
			PageTableModel model = new PageTableModel();
			RowHeaderTable table = new RowHeaderTable();
			table.setTableSorter(model);
			table.resetColumnCellRenderers();
			return table;
		}

		private RowHeaderTable createTable(IScrollableResultSet resultSet, ResultType resultType) throws Exception {
			if (resultSet == null) {
				return createEmptyTable();
			}
			List objList = resultSet.toList();
			if (objList == null || objList.size() == 0) {
				return createEmptyTable();
			}
			RowHeaderTable table = createObjectTable(resultSet, resultType);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			// copy feature
			ActionMap map = table.getActionMap();
			map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
			final RowHeaderTable table2 = table;
			table.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					if (e.getClickCount() == 2) {
						int row = table2.getSelectedRow();
						if (row >= 0) {
							PageTableModel tableModel = (PageTableModel) table2.getTableModel();
							TableSorter tableSorter = (TableSorter) table2.getModel();
							int modelRow = tableSorter.modelIndex(row);
							Object object = tableModel.getUserData(modelRow);
							openObjectTreeTable(object);
						}
					}
				}
			});
			return table;
		}

		private RowHeaderTable createObjectTable(IScrollableResultSet resultSet, ResultType resultType)
				throws Exception {
			PageTableModel tableModel = new PageTableModel(resultType);
			
			QueryResultSet qrs = (QueryResultSet)resultSet;
			String resultName = qrs.getResultName();
			ResultPanel resultPanel = resultPanelMap.get(resultName);
			tableModel.reset(resultSet.getStartIndex() + +resultSet.getViewStartIndex() + 1, resultSet, null);
			KeyType[] keyTypes = null;
			if (tableModel.isKeyMap()) {
				Class[] classes = tableModel.getKeyTypeClasses();
				if (classes.length > 0) {
					keyTypes = KeyTypeManager.getAllRegisteredVersions(classes[0]);
				}
			}
			queryControlPanel.setKeyTypes(keyTypes);
			queryControlPanel.setKeyTypeComboBoxVisible(tableModel.isKeyMap());
			final RowHeaderTable table = new RowHeaderTable();
			table.setTableSorter(tableModel);
			// table.resetColumnCellRenderers();
			table.addMouseListener(new java.awt.event.MouseAdapter() {

				public void mouseReleased(MouseEvent e) {
					rowSelected(table, false);
				}
			});
			table.addKeyListener(new java.awt.event.KeyAdapter() {

				public void keyReleased(KeyEvent e) {
					rowSelected(table, false);
				}
			});
			table.setDragEnabled(true);
			table.repaint();
			return table;
		}

		public PageTableModel getTableModel() {
			return tableModel;
		}

		public RowHeaderTable getTable() {
			return table;
		}

		public void setProgress(final String progressMessage) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					textPane.setText(progressMessage);
				}
			});
		}

		public IScrollableResultSet getResultSet() {
			if (tableModel == null) {
				return null;
			}
			return tableModel.getResultSet();
		}
	}

	public static void main(String[] args) throws Exception {
		GridFrame.main(new String[] { PageTablePanel.class.getName() });
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		HashMap map = new HashMap(3);
		map.put("mainSplitPane.DividerLocation", mainSplitPane.getDividerLocation());
		map.put("splitPane.DividerLocation", splitPane.getDividerLocation());
		out.writeObject(map);
		oqlInputPanel.writeExternal(out);
		luceneInputPanel.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		HashMap map = (HashMap) in.readObject();
		if (map.containsKey("mainSplitPane.DividerLocation")) {
			mainSplitPane.setDividerLocation((Integer) map.get("mainSplitPane.DividerLocation"));
		}
		if (map.containsKey("splitPane.DividerLocation")) {
			splitPane.setDividerLocation((Integer) map.get("splitPane.DividerLocation"));
		}
		oqlInputPanel.readExternal(in);
		luceneInputPanel.readExternal(in);
	}

	public class PageTablePanelCommandProvider implements ICommandProvider 
	{
		public ReturnDataObject onMapItem(String topic, String sourceInternTopic,
				ItemSelectionInfo itemSelectionInfo) 
		{
			ReturnDataObject retObj = new ReturnDataObject(this, null);
			String resultName = getResultName(itemSelectionInfo.getGridId(), itemSelectionInfo.getItem());
			String query = "select * from " + itemSelectionInfo.getItem().getMapName();
			executeWorkerQuery(resultName, itemSelectionInfo.getGridId(), query);
			return retObj;
		}

		public CommandDescriptor[] getCommandDescriptors() {
			return commandDescriptors;
		}
	}
}
