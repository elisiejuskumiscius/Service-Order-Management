package telia.Service_Order_Management.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import telia.Service_Order_Management.model.ServiceRecord;
import telia.Service_Order_Management.util.ValidationUtil;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

@Slf4j
@Component
public class XMLTransformer {

    public ServiceRecord applyTransformations(ServiceRecord serviceRecord) {
        if (serviceRecord == null) {
            return null;
        }

        if (ValidationUtil.isVipCustomer(serviceRecord.getCustomerId())) {
            serviceRecord.setVipCustomer(true);
        }

        if (serviceRecord.getServiceDetails() != null) {
            String planType = serviceRecord.getServiceDetails().getPlanType();
            String dataLimit = serviceRecord.getServiceDetails().getDataLimit();

            if (ValidationUtil.qualifiesForSpecialOffer(planType, dataLimit)) {
                serviceRecord.setSpecialOffer("ExtraData");
            }
        }

        if (serviceRecord.getCustomerDetails() != null &&
                serviceRecord.getCustomerDetails().getAddress() != null &&
                serviceRecord.getServiceDetails() != null) {

            String country = serviceRecord.getCustomerDetails().getAddress().getCountry();
            if (ValidationUtil.shouldRemoveRoaming(country)) {
                serviceRecord.getServiceDetails().setRoamingEnabled(null);
            }
        }

        if (serviceRecord.getCustomerDetails() != null) {
            String contactNumber = serviceRecord.getCustomerDetails().getContactNumber();
            if (ValidationUtil.isValidContactNumber(contactNumber)) {
                serviceRecord.setError("InvalidContactNumber");
            }
        }

        return serviceRecord;
    }

    public Document transformXMLDocument(Document xmlDocument) throws Exception {
        if (xmlDocument == null) {
            return null;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document transformedDoc = builder.newDocument();

        Node importedNode = transformedDoc.importNode(xmlDocument.getDocumentElement(), true);
        transformedDoc.appendChild(importedNode);
        applyXMLTransformations(transformedDoc);

        return transformedDoc;
    }

    private void applyXMLTransformations(Document document) {
        try {
            Element root = document.getDocumentElement();

            String customerId = getElementValue(document, "CustomerId");
            String planType = getElementValue(document, "PlanType");
            String dataLimit = getElementValue(document, "DataLimit");
            String country = getElementValue(document, "Country");
            String contactNumber = getElementValue(document, "ContactNumber");

            NodeList createNodes = document.getElementsByTagName("Create");
            if (createNodes.getLength() == 0) {
                createNodes = document.getElementsByTagNameNS("*", "Create");
            }

            if (createNodes.getLength() > 0) {
                Element createElement = (Element) createNodes.item(0);

                if (ValidationUtil.isVipCustomer(customerId)) {
                    Element vipElement = document.createElementNS("test", "tel:VIPCustomer");
                    vipElement.setTextContent("true");
                    createElement.appendChild(vipElement);
                }

                if (ValidationUtil.qualifiesForSpecialOffer(planType, dataLimit)) {
                    Element specialOfferElement = document.createElementNS("test", "tel:SpecialOffer");
                    specialOfferElement.setTextContent("ExtraData");
                    createElement.appendChild(specialOfferElement);
                }

                if (ValidationUtil.shouldRemoveRoaming(country)) {
                    removeElementsByName(document, "RoamingEnabled");
                }

                if (ValidationUtil.isValidContactNumber(contactNumber)) {
                    Element errorElement = document.createElementNS("test", "tel:Error");
                    errorElement.setTextContent("InvalidContactNumber");
                    createElement.appendChild(errorElement);
                }
            }
        } catch (Exception e) {
            log.info("Error during XML transformation: {}", e.getMessage());
        }
    }

    private String getElementValue(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            nodes = document.getElementsByTagNameNS("*", tagName);
        }

        if (nodes.getLength() > 0) {
            Element element = (Element) nodes.item(0);
            return element.getTextContent();
        }
        return null;
    }

    private void removeElementsByName(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            // Try with namespace
            nodes = document.getElementsByTagNameNS("*", tagName);
        }

        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            Node node = nodes.item(i);
            if (node.getParentNode() != null) {
                node.getParentNode().removeChild(node);
            }
        }
    }
}
