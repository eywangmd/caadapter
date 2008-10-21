/**
 * The content of this file is subject to the caAdapter Software License (the "License").  
 * A copy of the License is available at:
 * [caAdapter CVS home directory]\etc\license\caAdapter_license.txt. or at:
 * http://ncicb.nci.nih.gov/infrastructure/cacore_overview/caadapter/indexContent
 * /docs/caAdapter_License
 */
package gov.nih.nci.cbiit.cmps.common;

import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import org.apache.xerces.xs.*;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import gov.nih.nci.cbiit.cmps.core.*; 

/**
 * This class 
 *
 * @author Chunqing Lin
 * @author LAST UPDATE $Author: linc $
 * @since     CMPS v1.0
 * @version    $Revision: 1.5 $
 * @date       $Date: 2008-10-21 20:49:08 $
 *
 */
public class XSDParser implements DOMErrorHandler {
	private XSLoader schemaLoader;
	private XSModel model;
	private Stack<String> ctStack;
	private String defaultNS = "";
	private static boolean debug = false;
	//private static final String[] prefix={">", "  =", "    -", "      *", "        %", "          $"};

	private static String getPrefix(int i){
		//if(i<prefix.length) return prefix[i];
		StringBuffer sb = new StringBuffer();
		for(int j=0; j<i+1; j++) sb.append("  ");
		sb.append("[").append(i<10?((char)('0'+i)):((char)('a'+i-10))).append("]-");
		return sb.toString();
	}

