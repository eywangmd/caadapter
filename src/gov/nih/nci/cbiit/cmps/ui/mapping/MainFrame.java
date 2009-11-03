/**
 * The content of this file is subject to the caAdapter Software License (the "License").  
 * A copy of the License is available at:
 * [caAdapter CVS home directory]\etc\license\caAdapter_license.txt. or at:
 * http://ncicb.nci.nih.gov/infrastructure/cacore_overview/caadapter/indexContent
 * /docs/caAdapter_License
 */
package gov.nih.nci.cbiit.cmps.ui.mapping;


import gov.nih.nci.cbiit.cmps.ui.common.ContextManager;
import gov.nih.nci.cbiit.cmps.ui.common.ContextManagerClient;
import gov.nih.nci.cbiit.cmps.ui.common.DefaultSettings;

import javax.swing.*;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class 
 *
 * @author Chunqing Lin
 * @author LAST UPDATE $Author: linc $
 * @since     CMPS v1.0
 * @version    $Revision: 1.2 $
 * @date       $Date: 2008-12-09 19:04:17 $
 *
 */
public class MainFrame extends JFrame
{
	private JTabbedPane tabbedPane = new JTabbedPane();

	private int tabcount = 0;

	private JPanel statusBar = new JPanel();

	private JPanel toolBarPanel;

	private JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

	private JPanel currentToolBarPanel;

	private java.util.Map<Class, JComponent> tabMap;

	private static MainFrame instance = null;

	private MainFrame(){
		super();
	}

