/**
 * <!-- LICENSE_TEXT_START -->
The contents of this file are subject to the caAdapter Software License (the "License"). You may obtain a copy of the License at the following location: 
[caAdapter Home Directory]\docs\caAdapter_license.txt, or at:
http://ncicb.nci.nih.gov/infrastructure/cacore_overview/caadapter/indexContent/docs/caAdapter_License
 * <!-- LICENSE_TEXT_END -->
 */


package gov.nih.nci.caadapter.ui.mapping.GME;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * This is the main class of wizard.
 * @author OWNER: Scott Jiang
 * @author LAST UPDATE $Author: phadkes $
 * @version Since caAdapter v1.2
 *          revision    $Revision: 1.3 $
 *          date        $Date: 2008-09-23 19:33:18 $
 */
public class NewMappingPanelWizard extends JDialog implements ActionListener
{
	private static final String OK_COMMAND = "OK";
	private static final String CANCEL_COMMAND = "Cancel";
	private int selectionType=1;
	//centerWindow
	private MappingTypePanelFrontPage frontPage;
	private boolean okButtonClicked = false;

	public NewMappingPanelWizard(Frame owner, String title, boolean modal) throws HeadlessException
	{
		super(owner, title, modal);
		initialize();
	}

	private void initialize()
	{
		Container contentPane = this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		frontPage = new MappingTypePanelFrontPage();
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
		this.setSize(360,240);
		this.setResizable(false);
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
		if(OK_COMMAND.equals(command))
		{
			this.setSelectionType(frontPage.getSelectionType());
			okButtonClicked = true;
		}
		else if(CANCEL_COMMAND.equals(command))
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

	public int getSelectionType() {
		return selectionType;
	}

	public void setSelectionType(int selectionType) {
		this.selectionType = selectionType;
	}

}
/**
 * $Log: not supported by cvs2svn $
*/