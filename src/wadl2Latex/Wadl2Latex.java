package wadl2Latex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A Parser class which parses a wadl rest description to a simple latex document
 * 
 *
 */
public class Wadl2Latex {
	
	public static void printMethodNode(PrintWriter writer, Node node, String path){
		Element element = (Element)node;
		path = path.replace("{", "\\{");
		path = path.replace("}", "\\}");
		writer.println("\\");
		writer.println();
		writer.println("\\textbf{Method Name: }" + element.getAttribute("id"));
		if(element.getAttribute("name").equals("GET")){
			writer.println("\\begin{get}");
			writer.println("/"+path);
			writer.println("\\end{get}");
			writer.println();
		} else if(element.getAttribute("name").equals("POST")){
			writer.println("\\begin{post}");
			writer.println("/"+path);
			writer.println("\\end{post}");
			writer.println();
		}  else if(element.getAttribute("name").equals("DELETE")){
			writer.println("\\begin{delete}");
			writer.println("/"+path);
			writer.println("\\end{delete}");
			writer.println();
		} 
		NodeList requestResponse = node.getChildNodes();
		for(int i = 0; i<requestResponse.getLength();i++){
			if(requestResponse.item(i).getNodeType() == Node.ELEMENT_NODE){
				if(requestResponse.item(i).getNodeName().equals("request")){
					NodeList requestNodeList = requestResponse.item(i).getChildNodes();
					for(int k = 0; k<requestNodeList.getLength();k++){
						if(requestNodeList.item(k).getNodeName().equals("representation")||
								requestNodeList.item(k).getNodeName().equals("ns2:representation")){
							Element rEl = (Element)requestNodeList.item(k);
							writer.println("\\begin{request}");
							writer.println(rEl.getAttribute("element")+" as "
									+ (rEl.getAttribute("mediaType").equals("*/*")?"text/plain":rEl.getAttribute("mediaType")));
							writer.println("\\end{request}");
							writer.println();
						}
					}
				} else if(requestResponse.item(i).getNodeName().equals("response")){
					NodeList responseNodeList = requestResponse.item(i).getChildNodes();
					for(int k = 0; k<responseNodeList.getLength();k++){
						if(responseNodeList.item(k).getNodeName().equals("representation")||
								responseNodeList.item(k).getNodeName().equals("ns2:representation")){
							Element rEl = (Element)responseNodeList.item(k);
							writer.println("\\begin{response}");
							writer.println(rEl.getAttribute("element")+" as "
									+ (rEl.getAttribute("mediaType").equals("*/*")?"text/plain":rEl.getAttribute("mediaType")));
							writer.println("\\end{response}");
							writer.println();
						}
					}

					
				}
			} 
		}
		
	}
	
	public static void printParam(PrintWriter writer, Node node){
		Element element = (Element)node;
		
		writer.println("\\begin{parameter}");
		writer.println(element.getAttribute("name")+" as "+element.getAttribute("type"));
		writer.println("\\end{parameter}");
		writer.println();
	}
	
	public static void main(String[] args)
	{
		File inputWadl = new File(args[0]);
		
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(inputWadl);
		} catch (Exception e) {
			System.err.println("could not read File");
			return;
		}
		
		Node applicationNode = doc.getFirstChild();
		NodeList applictionChildsNode = applicationNode.getChildNodes();
		Node resourcesNode = null;
		for (int i = 0; i < applictionChildsNode.getLength(); i++) {
			if (applictionChildsNode.item(i).getNodeType() == Node.ELEMENT_NODE) {
				if(applictionChildsNode.item(i).getNodeName().equals("resources"))
					resourcesNode = applictionChildsNode.item(i);
			}
		}
		
		if(resourcesNode == null){
			System.err.println("no resources found");
			return;
		}
		
		
		try {
			PrintWriter writer = new PrintWriter(args[1], "UTF-8");
			

			writer.println("\\chapter{Documentation}");
			writer.println("");
			writer.println("\\section{Baseresource: }");
			writer.println(((Element)resourcesNode).getAttribute("base") );
			writer.println("");
			
			NodeList classResourceNodes = resourcesNode.getChildNodes();
			
			for (int i = 0; i < classResourceNodes.getLength(); i++) {
				if (classResourceNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
					if(classResourceNodes.item(i).getNodeName().equals("resource")){
						writer.println("\\section{"+((Element)classResourceNodes.item(i)).getAttribute("path")+"}");
						writer.println("");
						
						NodeList resourceNodes = classResourceNodes.item(i).getChildNodes();
						for (int u = 0; u < resourceNodes.getLength(); u++) {
							Node node = resourceNodes.item(u);

							if (node.getNodeType() == Node.ELEMENT_NODE) {
								if(node.getNodeName().equals("resource")){
									
									String path = ((Element)node).getAttribute("path");
									NodeList methodAndParameterList = node.getChildNodes();
									for(int k = 0; k < methodAndParameterList.getLength();k++){
										Node mapNode = methodAndParameterList.item(k);
										if(mapNode.getNodeType() == Node.ELEMENT_NODE){
											if(mapNode.getNodeName().equals("method"))
												Wadl2Latex.printMethodNode(writer, mapNode, path);
										} 
									}
									for(int k = 0; k < methodAndParameterList.getLength();k++){
										Node mapNode = methodAndParameterList.item(k);
										if(mapNode.getNodeType() == Node.ELEMENT_NODE){
											if(mapNode.getNodeName().equals("param"))
												Wadl2Latex.printParam(writer, mapNode);
										} 
									}
		
								} else if(node.getNodeName().equals("method"))
									Wadl2Latex.printMethodNode(writer, node, ((Element)resourcesNode).getAttribute("path"));
							}
						}
					} 
				}
			}
			
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

}
