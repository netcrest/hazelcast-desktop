package com.netcrest.pado.ui.swing.pado.hazelcast;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.InternalFrameEvent;
import javax.swing.plaf.FontUIResource;

import com.netcrest.commandspace.CommandDescriptor;
import com.netcrest.commandspace.CommandPost;
import com.netcrest.commandspace.ICommandProvider;
import com.netcrest.commandspace.IDesktopCommandNames;
import com.netcrest.pado.exception.PadoException;
import com.netcrest.pado.internal.Constants;
import com.netcrest.pado.internal.util.PadoUtil;
import com.netcrest.pado.ui.swing.GridDesktopUtil;
import com.netcrest.pado.ui.swing.GridLoginDialog;
import com.netcrest.pado.ui.swing.GridLoginFrame;
import com.netcrest.pado.ui.swing.GridLoginPanel;
import com.netcrest.pado.ui.swing.ISharedCache;
import com.netcrest.pado.ui.swing.info.LoginInfo;
import com.netcrest.pado.ui.swing.pado.gemfire.ICommandNames;
import com.netcrest.pado.ui.swing.pado.gemfire.PadoInfoManager;
import com.netcrest.services.ICommandSpace;
import com.netcrest.services.IPersistenceManager;
import com.netcrest.services.IPreferenceManager;
import com.netcrest.services.IServiceNames;
import com.netcrest.services.NamingService;
import com.netcrest.ui.desktop.Desktop;
import com.netcrest.ui.desktop.DetachedFrame;
import com.netcrest.ui.desktop.IDesktopInit;
import com.netcrest.ui.desktop.IDesktopNames;
import com.netcrest.ui.desktop.InternalFrame;
import com.netcrest.ui.desktop.Worksheet;
import com.netcrest.ui.desktop.Workspace;
import com.netcrest.ui.swing.util.SwingUtil;
import com.netcrest.util.StringManipulator;

public class HazelcastDesktopInit implements IDesktopInit
{
	private Desktop desktop;

	private CommandPost commandPost = new CommandPost();
	private GridLoginDialog loginDialog;
	private GridLoginFrame loginFrame;
	private String username;
	private String desktopTitle;

	private LoginInfo loginInfo = new LoginInfo();

	public HazelcastDesktopInit()
	{
		super();
	}

	private static void initializeFontSize()
	{
		String fontSizeParam = PadoUtil.getProperty("font.size");
		if (fontSizeParam == null) {
			return;
		}
		int newSize = Integer.parseInt(fontSizeParam);
		if (fontSizeParam != null) {
			UIDefaults defaults = UIManager.getDefaults();
			int i = 0;
			for (Enumeration e = defaults.keys(); e.hasMoreElements(); i++) {
				Object key = e.nextElement();
				Object value = defaults.get(key);
				if (value instanceof Font) {
					Font font = (Font) value;
					if (value instanceof FontUIResource) {
						defaults.put(key, new FontUIResource(font.getName(), font.getStyle(), newSize));
					} else {
						defaults.put(key, new Font(font.getName(), font.getStyle(), newSize));
					}
				}
			}
		}
	}
	
