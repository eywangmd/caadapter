/**
 * The content of this file is subject to the caAdapter Software License (the "License").  
 * A copy of the License is available at:
 * [caAdapter CVS home directory]\etc\license\caAdapter_license.txt. or at:
 * http://ncicb.nci.nih.gov/infrastructure/cacore_overview/caadapter/indexContent
 * /docs/caAdapter_License
 */
package gov.nih.nci.cbiit.cmps.ui.tree;

import gov.nih.nci.cbiit.cmps.core.FunctionDef;
import gov.nih.nci.cbiit.cmps.ui.common.CommonTransferHandler;
import gov.nih.nci.cbiit.cmps.ui.common.MappableNode;
import gov.nih.nci.cbiit.cmps.ui.common.UIHelper;
import gov.nih.nci.cbiit.cmps.ui.function.FunctionTypeNodeLoader;
import gov.nih.nci.cbiit.cmps.ui.mapping.CmpsMappingPanel;
import gov.nih.nci.cbiit.cmps.ui.mapping.ElementMetaLoader;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * This class 
 *
 * @author Chunqing Lin
 * @author LAST UPDATE $Author: wangeug $
 * @since     CMPS v1.0
 * @version    $Revision: 1.8 $
 * @date       $Date: 2009-12-02 18:46:50 $
 *
 */
public class TreeTransferHandler extends CommonTransferHandler {

	private CmpsMappingPanel panel;
	/**
	 * @param tree
	 */
	public TreeTransferHandler(CmpsMappingPanel panel) {
		this.panel = panel;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#canImport(javax.swing.TransferHandler.TransferSupport)
	 */
	@Override
	public boolean canImport(TransferSupport info) {


        if (!info.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            return false;
        }

        JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
        TreePath path = dl.getPath();
        if (path == null) {
            return false;
        }
        if(path.getLastPathComponent() instanceof DefaultSourceTreeNode){
        	return false;
        }
        return true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
	 */
	@Override
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree)c;
		TreePath path = tree.getSelectionPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
		String pathString = null;
		if(node.getUserObject() instanceof ElementMetaLoader.MyTreeObject)
			pathString = UIHelper.getPathStringForNode(node);
		else if(node.getUserObject() instanceof FunctionTypeNodeLoader.MyTreeObject){
			FunctionDef f =((FunctionDef)((FunctionTypeNodeLoader.MyTreeObject)node.getUserObject()).getObj()); 
			pathString = f.getGroup()+":"+f.getName();
		}
		System.out.println("TreeTransferHandler.createTransferable() ..createTransferable: obj="+pathString);
		return new StringSelection(pathString);
	}


	/* (non-Javadoc)
	 * @see javax.swing.TransferHandler#importData(javax.swing.TransferHandler.TransferSupport)
	 */
	@Override
	public boolean importData(TransferSupport info) {
        String data;
        try {
            data = (String)info.getTransferable().getTransferData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException e) {
        	e.printStackTrace();
            return false;
        } catch (IOException e) {
        	e.printStackTrace();
            return false;
        }
       
        JTree.DropLocation dl = (JTree.DropLocation)info.getDropLocation();
        TreePath path = dl.getPath();
        DefaultMutableTreeNode targetNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        DefaultMutableTreeNode sourceNode = UIHelper.findTreeNodeWithXmlPath((DefaultMutableTreeNode)panel.getSourceTree().getModel().getRoot(), data);
        
        boolean ret = this.panel.getMiddlePanel().getGraphController().createMapping((MappableNode)sourceNode, (MappableNode)targetNode);
        System.out.println("TreeTransferHandler.importData()..dragged object:"+data +"...accepted:"+ret);
        return ret;
	}

}
/**
 * HISTORY: $Log: not supported by cvs2svn $
 * HISTORY: Revision 1.7  2009/11/03 18:31:15  wangeug
 * HISTORY: clean codes: keep MiddlePanelJGraphController only with MiddleMappingPanel
 * HISTORY:
 * HISTORY: Revision 1.6  2009/10/28 16:45:56  wangeug
 * HISTORY: clean codes
 * HISTORY:
 * HISTORY: Revision 1.5  2009/10/27 18:23:10  wangeug
 * HISTORY: clean codes
 * HISTORY:
 * HISTORY: Revision 1.4  2008/12/29 22:18:18  linc
 * HISTORY: function UI added.
 * HISTORY:
 * HISTORY: Revision 1.3  2008/12/10 15:43:03  linc
 * HISTORY: Fixed component id generator and delete link.
 * HISTORY:
 * HISTORY: Revision 1.2  2008/12/09 19:04:17  linc
 * HISTORY: First GUI release
 * HISTORY:
 * HISTORY: Revision 1.1  2008/12/04 21:34:20  linc
 * HISTORY: Drap and Drop support with new Swing.
 * HISTORY:
 */