	/**
	 * @return the instance
	 */
	public static MainFrame getInstance() {
		if(instance == null) instance = new MainFrame();
		return instance;
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#launch()
	 */
	private void launch()
	{

		try {
			tabMap = new HashMap<Class, JComponent>();
			ContextManager contextManager = ContextManager.getContextManager();

			MainMenuBar frameMenu=new MainMenuBar(this);
			contextManager.setMenu(frameMenu);
			contextManager.setToolBarHandler(new MainToolBarHandler());
			contextManager.initContextManager(this);
			this.setTitle("CMPS");
			Container contentPane = this.getContentPane();
			contentPane.setLayout(new BorderLayout());
			//set the icon.
			Image icon = DefaultSettings.getMainframeImage();//using default image file
			setIconImage(icon);
			// set the menu bar.
			setJMenuBar(frameMenu);
			//set size before constructing each of those panels since some of them
			//may depend on the size to align components.
			this.setSize(DefaultSettings.FRAME_DEFAULT_WIDTH, DefaultSettings.FRAME_DEFAULT_HEIGHT);
			contentPane.add(constructNorthPanel(), BorderLayout.NORTH);
			JPanel p = (JPanel) constructCenterPanel();
			contentPane.add(p, BorderLayout.CENTER); 
			//--------------------------------------
			contentPane.add(statusBar, BorderLayout.SOUTH);
			tabbedPane.addChangeListener(contextManager);
			tabbedPane.setOpaque(false);
			enableEvents(AWTEvent.WINDOW_EVENT_MASK);
			//        this.addWindowListener(new CaWindowClosingListener());
			this.setVisible(true);
			DefaultSettings.centerWindow(this);
			this.setFocusable(true);
			this.setFocusableWindowState(true);

//			CmpsMappingPanel cp = null;
//			try {
//				cp = new CmpsMappingPanel();
//				addNewTab(cp);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			//XXX debug
			//			System.out.println("JPanel bound="+p.getBounds());
			//			System.out.println("CmpsMappingPanel bound="+cp.getBounds());
			//			System.out.println("leftrightpane bound="+cp.getComponent(0).getBounds());
			//			System.out.println("leftrightpane bound="+((JSplitPane)cp.getComponent(0)).getComponent(0).getBounds());
			//			System.out.println("leftrightpane bound="+((JSplitPane)cp.getComponent(0)).getComponent(1).getBounds());
			//			System.out.println("leftrightpane bound="+((JSplitPane)cp.getComponent(0)).getComponent(2).getBounds());
			//			
			//			System.out.println("sourceLocationArea bound="+cp.sourceLocationArea.getBounds());
			//			System.out.println("sourceLocationPanel bound="+cp.sourceLocationPanel.getBounds());
			//			System.out.println("sourceButtonPanel bound="+cp.sourceButtonPanel.getBounds());
			//			System.out.println("sourceScrollPane bound="+cp.sourceScrollPane.getBounds());




			//helpContentViewer = new HelpContentViewer(this);

			//		InitialSplashWindow isw = new InitialSplashWindow();
			//isw.setAlwaysOnTop(true);
			//		DefaultSettings.centerWindow(isw);
			//		isw.setVisible(true);

			//	    try {
			//		    Thread.sleep(2000);
			//		} catch (InterruptedException e) {
			//			e.printStackTrace();
			//		}
			//		isw.dispose();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Component constructCenterPanel() {
		ImageIcon ii1 = new ImageIcon(DefaultSettings.getImage("default_scr.gif"));
		JLabel baseScreenJLabel = new JLabel(ii1);
		ii1.setImageObserver(baseScreenJLabel);
		centerPanel.add(baseScreenJLabel);
		centerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
		centerPanel.setOpaque(false);

		return centerPanel;
	}

	private JPanel constructNorthPanel() {
		Image bannerImage = DefaultSettings.getImage("NCICBBanner.jpg");
		ImageIcon imageIcon = new ImageIcon(bannerImage);
		toolBarPanel = new JPanel(new BorderLayout());
		JPanel northUpperPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		JLabel label = new JLabel(imageIcon);
		northUpperPanel.add(label);
		toolBarPanel.add(northUpperPanel, BorderLayout.NORTH);
		currentToolBarPanel = constructToolbarPanel();
		toolBarPanel.add(currentToolBarPanel, BorderLayout.SOUTH);
		return toolBarPanel;
	}

	private JPanel constructToolbarPanel()
	{
		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(ContextManager.getContextManager().getToolBarHandler().getToolBar(), BorderLayout.CENTER);
		mainP.add(new JPanel(), BorderLayout.EAST);
		return mainP;
	}

	public void updateToolBar(JToolBar newToolBar) {
		updateToolBar(newToolBar, null);
	}

	public void updateToolBar(JToolBar newToolBar, JButton rightSideButton) {
		//remove first in case it already contains
		JToolBar rightSideToolbar = new JToolBar();
		JPanel rightSidePanel = new JPanel(new BorderLayout());
		if (rightSideButton != null)
		{
			rightSideToolbar.add(rightSideButton);
			rightSidePanel.add(rightSideToolbar, BorderLayout.CENTER);
		}
		toolBarPanel.remove(currentToolBarPanel);
		currentToolBarPanel.removeAll();
		currentToolBarPanel.add(newToolBar, BorderLayout.CENTER);
		currentToolBarPanel.add(rightSidePanel, BorderLayout.EAST);
		toolBarPanel.add(currentToolBarPanel, BorderLayout.SOUTH);
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#addNewTab(javax.swing.JPanel)
	 */
	public void addNewTab(JPanel panel) {
		if (tabbedPane.getTabCount() == 0) {
			centerPanel.removeAll();
			centerPanel.setLayout(new BorderLayout());
			centerPanel.add(tabbedPane, BorderLayout.CENTER);
		}
		String title = null;

		if (panel instanceof CmpsMappingPanel) {
			title = "Untitled_" + (++tabcount) + ".map";
		}
		else
			title = "Untitled_" + (++tabcount) + ".dat";
		tabbedPane.addTab(title, panel);
		tabbedPane.setSelectedComponent(panel);
		System.out.println("Panel Class: '" + (panel==null?"null":panel.getClass().getName()) + "'.");
		tabMap.put(panel.getClass(), panel);
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#closeTab()
	 */
	public void closeTab() {
		Component comp = tabbedPane.getSelectedComponent();
		tabbedPane.remove(comp);
		if (tabbedPane.getTabCount() == 0) {//reset if not tab at all.
			tabcount = 0;
			centerPanel.removeAll();
			centerPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
			//			centerPanel.add(baseScreenJLabel);
			centerPanel.update(centerPanel.getGraphics());
			//-------------------------------------------------------------------------------------------------
		}

		if( comp != null ){
			tabMap.remove(comp.getClass());

			if (comp instanceof ContextManagerClient) {
				ContextManager.getContextManager().getContextFileManager().removeFileUsageListener((ContextManagerClient) comp);
			}
		}
	}
	/**
	 * Only accessible by the same package so as to
	 * avoid abuse of using the tabbedPane directly.
	 * @return the tab pane.
	 */
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#hasComponentOfGivenClass(java.lang.Class, boolean)
	 */
	public JComponent hasComponentOfGivenClass(Class classValue, boolean bringToFront) {
		JComponent component = tabMap.get(classValue);
		if (component != null) {
			if (bringToFront) {
				try {
					tabbedPane.setSelectedComponent(component);
				} catch (Throwable e) {
					e.printStackTrace();
					//		    Log.logInfo(this, "What kind of Component is this: '" + component.getClass().getName() + "'.");
					//		    Log.logException(this, e);
				}
			}
			return component;
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#getAllTabs()
	 */
	public java.util.List<Component> getAllTabs() {
		java.util.List<Component> resultList = new java.util.ArrayList<Component>();
		int count = tabbedPane.getComponentCount();
		for (int i = 0; i < count; i++) {
			Component comp = tabbedPane.getComponentAt(i);
			resultList.add(comp);
		}
		return resultList;
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#setCurrentPanelTitle(java.lang.String)
	 */
	public boolean setCurrentPanelTitle(String newTitle) {
		int seleIndex = tabbedPane.getSelectedIndex();
		if (seleIndex != -1) {
			tabbedPane.setTitleAt(seleIndex, newTitle);
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args)
	{
		//Preferences.loadDefaults();
		try
		{
			try
			{
				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			}
			catch (ClassNotFoundException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (InstantiationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (IllegalAccessException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			catch (UnsupportedLookAndFeelException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			MainFrame.getInstance().launch();
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			//	        Log.logException(new Object(), t);
		}
	}


	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#resetCenterPanel()
	 */
	public void resetCenterPanel() // inserted by umkis on 01/18/2006, defaect# 252
	{
		centerPanel.updateUI();
	}


	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#processWindowEvent(java.awt.event.WindowEvent)
	 */
	public void processWindowEvent(WindowEvent e) {
		//		Log.logInfo(this, "processWindowEvent() invoked with '" + e + "'.");
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.getMenuBar();
			//		MainMenuBar frameMenuBar=(MainMenuBar)ContextManager.getContextManager().getMenu();
			//		CloseAllAction closeAllAction=(CloseAllAction)frameMenuBar.getDefinedAction(ActionConstants.CLOSE_ALL);//.closeAllAction;
			//	    CloseAllAction closeAllAction =ContextManager.getContextManager().getMenu().closeAllAction;
			//	    if (closeAllAction != null && closeAllAction.isEnabled()) {
			//		closeAllAction.actionPerformed(null);
			//		if (closeAllAction.isSuccessfullyPerformed()) {
			//		    super.processWindowEvent(e);
			//		} else {//back to normal process.
			//		    return;
			//		}
			//	    } else {
			//		super.processWindowEvent(e);
			//	    }
			exit();
		} else {
			super.processWindowEvent(e);
		}
	}

	/* (non-Javadoc)
	 * @see gov.nih.nci.caadapter.ui.main.AbstractMainFrame#exit()
	 */
	public void exit() {
		//    	ContextManager contextManager = ContextManager.getContextManager();
		//		if (contextManager.isItOKtoShutdown()) {
		this.exit(0);
		//		}
	}

	protected void exit(int errorLevel) {
		this.setVisible(false);
		this.dispose();
		//	Log.logInfo(this, "\r\n\r\nShutting down logging with exit code = " + errorLevel + "\r\n\r\n" + "===============================================================\r\n" + "===============================================================\r\n");
		System.exit(errorLevel);
	}
}
/**
 * HISTORY: $Log: not supported by cvs2svn $
 * HISTORY: Revision 1.1  2008/12/03 20:46:14  linc
 * HISTORY: UI update.
 * HISTORY:
 */