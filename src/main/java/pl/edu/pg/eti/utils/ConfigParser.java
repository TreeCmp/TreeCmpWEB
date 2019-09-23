package pl.edu.pg.eti.utils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/////////////////////////////////////////////
//////// USED FOR UPDATING METRIC LIST//////
////////////////////////////////////////////
public class ConfigParser {

	private InputStream metricConfigFileStream;

	public ConfigParser() {
		
		availableRootedMetricsWithCmd = new HashMap<String, MetricProperties>();
		availableUnrootedMetricsWithCmd = new HashMap<String, MetricProperties>();
		availableMetricsFiles = new ArrayList<String>();
	}

	private Map<String, MetricProperties> availableRootedMetricsWithCmd;
	private Map<String, MetricProperties> availableUnrootedMetricsWithCmd;
	private List<String> availableMetricsFiles;

	public Map<String, MetricProperties> getAvailableRootedMetricsWithCmd() {
		return availableRootedMetricsWithCmd;
	}
	public Map<String, MetricProperties> getAvailableUnrootedMetricsWithCmd() {
		return availableUnrootedMetricsWithCmd;
	}
	public List<String> getAvailableMetricsFiles() { return availableMetricsFiles; }

	public void clearAndSetAvailableMetrics() {

		availableRootedMetricsWithCmd.clear();
		availableUnrootedMetricsWithCmd.clear();
		availableMetricsFiles.clear();;

		try {
			if (metricConfigFileStream == null) {
				return;
			}
			Document doc = readXml(metricConfigFileStream);

			NodeList nList = doc.getElementsByTagName("metric");

			for (int temp = 0; temp < nList.getLength(); temp++) {

				Node nNode = nList.item(temp);

				if (nNode.getNodeType() == Node.ELEMENT_NODE) {

					Element eElement = (Element) nNode;
					
					NodeList rooted = eElement.getElementsByTagName("rooted");
					/****** GETTING ROOTED METRICS IF THERE IS A NOTE "ROOTED" *******/
					if(rooted.getLength() != 0)
					{
						availableRootedMetricsWithCmd.put(eElement.getElementsByTagName("command_name")
								.item(0).getTextContent(), new MetricProperties(
								String.format(" %s", eElement.getElementsByTagName("name").item(0).getTextContent()),
								String.format(" %s", eElement.getElementsByTagName("fullname").item(0).getTextContent()),
                                String.format(" %s", eElement.getElementsByTagName("full_description").item(0).getTextContent())
                        ));
					}
					else	/****** GETTING UNROOTED METRICS *******/ {
                        availableUnrootedMetricsWithCmd.put(eElement.getElementsByTagName("command_name")
                                .item(0).getTextContent(), new MetricProperties(
								String.format(" %s", eElement.getElementsByTagName("name").item(0).getTextContent()),
                                String.format(" %s", eElement.getElementsByTagName("fullname").item(0).getTextContent()),
                                String.format(" %s", eElement.getElementsByTagName("full_description").item(0).getTextContent())
                        ));
                    }
					availableMetricsFiles.add(eElement.getElementsByTagName("unif_data").item(0).getTextContent());
					availableMetricsFiles.add(eElement.getElementsByTagName("yule_data").item(0).getTextContent());
				}
			}

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	// ///////////////////////////////////////////
	// /// METHOD USED FOR EXTRACTING XML DOCUMENT
	// //////////////////////////////////////////////
	private Document readXml(InputStream is) throws SAXException, IOException,
			ParserConfigurationException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setValidating(false);
		dbf.setIgnoringComments(false);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);

		DocumentBuilder db = null;
		db = dbf.newDocumentBuilder();
		db.setEntityResolver(new NullResolver());
		return db.parse(is);
	}

	public InputStream getMetricConfigFileStream() {
		return metricConfigFileStream;
	}

	public void setMetricConfigFileStream(InputStream metricConfigFileStream) {
		this.metricConfigFileStream = metricConfigFileStream;
	}

}

class NullResolver implements EntityResolver {
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		return new InputSource(new StringReader(""));
	}
}
