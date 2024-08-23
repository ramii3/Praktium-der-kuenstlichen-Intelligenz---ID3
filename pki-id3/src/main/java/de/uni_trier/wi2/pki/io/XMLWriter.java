package de.uni_trier.wi2.pki.io;

import de.uni_trier.wi2.pki.tree.DecisionTreeNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Map;

/**
 * Serializes the decision tree in form of an XML structure.
 */
public class XMLWriter {

    /**
     * Serialize decision tree to specified path.
     *
     * @param path         the path to write to.
     * @param decisionTree the tree to serialize.
     * @throws IOException if something goes wrong.
     */
    public static void writeXML(String path, DecisionTreeNode decisionTree) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElement("DecisionTree");
        doc.appendChild(rootElement);

        // Füge den Baum zum Root-Element hinzu
        rootElement.appendChild(toXmlElement(doc,decisionTree));

        // XML in String konvertieren
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        convertXmlToCsv(writer.toString(),path);
        //System.out.println(writer.toString());
    }
    public static Element toXmlElement(Document document, DecisionTreeNode node) {
        // Entscheidungsknoten oder Blattknoten erstellen
        Element nodeElement = !node.isLeafNode() ? document.createElement("Node") : document.createElement("LeafNode");

        // Attributname und Klassifizierung setzen, wenn vorhanden
        if (node.isLeafNode()) {
            nodeElement.setAttribute("class", node.getNodeClass().toString());
        }
        else if (!node.isLeafNode()) {
            nodeElement.setAttribute("attribute", node.getName());
        }


        // Rekursiv für jeden Split
        for (Map.Entry<String, DecisionTreeNode> entry : node.getSplits().entrySet()) {
            Element ifElement = document.createElement("IF");
            ifElement.setAttribute("value", entry.getKey());

            DecisionTreeNode childNode = entry.getValue();
            ifElement.appendChild(toXmlElement(document,childNode));
            nodeElement.appendChild(ifElement);
        }

        return nodeElement;
    }
    public static void convertXmlToCsv(String xmlString, String csvPath) throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(csvPath));
        writer.write(xmlString);

        writer.close();



    }

    public static void main(String[] args) {
        String xmlString = "<records><record><field1>value1</field1><field2>value2</field2></record></records>";
        String csvFilePath = "output.csv";
        try {
            convertXmlToCsv(xmlString, csvFilePath);
            System.out.println("XML was successfully converted to CSV and written to " + csvFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
