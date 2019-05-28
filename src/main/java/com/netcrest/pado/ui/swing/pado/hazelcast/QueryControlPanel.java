package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;

import com.netcrest.pado.data.KeyType;
import com.netcrest.pado.ui.swing.beans.GridLabel;
import com.netcrest.pado.ui.swing.beans.GridSpinner;

public class QueryControlPanel extends JPanel implements Externalizable
{
	private static NumberFormat numberFormat = new DecimalFormat("#,###");
	private GridSpinner fetchSizeSpinner;
	private GridSpinner pageSpinner;
	private JButton prevButton;
	private JButton lastButton;
	private JButton playButton;
	private JButton nextButton;
	private JButton firstButton;
	private GridLabel totalPageCountLabel;
	private GridLabel totalSizeLabel;
	private int totalSize = 0;
	private JButton refreshButton;
	private ArrayList<ActionListener> pageSpinnerActionListenerList = new ArrayList(2);
	private JComboBox keyTypeComboBox;

	/**
	 * Create the panel.
	 */
	public QueryControlPanel()
	{
		setBorder(new EmptyBorder(2, 0, 2, 2));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0, 0, 80, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gridBagLayout.rowHeights = new int[]{0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);
		
		GridLabel grdlblFretchSize = new GridLabel();
		grdlblFretchSize.setText("Fetch Size:");
		GridBagConstraints gbc_grdlblFretchSize = new GridBagConstraints();
		gbc_grdlblFretchSize.anchor = GridBagConstraints.EAST;
		gbc_grdlblFretchSize.insets = new Insets(0, 0, 0, 5);
		gbc_grdlblFretchSize.gridx = 0;
		gbc_grdlblFretchSize.gridy = 0;
		add(grdlblFretchSize, gbc_grdlblFretchSize);
		
		fetchSizeSpinner = new GridSpinner();
//		fetchSizeSpinner.addChangeListener(new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				SpinnerNumberModel model = (SpinnerNumberModel)pageSpinner.getModel();
//				model.setValue(model.getMaximum());
//				setMaximumPage(tableModel.getPageCount());
//			}
//		});
		fetchSizeSpinner.setToolTipText("Enter fetch (page) size");
		fetchSizeSpinner.setModel(new SpinnerNumberModel(100, 1, 1000, 1));
		GridBagConstraints gbc_fetchSizeSpinner = new GridBagConstraints();
		gbc_fetchSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fetchSizeSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_fetchSizeSpinner.gridx = 1;
		gbc_fetchSizeSpinner.gridy = 0;
		add(fetchSizeSpinner, gbc_fetchSizeSpinner);
		
		totalSizeLabel = new GridLabel();
		totalSizeLabel.setText("/ 0");
		GridBagConstraints gbc_totalSizeLabel = new GridBagConstraints();
		gbc_totalSizeLabel.insets = new Insets(0, 0, 0, 5);
		gbc_totalSizeLabel.gridx = 2;
		gbc_totalSizeLabel.gridy = 0;
		add(totalSizeLabel, gbc_totalSizeLabel);
		
		GridLabel grdlblPage = new GridLabel();
		grdlblPage.setText("Page:");
		GridBagConstraints gbc_grdlblPage = new GridBagConstraints();
		gbc_grdlblPage.insets = new Insets(0, 5, 0, 5);
		gbc_grdlblPage.gridx = 3;
		gbc_grdlblPage.gridy = 0;
		add(grdlblPage, gbc_grdlblPage);
		
		pageSpinner = new GridSpinner();
		pageSpinner.setToolTipText("Enter page number (Hit Enter key or click spinner to execute)");
		GridBagConstraints gbc_pageSpinner = new GridBagConstraints();
		gbc_pageSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_pageSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_pageSpinner.gridx = 4;
		gbc_pageSpinner.gridy = 0;
		add(pageSpinner, gbc_pageSpinner);
		
		totalPageCountLabel = new GridLabel();
		resetMaximumPage();
		GridBagConstraints gbc_totalPageCountLabel = new GridBagConstraints();
		gbc_totalPageCountLabel.insets = new Insets(0, 0, 0, 5);
		gbc_totalPageCountLabel.gridx = 5;
		gbc_totalPageCountLabel.gridy = 0;
		add(totalPageCountLabel, gbc_totalPageCountLabel);
		
		prevButton = new JButton("");
		prevButton.setToolTipText("Previous page");
		prevButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/Rewind16.gif")));
		prevButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc_prevButton = new GridBagConstraints();
		gbc_prevButton.insets = new Insets(0, 10, 0, 5);
		gbc_prevButton.gridx = 6;
		gbc_prevButton.gridy = 0;
		add(prevButton, gbc_prevButton);
		
		nextButton = new JButton("");
		nextButton.setToolTipText("Next page");
		nextButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/FastForward16.gif")));
		nextButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc_nextButton = new GridBagConstraints();
		gbc_nextButton.insets = new Insets(0, 0, 0, 5);
		gbc_nextButton.gridx = 7;
		gbc_nextButton.gridy = 0;
		add(nextButton, gbc_nextButton);
		
		firstButton = new JButton("");
		firstButton.setToolTipText("First page");
		firstButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/StepBack16.gif")));
		firstButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc_firstButton = new GridBagConstraints();
		gbc_firstButton.insets = new Insets(0, 10, 0, 5);
		gbc_firstButton.gridx = 8;
		gbc_firstButton.gridy = 0;
		add(firstButton, gbc_firstButton);
		
		lastButton = new JButton("");
		lastButton.setToolTipText("Last page");
		lastButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/StepForward16.gif")));
		lastButton.setMargin(new Insets(0, 0, 0, 0));
		GridBagConstraints gbc_lastButton = new GridBagConstraints();
		gbc_lastButton.insets = new Insets(0, 0, 0, 5);
		gbc_lastButton.gridx = 9;
		gbc_lastButton.gridy = 0;
		add(lastButton, gbc_lastButton);
		
		playButton = new JButton("");
		playButton.setToolTipText("Execute query from the input field (Ctrl-Enter)");
		playButton.setMargin(new Insets(0, 0, 0, 0));
		playButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/Play16.gif")));
		GridBagConstraints gbc_playButton = new GridBagConstraints();
		gbc_playButton.anchor = GridBagConstraints.EAST;
		gbc_playButton.insets = new Insets(0, 20, 0, 5);
		gbc_playButton.gridx = 10;
		gbc_playButton.gridy = 0;
		add(playButton, gbc_playButton);
		
		refreshButton = new JButton("");
		refreshButton.setIcon(new ImageIcon(QueryControlPanel.class.getResource("/images/Refresh16.gif")));
		refreshButton.setMargin(new Insets(0, 0, 0, 0));
		refreshButton.setToolTipText("Refresh (re-execute query)");
		GridBagConstraints gbc_refreshButton = new GridBagConstraints();
		gbc_refreshButton.insets = new Insets(0, 5, 0, 5);
		gbc_refreshButton.gridx = 11;
		gbc_refreshButton.gridy = 0;
		add(refreshButton, gbc_refreshButton);
		
		keyTypeComboBox = new JComboBox();
		keyTypeComboBox.setVisible(false);
		keyTypeComboBox.setEditable(false);
		GridBagConstraints gbc_keyTypeComboBox = new GridBagConstraints();
		gbc_keyTypeComboBox.insets = new Insets(0, 10, 0, 0);
		gbc_keyTypeComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_keyTypeComboBox.gridx = 12;
		gbc_keyTypeComboBox.gridy = 0;
		add(keyTypeComboBox, gbc_keyTypeComboBox);
		
		initUI();
	}
	
