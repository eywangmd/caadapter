/**
 * The content of this file is subject to the caAdapter Software License (the "License").  
 * A copy of the License is available at:
 * [caAdapter CVS home directory]\etc\license\caAdapter_license.txt. or at:
 * http://ncicb.nci.nih.gov/infrastructure/cacore_overview/caadapter/indexContent
 * /docs/caAdapter_License
 */


package gov.nih.nci.cbiit.cmps.ui.message;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * This class is the main entry class of message wizard to collect user's inputs.
 * @author Chunqing Lin
 * @author LAST UPDATE $Author: linc $
 * @since     CMPS v1.0
 * @version    $Revision: 1.1 $
 * @date       $Date: 2008-12-09 19:04:17 $
 */
public class OpenMessageWizard extends JDialog implements ActionListener
{
	private static final String OK_COMMAND = "OK";

	private static final String CANCEL_COMMAND = "Cancel";
	private boolean okButtonClicked = false;
	private OpenMessageFrontPage frontPage;

	public static final String TITLE = "Select Data Input and Map Specification...";

	public OpenMessageWizard(Frame owner, String title, boolean modal) throws HeadlessException
	{
		super(owner, title, modal);
		initialize();
	}

	public OpenMessageWizard(Dialog owner, String title, boolean modal)
			throws HeadlessException
	{
		super(owner, title, modal);
		initialize();
	}

    private void initialize()
	{
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		frontPage = new OpenMessageFrontPage(this);
		contentPane.add(frontPage, BorderLayout.CENTER);

		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));//new BorderLayout());
		JButton okButton = new JButton(OK_COMMAND);
		okButton.setMnemonic('O');
		okButton.addActionListener(this);
		JButton cancelButton = new JButton(CANCEL_COMMAND);
		cancelButton.setMnemonic('C');
		cancelButton.addActionListener(this);
		JPanel tempPanel = new JPanel(new GridLayout(1, 2));
		tempPanel.add(okButton);
		tempPanel.add(cancelButton);
		buttonPanel.add(tempPanel);//, BorderLayout.EAST);
		southPanel.add(buttonPanel, BorderLayout.NORTH);
		contentPane.add(southPanel, BorderLayout.SOUTH);
		pack();
	}

	public File getMapFile()
	{
		return frontPage.getMapFile();
	}

	public File getDataFile()
	{
		return frontPage.getDataFile();
	}

	public File getDestFile()
	{
		return frontPage.getDestFile();
	}

	public boolean isOkButtonClicked()
	{
		return okButtonClicked;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e)
	{
		String command = e.getActionCommand();
		if (OK_COMMAND.equals(command))
		{
			if(!frontPage.validateInputFields())
			{
				okButtonClicked = false;
				return;
			}
			else
			{
				okButtonClicked = true;
			}
		}
		else if (CANCEL_COMMAND.equals(command))
		{
			okButtonClicked = false;
		}
		else
		{
			System.err.println("Why am I here with command '" + command + "'?");
		}
		setVisible(false);
		dispose();
	}

//	public static void main(String[] args)
//	{
//		OpenHL7MessageWizard openWizard = new OpenHL7MessageWizard((Frame) null, "test openWizard", true);
//		DefaultSettings.centerWindow(openWizard);
//		openWizard.setVisible(true);
//		if(openWizard.isOkButtonClicked())
//		{
//			Log.logInfo(openWizard, "Data file selected: '" + openWizard.getDataFile().getAbsolutePath());
//			Log.logInfo(openWizard, "Map specification selected: '" + openWizard.getMapFile().getAbsolutePath());
//		}
//	}
}
/**
 * HISTORY      : $Log: not supported by cvs2svn $
 */