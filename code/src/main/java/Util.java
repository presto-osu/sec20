/*
 * Util.java - part of the GATOR project
 *
 * Copyright (c) 2018 The Ohio State University
 *
 * This file is distributed under the terms described in LICENSE
 * in the root directory.
 */

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Util {
  public static void printGraph(Node root) {
    _print_graph_recursive(root, "");
  }

  private static void _print_graph_recursive(Node root, String prefix) {
    System.out.println(prefix + root);
    root.getSuccessors().forEach(node -> _print_graph_recursive(node, prefix + "\t"));
  }

  public static DiGraph genDomTree(CFG cfg) {
    NewLenTar lt = new NewLenTar(cfg);
    lt.findDominators();

    //    CFG domTree = new CFG();
    DiGraph domTree = new DiGraph();
    for (Node n : cfg.getNodes()) domTree.addNode(n.getId());

    domTree.setRoot(domTree.addNode("artificial entry"));
    for (Node n : cfg.getNodes()) {
      Node d_n = domTree.getNode(n.getId());
      Node d_d = domTree.getNode(lt.getDominator(n).getId());
      domTree.addEdge(d_d, d_n);
    }
    return domTree;
  }

  public static void dumpCFGToXML(CFG cfg, PrintStream ps) {
    dumpCFGToXML(cfg, ps, true);
  }

  public static void dumpCFGToXML(CFG cfg, PrintStream ps, boolean withLabel) {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document document = dBuilder.newDocument();
      Element root = document.createElement("graph");
      document.appendChild(root);

      Set<Node> visited = new HashSet<>();
      Element edges = document.createElement("edges");
      root.appendChild(edges);
      for (Edge e : cfg.getEdges()) {
        visited.add(e.getSource());
        visited.add(e.getTarget());
        Element edge = document.createElement("edge");
        Element src = document.createElement("src");
        src.appendChild(document.createTextNode(e.getSource().getId()));
        edge.appendChild(src);
        Element tgt = document.createElement("tgt");
        tgt.appendChild(document.createTextNode(e.getTarget().getId()));
        edge.appendChild(tgt);
        if (withLabel) {
          Element freq = document.createElement("freq");
          freq.appendChild(document.createTextNode(String.valueOf(cfg.getFreq(e))));
          edge.appendChild(freq);
        }
        edges.appendChild(edge);
      }

      Element nodes = document.createElement("nodes");
      root.appendChild(nodes);
      for (Node n : visited) {
        Element node = document.createElement("node");
        Element id =
            cfg.getRoot() == n ? document.createElement("root") : document.createElement("id");
        id.appendChild(document.createTextNode(n.getId()));
        node.appendChild(id);
        if (n.getName() != null) {
          Element name = document.createElement("name");
          name.appendChild(document.createTextNode(n.getName()));
          node.appendChild(name);
        }
        nodes.appendChild(node);
      }

      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(ps);
      transformer.transform(source, result);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }

  public static CFG readCFGFromXML(InputStream in) {
    return readCFGFromXML(in, false, 0);
  }

  public static CFG readCFGFromXML(InputStream in, int defaultFreq) {
    return readCFGFromXML(in, false, defaultFreq);
  }

  public static CFG readCFGFromXML(InputStream in, boolean withZeroEdge, int defaultFreq) {
    CFG cfg = new CFG();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(in);
      doc.getDocumentElement().normalize();
      org.w3c.dom.Node graph = doc.getElementsByTagName("graph").item(0);
      List<org.w3c.dom.Node> worklist = new LinkedList<>();
      worklist.add(graph);
      while (!worklist.isEmpty()) {
        org.w3c.dom.Node node = worklist.remove(0);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          org.w3c.dom.Node curNode = children.item(i);
          String nodeName = curNode.getNodeName();
          if (nodeName.equals("nodes") || nodeName.equals("edges")) {
            worklist.add(curNode);
          } else if (nodeName.equals("node")) {
            handleNode(cfg, curNode);
          } else if (nodeName.equals("edge")) {
            handleEdge(cfg, curNode, withZeroEdge, defaultFreq);
          }
        }
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return cfg;
  }

  private static void handleEdge(
      CFG cfg, org.w3c.dom.Node node, boolean withZeroEdge, int defaultFreq) {
    NodeList children = node.getChildNodes();
    String src = null;
    String tgt = null;
    long freq = defaultFreq;
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node curNode = children.item(i);
      String name = curNode.getNodeName();
      if (name.equals("src")) {
        src = curNode.getTextContent();
      } else if (name.equals("tgt")) {
        tgt = curNode.getTextContent();
      } else if (name.equals("freq")) {
        freq = Long.parseLong(curNode.getTextContent());
      }
    }
    if (freq > 0 || withZeroEdge) cfg.addEdge(cfg.addNode(src), cfg.addNode(tgt), freq);
  }

  private static void handleNode(DiGraph graph, org.w3c.dom.Node node) {
    NodeList children = node.getChildNodes();
    String id = null, name = null;
    boolean root = false;
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node curNode = children.item(i);
      if (curNode.getNodeName().equals("id")) {
        id = curNode.getTextContent();
      } else if (curNode.getNodeName().equals("root")) {
        id = curNode.getTextContent();
        root = true;
      } else if (curNode.getNodeName().equals("name")) {
        name = curNode.getTextContent();
      }
    }
    if (root) graph.setRoot(graph.addNode(id));
    if (name != null) {
      Node n = graph.addNode(id);
      n.setName(name);
      if (name.equals("_start_")) graph.setRoot(n); // trick for call graphs
    }
  }

  public static DiGraph readDiGraphFromXML(InputStream in) {
    DiGraph diGraph = new DiGraph();
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(in);
      doc.getDocumentElement().normalize();
      org.w3c.dom.Node graph = doc.getElementsByTagName("graph").item(0);
      List<org.w3c.dom.Node> worklist = new LinkedList<>();
      worklist.add(graph);
      while (!worklist.isEmpty()) {
        org.w3c.dom.Node node = worklist.remove(0);
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
          org.w3c.dom.Node curNode = children.item(i);
          String nodeName = curNode.getNodeName();
          if (nodeName.equals("nodes") || nodeName.equals("edges")) {
            worklist.add(curNode);
          } else if (nodeName.equals("node")) {
            handleNode(diGraph, curNode);
          } else if (nodeName.equals("edge")) {
            handleEdge(diGraph, curNode);
          }
        }
      }
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (SAXException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return diGraph;
  }

  private static void handleEdge(DiGraph graph, org.w3c.dom.Node node) {
    NodeList children = node.getChildNodes();
    String src = null;
    String tgt = null;
    for (int i = 0; i < children.getLength(); i++) {
      org.w3c.dom.Node curNode = children.item(i);
      String name = curNode.getNodeName();
      if (name.equals("src")) {
        src = curNode.getTextContent();
      } else if (name.equals("tgt")) {
        tgt = curNode.getTextContent();
      }
    }
    graph.addEdge(graph.addNode(src), graph.addNode(tgt));
  }

  public static void dumpDiGraphToXML(DiGraph graph, PrintStream ps) {
    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document document = dBuilder.newDocument();
      Element root = document.createElement("graph");
      document.appendChild(root);

      Element nodes = document.createElement("nodes");
      root.appendChild(nodes);
      for (Node n : graph.getNodes()) {
        Element node = document.createElement("node");
        nodes.appendChild(node);
        Element id =
            graph.getRoot() == n ? document.createElement("root") : document.createElement("id");
        id.appendChild(document.createTextNode(n.getId()));
        node.appendChild(id);

        Element name = document.createElement("name");
        name.appendChild(document.createTextNode(n.getName()));
        node.appendChild(name);
      }

      Element edges = document.createElement("edges");
      root.appendChild(edges);
      for (Edge e : graph.getEdges()) {
        Element edge = document.createElement("edge");
        Element src = document.createElement("src");
        src.appendChild(document.createTextNode(e.getSource().getId()));
        edge.appendChild(src);
        Element tgt = document.createElement("tgt");
        tgt.appendChild(document.createTextNode(e.getTarget().getId()));
        edge.appendChild(tgt);
        edges.appendChild(edge);
      }

      // write the content into xml file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      DOMSource source = new DOMSource(document);
      StreamResult result = new StreamResult(ps);
      transformer.transform(source, result);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerConfigurationException e) {
      e.printStackTrace();
    } catch (TransformerException e) {
      e.printStackTrace();
    }
  }
}