	private void initUI()
	{
		DefaultEditor editor = (DefaultEditor)pageSpinner.getEditor();
		final JFormattedTextField textField = editor.getTextField();
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e)
			{
				try {
					firePageSpinnerActionEvent(e);
				} catch (Exception ex) {
					// ignore
				}
			}
			
		});
	}

	protected JSpinner getFetchSizeSpinner() {
		return fetchSizeSpinner;
	}
	protected JSpinner getPageSpinner() {
		return pageSpinner;
	}
	
	public void setKeyTypeComboBoxVisible(boolean keyTypeComboBoxEnabled)
	{
		keyTypeComboBox.setVisible(keyTypeComboBoxEnabled);
	}
	
	public boolean isKeyTypeComboBoxVisible()
	{
		return keyTypeComboBox.isVisible();
	}
	
	public void setKeyTypes(KeyType[] keyType)
	{
		keyTypeComboBox.removeAllItems();
		if (keyType == null) {
			return;
		}
		keyTypeComboBox.addItem(new KeyTypeItem(null));
		for (KeyType keyType2 : keyType) {
			keyTypeComboBox.addItem(new KeyTypeItem(keyType2));
		}
	}
	
	public void setFetchSize(int fetchSize)
	{
		this.fetchSizeSpinner.setValue(fetchSize);
	}
	
	public int getFetchSize()
	{
		return (Integer)fetchSizeSpinner.getValue();
	}
	
	public void setTotalSize(int totalSize)
	{
		this.totalSize = totalSize;
		totalSizeLabel.setText("/ " + numberFormat.format(totalSize));
	}
	
	public int getTotalSize()
	{
		return totalSize;
	}
	
	public void setMinimumFetchSize(int minFetchSize)
	{
		((SpinnerNumberModel)this.fetchSizeSpinner.getModel()).setMinimum(minFetchSize);
	}
	
	public void setMaximumFetchSize(int maximumFetchSize)
	{
		((SpinnerNumberModel)this.fetchSizeSpinner.getModel()).setMaximum(maximumFetchSize);
	}
	
	public void setPage(int page)
	{
		this.pageSpinner.setValue(page);
	}
	
	public int getPage()
	{
		return (Integer)pageSpinner.getValue();
	}

	public void setMinimumPage(int minimumPage)
	{
		((SpinnerNumberModel)this.pageSpinner.getModel()).setMinimum(minimumPage);
	}
	
	public int getMinimumPage()
	{
		return (Integer)((SpinnerNumberModel)this.pageSpinner.getModel()).getMinimum();
	}
	
	public void setMaximumPage(int maximumPage)
	{
		((SpinnerNumberModel)this.pageSpinner.getModel()).setMaximum(maximumPage);
		totalPageCountLabel.setText("/ " + numberFormat.format(maximumPage));
	}
	
	public void resetMaximumPage()
	{
		((SpinnerNumberModel)this.pageSpinner.getModel()).setMaximum(Integer.MAX_VALUE);
		totalPageCountLabel.setText("/");
	}
	
	public int getMaximumPage()
	{
		return (Integer)((SpinnerNumberModel)this.pageSpinner.getModel()).getMaximum();
	}
	
	protected JButton getPrevButton() {
		return prevButton;
	}
	protected JButton getLastButton() {
		return lastButton;
	}
	protected JButton getPlayButton() {
		return playButton;
	}
	protected JButton getNextButton() {
		return nextButton;
	}
	protected JButton getFirstButton() {
		return firstButton;
	}
	
	public void addPageChangedListener(ChangeListener listener)
	{
		pageSpinner.addChangeListener(listener);
	}
	
	public void removePageChangedListener(ChangeListener listener)
	{
		pageSpinner.removeChangeListener(listener);
	}
	
	public void addFetchSizeChangedListener(ChangeListener listener)
	{
		fetchSizeSpinner.addChangeListener(listener);
	}
	
	public void removeFetchSizeChangedListener(ChangeListener listener)
	{
		fetchSizeSpinner.removeChangeListener(listener);
	}
	
	public void addPreviousButtonActionListener(ActionListener listener)
	{
		prevButton.addActionListener(listener);
	}
	
	public void removePreviousButtonActionListener(ActionListener listener)
	{
		prevButton.removeActionListener(listener);
	}
	
	public void addNextButtonActionListener(ActionListener listener)
	{
		nextButton.addActionListener(listener);
	}
	
	public void removeNextButtonActionListener(ActionListener listener)
	{
		nextButton.removeActionListener(listener);
	}
	
	public void addFirstButtonActionListener(ActionListener listener)
	{
		firstButton.addActionListener(listener);
	}
	
	public void removeFirstButtonActionListener(ActionListener listener)
	{
		firstButton.removeActionListener(listener);
	}
	
	public void addLastButtonActionListener(ActionListener listener)
	{
		lastButton.addActionListener(listener);
	}
	
	public void removeLastButtonActionListener(ActionListener listener)
	{
		lastButton.removeActionListener(listener);
	}
	
	public void addPlayButtonActionListener(ActionListener listener)
	{
		playButton.addActionListener(listener);
	}
	
	public void removePlayButtonActionListener(ActionListener listener)
	{
		playButton.removeActionListener(listener);
	}

	public void addRefreshButtonActionListener(ActionListener listener)
	{
		refreshButton.addActionListener(listener);
	}
	
	public void removeRefreshButtonActionListener(ActionListener listener)
	{
		refreshButton.removeActionListener(listener);
	}
	
	public void addPageSpinnerActionListener(ActionListener listener)
	{
		pageSpinnerActionListenerList.add(listener);
	}
	
	public void removePageSpinnerActionListener(ActionListener listener)
	{
		pageSpinnerActionListenerList.remove(listener);
	}
	
	private void firePageSpinnerActionEvent(ActionEvent event)
	{
		for (ActionListener listener : pageSpinnerActionListenerList) {
			listener.actionPerformed(event);
		}
	}
	
	public void addKeyTypeComboBoxItemListener(ItemListener listener)
	{
		keyTypeComboBox.addItemListener(listener);
	}
	
	public void removeKeyTypeComboBoxItemListener(ItemListener listener)
	{
		keyTypeComboBox.removeItemListener(listener);
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
	}
	
	public class KeyTypeItem
	{
		private KeyType keyType;
		
		private KeyTypeItem(KeyType keyType) 
		{
			this.keyType = keyType;
		}
		
		public KeyType getKeyType()
		{
			return keyType;
		}
		
		public String toString()
		{
			if (keyType == null) {
				return "All Versions";
			}
			return keyType.getClass().getSimpleName();
		}
	}
}