	public void beforeRealized(Desktop desktop)
	{
		this.desktop = desktop;

		initializeFontSize();

		switch (desktop.getAppType()) {
		case Desktop.WEBSTART:
		case Desktop.APPLET:
			try {
				initPadoJnlp();
			} catch (Exception ex) {
				SwingUtil.showErrorMessageDialog(desktop, "Error occurred while initializing SSL", ex);
				System.exit(-1);
			}
			break;
		}

		try {
			boolean isConfigFileEnabled = PadoUtil.getBoolean("hazelcast.client.config.file.enabled", false);
			if (isConfigFileEnabled) {
				loginViaConfigFile();
			} else {
				login();
			}
		} catch (Exception ex) {
			GridLoginFrame loginFrame = getLoginFrame();
			JOptionPane.showMessageDialog(loginFrame, ex.getLocalizedMessage(), "Commnucations Error",
					JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
	}

	/**
	 * Extracts the specified resource in the user's home directory under .pado/
	 * and sets the system property to the absolute file path.
	 * 
	 * @param systemPropertyName
	 *            System property name.
	 * @param fromPath
	 *            JNLP relative from-file path
	 * @param toPath
	 *            <user-home>/.pado relative to-file path.
	 */
	private void extractResource(String systemPropertyName, String fromPath, String toPath)
	{
		try {
			URL url = this.getClass().getResource(fromPath);
			if (url == null) {
				System.out.println(fromPath + " does not exists.");
				return;
			}
			InitialContext initial = (InitialContext) NamingService.getInitialContext();
			IPersistenceManager pm = (IPersistenceManager) initial.lookup(IServiceNames.SVC_PERSISTENCE_MANAGER);
			byte[] data = pm.read(url);

			String userHomeDir = System.getProperty("user.home");
			String padoDir = userHomeDir + File.separator + ".pado";
			File file = new File(padoDir + File.separator + toPath);

			File parentFile = file.getParentFile();
			if (parentFile.exists() == false) {
				parentFile.mkdirs();
			}
			FileOutputStream os = new FileOutputStream(file);
			os.write(data);
			os.close();
			System.setProperty(systemPropertyName, file.getAbsolutePath());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Initializes Pado specifics for JNLP.
	 * 
	 * @throws NamingException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void initPadoJnlp() throws NamingException, MalformedURLException, IOException
	{
		// Place necessary configuration files in the local file system
		// and set system properties for local file paths.

		InitialContext initial = (InitialContext) NamingService.getInitialContext();
		IPersistenceManager pm = (IPersistenceManager) initial.lookup(IServiceNames.SVC_PERSISTENCE_MANAGER);
		IPreferenceManager preferenceManager = desktop.getPreferenceManager();
		Properties props = pm.openProperties(new URL(preferenceManager.getCodeBaseURL(), "etc/gfsecurity.properties"));
		String val = props.getProperty("cluster-ssl-enabled");
		boolean sslEnabled = val != null && val.equalsIgnoreCase("true");
		if (sslEnabled == false) {
			return;
		}

		extractResource(Constants.PROP_SYSTEM_PADO + Constants.PROP_SECURITY_AES_USER_CERTIFICATE, "/security/user.cer",
				"security/user.cer");
		extractResource(Constants.PROP_SYSTEM_PADO + Constants.PROP_SECURITY_KEYSTORE_FILE_PATH,
				"/security/client/desktop.keystore", "security/client/desktop.keystore");
		extractResource("gemfire.cluster-ssl-truststore", "/security/ssl/trusted.keystore",
				props.getProperty("cluster-ssl-truststore", "security/ssl/trusted.keystore"));
		extractResource("gemfire.cluster-ssl-keystore", "/security/ssl/server.keystore",
				props.getProperty("cluster-ssl-keystore", "security/ssl/server.keystore"));
		extractResource("gemfireSecurityPropertyFile", "/etc/gfsecurity.properties", "etc/gfsecurity.properties");
	}
	
	private synchronized void loginViaConfigFile() throws PadoException, LoginException
	{
		username = "client";
		String envName =  "hazelcast";
		String appId = "dev";
		String domain = "dev";
		String username = "sys";
		String locators = "localhost:5701";
		
		ISharedCache sharedCache = HazelcastSharedCache.getSharedCache();
		sharedCache.login(envName, locators, appId, domain, username, null);

		// pado://_USER:dpark@localhost:20000
		desktopTitle = "Hazelcast Desktop (" + getPaodUri(envName, appId, username, locators) + ")";
		Frame frame = Desktop.findAncestorFrame(desktop);
		if (frame != null) {
			frame.setTitle(desktopTitle);
		}
	}

	private synchronized void login()
	{
		// Initialize the login dialog
		GridLoginFrame loginFrame = getLoginFrame();
		loginFrame.setVisible(true);

		int loginAttempCount = 0;
		// while (loginInfo.loginStatus == LoginInfo.LOGIN_NOT_PERFORMED) {
		while (loginFrame.getLoginPanel().getMode() == GridLoginPanel.MODE_IDLE) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		loginAttempCount++;

		if (loginFrame.getLoginPanel().getMode() == GridLoginPanel.MODE_CANCEL) {
			System.exit(0);
		}

		username = loginFrame.getLoginPanel().getUserName();

		if (loginAttempCount == 3) {
			System.exit(-1);
		}
		// }
		loginFrame.setVisible(false);
		loginFrame.dispose();

		String envName = loginFrame.getLoginPanel().getEnvName();
		String appId = loginFrame.getLoginPanel().getAppId();
		String username = loginFrame.getLoginPanel().getUserName();
		String locators = loginFrame.getLoginPanel().getLocators();

		// pado://_USER:dpark@localhost:20000
		desktopTitle = "Hazelcast Desktop (" + getPaodUri(envName, appId, username, locators) + ")";
		Frame frame = Desktop.findAncestorFrame(desktop);
		if (frame != null) {
			frame.setTitle(desktopTitle);
		}
	}

	private String getPaodUri(String envName, String appId, String username, String locators)
	{
		return "pado://" + envName + ":" + appId + ":" + username + "@" + locators;
	}
	
	public String getDesktopTitle()
	{
		return desktopTitle;
	}

	public void afterRealized(Desktop desktop)
	{
		GridDesktopUtil.initialize(desktop);

		// Create the command provider
		GridDesktopInitCommandProvider commandProvider = new GridDesktopInitCommandProvider();

		try {
			InitialContext initial = NamingService.getInitialContext();
			ICommandSpace commandSpace = (ICommandSpace) initial.lookup(IServiceNames.SVC_COMMAND_SPACE);
			commandPost = new CommandPost();
			commandSpace.addCommandPost(IDesktopCommandNames.BUS_ACTIVE_DESKTOP, commandPost);
			commandPost.addCommandProvider(IDesktopNames.CS_DESKTOP.TOPIC, commandProvider);
		} catch (NamingException ex) {
			ex.printStackTrace();
		}

		// post commands for the realized components
		commandPost.execCommand(ICommandNames.BUS_ALL_WORKSPACES, ICommandNames.CS_PADO_INFO.TOPIC,
				ICommandNames.CS_PADO_INFO.COMMAND_onPadoInfo, PadoInfoManager.getPadoInfoManager().getPadoInfo());
		System.out.println();
	}

	protected GridLoginDialog getLoginDialog()
	{
		if (loginDialog == null) {
			Frame frame = desktop.findAncestorFrame(desktop);
			loginDialog = new GridLoginDialog(frame, "Grid Login Dialog", true);
			loginDialog.setSize(350, 230);
			if (frame == null) {
				loginDialog.setLocation(100, 100);
			} else {
				int x = frame.getLocationOnScreen().x + frame.getSize().width / 2 - loginDialog.getSize().width / 2;
				int y = frame.getLocationOnScreen().y + frame.getSize().height / 2 - loginDialog.getSize().height / 2;
				loginDialog.setLocation(x, y);
			}
		}
		return loginDialog;
	}

	protected GridLoginFrame getLoginFrame()
	{
		if (loginFrame == null) {
			loginFrame = new GridLoginFrame();
			Frame frame = Desktop.findAncestorFrame(desktop);
			if (frame != null && frame.getIconImage() != null) {
				loginFrame.setIconImage(frame.getIconImage());
			}

			Dimension windowSize = loginFrame.getSize();
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			loginFrame.setBounds((screenSize.width - windowSize.width) / 2, (screenSize.height - windowSize.height) / 2,
					windowSize.width, windowSize.height);
		}

		return loginFrame;

	}

	public class GridDesktopInitCommandProvider implements ICommandProvider
	{
		private int locationX = 50;
		private int locationY = 50;

		public GridDesktopInitCommandProvider()
		{
		}

		private CommandDescriptor commandDescriptors[] = {
				
		};

		private void openWorkspace(Component comp, int width, int height)
		{
			openWorkspace(comp, locationX, locationY, width, height);
		}

		private void openWorkspace(Component comp, int x, int y, int width, int height)
		{
			InternalFrame internalFrame = desktop.findInternalFrame(comp);
			if (internalFrame == null) {
				DetachedFrame detachedFrame = desktop.findDetachedFrame(comp);
				if (detachedFrame == null) {
					String name = StringManipulator.getShortClassName(comp);
					internalFrame = desktop.openNewWorkspace(1);
					internalFrame.setTitle(name);
					Workspace workspace = internalFrame.getWorkspace();
					Worksheet worksheet = workspace.getWorksheet(0);
					worksheet.getRootBeanPanel().add(comp);
					worksheet.setName(name);
					internalFrame.setSize(width, height);
					internalFrame.setLocation(x, y);
					internalFrame.setDefaultCloseOperation(InternalFrame.HIDE_ON_CLOSE);
				} else {
					detachedFrame.setExtendedState(Frame.NORMAL);
					detachedFrame.setVisible(true);
					detachedFrame.toFront();
				}
			} else {

				internalFrame.setLocation(x, y);
				try {
					internalFrame.setIcon(false);
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				internalFrame.setVisible(true);
				internalFrame.toFront();
			}
		}

		private Component getComponent(Class compClass, int width, int height, String title)
		{
			return getComponent(compClass, -1, -1, width, height, title);
		}

		private Component getComponent(Class compClass, int x, int y, int width, int height, String title)
		{
			// Search InternalFrame
			InternalFrame frames[] = desktop.getAllInternalFrames();
			InternalFrame frame;
			ArrayList frameList = new ArrayList();
			Workspace workspace;
			for (int i = 0; i < frames.length; i++) {
				frame = (InternalFrame) frames[i];
				workspace = frame.getWorkspace();
				if (workspace.hasComponent(compClass)) {
					frameList.add(frame);
				}
			}

			// Close all frames except one. There should be only 0 or 1 frame.
			frame = null;
			frames = (InternalFrame[]) frameList.toArray(new InternalFrame[0]);
			if (frames.length > 0) {
				frame = frames[0];
			}
			for (int i = 1; i < frames.length; i++) {
				try {
					frames[i].setClosed(true);
				} catch (PropertyVetoException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// See if there are any DetachedFrames
			DetachedFrame detachedFrames[] = desktop.getAllDetachedFrames();
			DetachedFrame detachedFrame;
			frameList.clear();
			for (int i = 0; i < detachedFrames.length; i++) {
				detachedFrame = detachedFrames[i];
				workspace = detachedFrame.getWorkspace();
				if (workspace.hasComponent(compClass)) {
					frameList.add(detachedFrame);
				}
			}

			// Close all frames except one. There should be only 0 or 1 frame.
			detachedFrame = null;
			int startIndex = 0;
			detachedFrames = (DetachedFrame[]) frameList.toArray(new DetachedFrame[0]);
			if (frame == null && detachedFrames.length > 0) {
				detachedFrame = detachedFrames[0];
				startIndex = 1;
			}
			for (int i = startIndex; i < detachedFrames.length; i++) {
				detachedFrames[i].dispose();
			}

			Component comp = null;
			if (frame != null) {
				workspace = frame.getWorkspace();
				Worksheet worksheets[] = workspace.getAllWorksheets();
				for (int i = 0; i < worksheets.length; i++) {
					Component beans[] = worksheets[i].getAllBeans();
					for (int j = 0; j < beans.length; j++) {
						if (beans[i].getClass() == compClass) {
							comp = beans[i];
							frame.setDefaultCloseOperation(InternalFrame.HIDE_ON_CLOSE);
							break;
						}
					}
					if (comp != null) {
						break;
					}
				}
			} else if (detachedFrame != null) {
				workspace = detachedFrame.getWorkspace();
				Worksheet worksheets[] = workspace.getAllWorksheets();
				for (int i = 0; i < worksheets.length; i++) {
					Component beans[] = worksheets[i].getAllBeans();
					for (int j = 0; j < beans.length; j++) {
						if (beans[i].getClass() == compClass) {
							comp = beans[i];
							break;
						}
					}
					if (comp != null) {
						break;
					}
				}
			} else {
				// Create a new one
				String name = title;
				if (title == null) {
					name = StringManipulator.getShortClassName(compClass.getName());
				}
				frame = desktop.createInternalFrame(1);
				frame.setDefaultCloseOperation(InternalFrame.HIDE_ON_CLOSE);
				frame.setTitle(name);
				workspace = frame.getWorkspace();
				Worksheet worksheet = workspace.getWorksheet(0);
				try {
					comp = (Component) compClass.newInstance();
					worksheet.getRootBeanPanel().add(comp);
					worksheet.setName(name);
					frame.setSize(width, height);
					if (x >= 0) {
						frame.setLocation(x, y);
					} else {
						frame.setLocation(locationX, locationY);
						locationX += 26;
						locationY += 26;
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			return comp;

		}

		private void workspace_internalFrameClosing(InternalFrameEvent e)
		{
			InternalFrame frame = (InternalFrame) e.getInternalFrame();
		}

		public CommandDescriptor[] getCommandDescriptors()
		{
			return commandDescriptors;
		}
	}

}