	public XSDParser() {
		try {
			// get DOM Implementation using DOM Registry
			System.setProperty(
					DOMImplementationRegistry.PROPERTY,
					"org.apache.xerces.dom.DOMXSImplementationSourceImpl");
			DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();

			XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");

			schemaLoader = impl.createXSLoader(null);

			DOMConfiguration config = schemaLoader.getConfig();

			// create Error Handler
			DOMErrorHandler errorHandler = this;

			// set error handler
			config.setParameter("error-handler", errorHandler);

			// set validation feature
			config.setParameter("validate", Boolean.TRUE);

			ctStack = new Stack<String>();
		} catch (ClassCastException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void loadSchema(String schemaURI) {

		// parse document
		if(debug) System.out.println("Parsing " + schemaURI + "...");
		model = schemaLoader.loadURI(schemaURI);

	}

	public ElementMeta getElementMeta(String namespace, String name){
		if (model != null) {
			// element declarations
			XSNamedMap map = model.getComponents(XSConstants.ELEMENT_DECLARATION);
			//processMap(map, 0);
			defaultNS = namespace;
			ctStack.clear();
			return processXSObject(map.itemByName(namespace, name), 0);
			//map = model.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
			//map = model.getComponents(XSConstants.TYPE_DEFINITION);
			//map = model.getComponents(XSConstants.NOTATION_DECLARATION);
		} else {
			return null;
		}

	}

	public ElementMeta getElementMetaFromComplexType(String namespace, String name){
		if (model != null) {
			// element declarations
			XSNamedMap map = model.getComponents(XSConstants.TYPE_DEFINITION);
			//processMap(map, 0);
			defaultNS = namespace;
			ctStack.clear();
			return processXSObject(map.itemByName(namespace, name), 0);
			//map = model.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
			//map = model.getComponents(XSConstants.NOTATION_DECLARATION);
		} else {
			return null;
		}

	}

	public static URL getResource(String name){
		URL ret = null;
		ret = XSDParser.class.getClassLoader().getResource(name);
		if(ret!=null) return ret;
		ret = XSDParser.class.getClassLoader().getResource("/"+name);
		if(ret!=null) return ret;
		ret = ClassLoader.getSystemResource(name);
		if(ret!=null) return ret;
		ret = ClassLoader.getSystemResource("/"+name);
		return ret;
	}


	private ElementMeta processXSObject(XSObject item, int depth) {
		if(item instanceof XSComplexTypeDefinition){
			return processComplexType((XSComplexTypeDefinition)item, depth);
		}else if(item instanceof XSSimpleTypeDefinition){
			//processSimpleType((XSSimpleTypeDefinition)item);
			return null;
		}else if(item instanceof XSElementDeclaration){
			return processElement((XSElementDeclaration)item, depth);
		}
		return null;
	}

	private List<ElementMeta> processMap(XSNamedMap map, int depth){
		ArrayList<ElementMeta> ret = new ArrayList<ElementMeta>();
		for (int i = 0; i < map.getLength(); i++) {
			XSObject item = map.item(i);
			if(item instanceof XSComplexTypeDefinition){
				ret.add(processComplexType((XSComplexTypeDefinition)item, depth));
			}else if(item instanceof XSSimpleTypeDefinition){
				processSimpleType((XSSimpleTypeDefinition)item, depth);
			}else if(item instanceof XSElementDeclaration){
				ret.add(processElement((XSElementDeclaration)item, depth));
			}
		}
		return ret;
	}

	private List<BaseMeta> processList(XSObjectList map, int depth){
		ArrayList<BaseMeta> ret = new ArrayList<BaseMeta>();
		for (int i = 0; i < map.getLength(); i++) {
			XSObject item = map.item(i);
			if(item instanceof XSComplexTypeDefinition){
				ret.add(processComplexType((XSComplexTypeDefinition)item, depth));
			}else if(item instanceof XSParticle){
				ret.addAll(processParticle((XSParticle)item, depth));
			}else if(item instanceof XSAttributeUse){
				ret.add(processAttribute((XSAttributeUse)item, depth));
			}
		}
		return ret;
	}

	private void processSimpleType(XSSimpleTypeDefinition item, int depth){
		if(debug) System.out.println(getPrefix(depth+1)+"SimpleType{" + item.getNamespace() + "}" + item.getName()+"["+item.getClass()+"]");
		//processParticle(item.getParticle(), indent);
	}
	private ElementMeta processComplexType(XSComplexTypeDefinition item, int depth){
		if(debug) System.out.println(getPrefix(depth)+"ComplexType{" + item.getNamespace() + "}" + item.getName()+"["+item.getClass()+"]");
		ElementMeta ret;
		String qname = "{" + item.getNamespace() + "}" + item.getName();
		boolean recursive = ctStack.contains(qname);
		ctStack.push(qname);
		try {
			ret = new ElementMeta();
			ret.setName(((item.getNamespace()==null || item.getNamespace().equals(defaultNS))?"":(item.getNamespace()+":"))+item.getName());
			List<ElementMeta> childs = ret.getChildElement();
			List<AttributeMeta> attrs = ret.getAttrData(); 
			List<BaseMeta> l = processList(item.getAttributeUses(), depth);
			for (BaseMeta b:l) {
				if (b instanceof AttributeMeta) {
					attrs.add((AttributeMeta)b);
				} else if (b instanceof ElementMeta) {
					childs.add((ElementMeta)b);
				}
			}

			//if recursive use return here
			if(recursive){
				return ret;
			}
			l = processParticle(item.getParticle(), depth);
			if(l==null) return ret;
			for (BaseMeta b:l) {
				if (b instanceof AttributeMeta) {
					attrs.add((AttributeMeta)b);
				} else if (b instanceof ElementMeta) {
					childs.add((ElementMeta)b);
				}
			}
		} finally {
			ctStack.pop();
		}

		return ret;
	}
	private List<BaseMeta> processParticle(XSParticle item, int depth){
		if(item == null){
			if(debug) System.out.println(getPrefix(depth+1)+"Particle{null}");
			return null;
		}
		List<BaseMeta> l = processTerm(item.getTerm(), depth+1);
		if(l.size() == 1){
			ElementMeta e = (ElementMeta)l.get(0);
			int maxOccur = item.getMaxOccurs();
			int minOccur = item.getMinOccurs();
			boolean unbound = item.getMaxOccursUnbounded();
			e.setIsRequired(minOccur>0);
			e.setMaxOccurs(BigInteger.valueOf(maxOccur));
			e.setMinOccurs(BigInteger.valueOf(minOccur));
			if(unbound) e.setMaxOccurs(BigInteger.valueOf(-1));
		}
		return l;
	}
	private List<BaseMeta> processTerm(XSTerm item, int depth){
		ArrayList<BaseMeta> ret = new ArrayList<BaseMeta>();
		if(debug) System.out.print(getPrefix(depth)+"Term{" + item.getNamespace() + "}" + item.getName()+"["+item.getClass()+"]");
		if(item instanceof XSModelGroup){
			short comp = ((XSModelGroup)item).getCompositor();
			if(debug) System.out.println(comp==XSModelGroup.COMPOSITOR_ALL?" *ALL* ":(comp==XSModelGroup.COMPOSITOR_CHOICE?" *CHOICE* ":" *SEQ* "));
			ret.addAll(processList(((XSModelGroup)item).getParticles(), depth));
			if(comp==XSModelGroup.COMPOSITOR_CHOICE) {
				for(BaseMeta i:ret) {
					((ElementMeta)i).setIsChoice(true);
				}
			}
		}else if(item instanceof XSElementDeclaration) {
			if(debug) System.out.println(" *ELEMENT*");
			ret.add(processElement((XSElementDeclaration)item, depth));
		}
		return ret;
	}
	private ElementMeta processElement(XSElementDeclaration item, int depth){
		if(debug) System.out.println(getPrefix(depth)+"Element{" + item.getNamespace() + "}" + item.getName()+"["+item.getClass()+"]");
		ElementMeta ret = null;
		//		if(indent>MAX_INDENT) {
		//			System.out.println("MMMMMMMMM Reached max depth, skipping the lower levels......");
		//			return;
		//		}
		XSTypeDefinition type = item.getTypeDefinition();
		if(type instanceof XSComplexTypeDefinition){
			ret = processComplexType((XSComplexTypeDefinition)type, depth);
		}else if(type instanceof XSSimpleTypeDefinition){
			processSimpleType((XSSimpleTypeDefinition)type, depth);
		} 
		if(ret == null) ret = new ElementMeta();
		ret.setName(((item.getNamespace()==null || item.getNamespace().equals(defaultNS))?"":(item.getNamespace()+":"))+item.getName());

		//processParticle(item.getParticle(), indent+1);
		return ret;
	}
	private AttributeMeta processAttribute(XSAttributeUse item, int depth){
		if(item == null){
			if(debug) System.out.println(getPrefix(depth+1)+"Attribute {null}");
			return null;
		}
		XSAttributeDeclaration 	attr = item.getAttrDeclaration();
		AttributeMeta ret = new AttributeMeta();
		ret.setName(((attr.getNamespace()==null || attr.getNamespace().equals(defaultNS))?"":(attr.getNamespace()+":"))+attr.getName());
		ret.setIsRequired(item.getRequired());
		if (attr.getConstraintType()==XSConstants.VC_DEFAULT) {
			ret.setDefaultValue(attr.getConstraintValue());
		} else if (attr.getConstraintType()!=XSConstants.VC_NONE) {
			ret.setIsFixed(true);
		}

		if(debug) System.out.print(getPrefix(depth+1)+"AttributeUse{" + item.getNamespace() + "}" + item.getName()+"["+item.getClass()+"]"
				+(item.getRequired()?",":"Required,")
				+(item.getConstraintType()==XSConstants.VC_NONE?"":((item.getConstraintType()==XSConstants.VC_DEFAULT?"default=":"fixed=")+item.getConstraintValue())));
		if(debug) System.out.println(", Attribute{" + attr.getNamespace() + "}" + attr.getName()+"["+attr.getClass()+"]"
				+("{"+attr.getTypeDefinition().getNamespace()+"}"+attr.getTypeDefinition().getName())
				+(attr.getConstraintType()==XSConstants.VC_NONE?"":((attr.getConstraintType()==XSConstants.VC_DEFAULT?"default=":"fixed=")+attr.getConstraintValue())));
		return ret;
	}




	public boolean handleError(DOMError error){
		short severity = error.getSeverity();
		if (severity == DOMError.SEVERITY_ERROR) {
			System.out.println("[xs-error]: "+error.getMessage());
		}

		if (severity == DOMError.SEVERITY_WARNING) {
			System.out.println("[xs-warning]: "+error.getMessage());
		}
		return true;

	}

}

/**
 * HISTORY: $Log: not supported by cvs2svn $
 * HISTORY: Revision 1.4  2008/10/20 20:46:15  linc
 * HISTORY: updated.
 * HISTORY:
 * HISTORY: Revision 1.3  2008/10/08 20:05:55  linc
 * HISTORY: speed up
 * HISTORY:
 * HISTORY: Revision 1.2  2008/10/08 18:54:42  linc
 * HISTORY: updated
 * HISTORY:
 * HISTORY: Revision 1.1  2008/09/30 17:30:41  linc
 * HISTORY: updated.
 * HISTORY:
 */
