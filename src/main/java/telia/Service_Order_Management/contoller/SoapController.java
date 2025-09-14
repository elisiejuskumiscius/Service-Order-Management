package telia.Service_Order_Management.contoller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import telia.Service_Order_Management.exception.ServiceException;
import telia.Service_Order_Management.model.Address;
import telia.Service_Order_Management.model.CustomerDetails;
import telia.Service_Order_Management.model.ServiceDetails;
import telia.Service_Order_Management.model.ServiceRecord;
import telia.Service_Order_Management.service.SoapService;
import telia.Service_Order_Management.service.XMLTransformer;

import java.util.Arrays;
import java.util.Collection;

@Endpoint
@RequiredArgsConstructor
public class SoapController {

    private final SoapService soapService;
    private final XMLTransformer xmlTransformer;

    private static final String NAMESPACE_URI = "test";

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Create")
    @ResponsePayload
    public Element handleCreateRequest(@RequestPayload Element request) {
        try {
            ServiceRecord serviceRecord = parseCreateRequest(request);
            Document requestDoc = request.getOwnerDocument();
            Document transformedDoc = xmlTransformer.transformXMLDocument(requestDoc);
            ServiceRecord createdRecord = soapService.createService(serviceRecord);

            return createSuccessResponse(request.getOwnerDocument(), "Service activated successfully");

        } catch (ServiceException e) {
            return createErrorResponse(request.getOwnerDocument(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            return createErrorResponse(request.getOwnerDocument(), "500", "Internal server error: " + e.getMessage());
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Get")
    @ResponsePayload
    public Element handleGetRequest(@RequestPayload Element request) {
        try {
            String serviceId = getElementValue(request, "ServiceId");

            if (serviceId == null || serviceId.trim().isEmpty()) {
                return createErrorResponse(request.getOwnerDocument(), "400", "ServiceId is required");
            }

            ServiceRecord record = soapService.getService(serviceId);
            return createGetSuccessResponse(request.getOwnerDocument(), record);

        } catch (ServiceException e) {
            return createErrorResponse(request.getOwnerDocument(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            return createErrorResponse(request.getOwnerDocument(), "500", "Internal server error: " + e.getMessage());
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Update")
    @ResponsePayload
    public Element handleUpdateRequest(@RequestPayload Element request) {
        try {
            ServiceRecord serviceRecord = parseUpdateRequest(request);
            ServiceRecord updatedRecord = soapService.updateService(serviceRecord);

            return createSuccessResponse(request.getOwnerDocument(),
                    "Service updated successfully");

        } catch (ServiceException e) {
            return createErrorResponse(request.getOwnerDocument(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            return createErrorResponse(request.getOwnerDocument(),
                    "500", "Internal server error: " + e.getMessage());
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Delete")
    @ResponsePayload
    public Element handleDeleteRequest(@RequestPayload Element request) {
        try {
            String serviceId = getElementValue(request, "ServiceId");

            if (serviceId == null || serviceId.trim().isEmpty()) {
                return createErrorResponse(request.getOwnerDocument(),
                        "400", "ServiceId is required");
            }

            boolean deleted = soapService.deleteService(serviceId);

            return createSuccessResponse(request.getOwnerDocument(),
                    "Service deleted successfully");

        } catch (ServiceException e) {
            return createErrorResponse(request.getOwnerDocument(), e.getErrorCode(), e.getErrorMessage());
        } catch (Exception e) {
            return createErrorResponse(request.getOwnerDocument(),
                    "500", "Internal server error: " + e.getMessage());
        }
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetAll")
    @ResponsePayload
    public Element handleGetAllRequest(@RequestPayload Element request) {
        try {
            Collection<ServiceRecord> records = soapService.getAllServices();
            return createGetAllSuccessResponse(request.getOwnerDocument(), records);

        } catch (Exception e) {
            return createErrorResponse(request.getOwnerDocument(),
                    "500", "Internal server error: " + e.getMessage());
        }
    }

    private ServiceRecord parseCreateRequest(Element request) {
        ServiceRecord record = new ServiceRecord();

        record.setServiceId(getElementValue(request, "ServiceId"));
        record.setServiceType(getElementValue(request, "ServiceType"));
        record.setCustomerId(getElementValue(request, "CustomerId"));
        record.setSubscriptionId(getElementValue(request, "SubscriptionId"));

        Element serviceDetailsElement = getChildElement(request, "ServiceDetails");
        if (serviceDetailsElement != null) {
            ServiceDetails serviceDetails = new ServiceDetails();
            serviceDetails.setPlanType(getElementValue(serviceDetailsElement, "PlanType"));
            serviceDetails.setDataLimit(getElementValue(serviceDetailsElement, "DataLimit"));

            String roamingStr = getElementValue(serviceDetailsElement, "RoamingEnabled");
            if (roamingStr != null) {
                serviceDetails.setRoamingEnabled(Boolean.parseBoolean(roamingStr));
            }

            Element additionalServicesElement = getChildElement(serviceDetailsElement, "AdditionalServices");
            if (additionalServicesElement != null) {
                NodeList serviceNodes = additionalServicesElement.getElementsByTagName("Service");
                if (serviceNodes.getLength() == 0) {
                    serviceNodes = additionalServicesElement.getElementsByTagNameNS("*", "Service");
                }

                String[] services = new String[serviceNodes.getLength()];
                for (int i = 0; i < serviceNodes.getLength(); i++) {
                    services[i] = serviceNodes.item(i).getTextContent();
                }
                serviceDetails.setAdditionalServices(Arrays.asList(services));
            }

            record.setServiceDetails(serviceDetails);
        }

        Element customerDetailsElement = getChildElement(request, "CustomerDetails");
        if (customerDetailsElement != null) {
            CustomerDetails customerDetails = new CustomerDetails();
            customerDetails.setName(getElementValue(customerDetailsElement, "Name"));
            customerDetails.setContactNumber(getElementValue(customerDetailsElement, "ContactNumber"));

            Element addressElement = getChildElement(customerDetailsElement, "Address");
            if (addressElement != null) {
                Address address = new Address();
                address.setStreet(getElementValue(addressElement, "Street"));
                address.setCity(getElementValue(addressElement, "City"));
                address.setPostalCode(getElementValue(addressElement, "PostalCode"));
                address.setCountry(getElementValue(addressElement, "Country"));
                customerDetails.setAddress(address);
            }

            record.setCustomerDetails(customerDetails);
        }

        return record;
    }

    private ServiceRecord parseUpdateRequest(Element request) {
        return parseCreateRequest(request);
    }

    private Element createSuccessResponse(Document document, String message) {
        Element response = document.createElement("Response");

        Element status = document.createElement("Status");
        status.setTextContent("Success");
        response.appendChild(status);

        Element messageElement = document.createElement("Message");
        messageElement.setTextContent(message);
        response.appendChild(messageElement);

        return response;
    }

    private Element createErrorResponse(Document document, String errorCode, String errorMessage) {
        Element response = document.createElement("Response");

        Element status = document.createElement("Status");
        status.setTextContent("Error");
        response.appendChild(status);

        Element errorCodeElement = document.createElement("ErrorCode");
        errorCodeElement.setTextContent(errorCode);
        response.appendChild(errorCodeElement);

        Element errorMessageElement = document.createElement("ErrorMessage");
        errorMessageElement.setTextContent(errorMessage);
        response.appendChild(errorMessageElement);

        return response;
    }

    private Element createGetSuccessResponse(Document document, ServiceRecord record) {
        Element response = document.createElement("Response");

        Element status = document.createElement("Status");
        status.setTextContent("Success");
        response.appendChild(status);

        Element serviceData = document.createElement("ServiceData");

        Element serviceId = document.createElement("ServiceId");
        serviceId.setTextContent(record.getServiceId());
        serviceData.appendChild(serviceId);

        Element serviceType = document.createElement("ServiceType");
        serviceType.setTextContent(record.getServiceType());
        serviceData.appendChild(serviceType);

        Element customerId = document.createElement("CustomerId");
        customerId.setTextContent(record.getCustomerId());
        serviceData.appendChild(customerId);

        response.appendChild(serviceData);

        return response;
    }

    private Element createGetAllSuccessResponse(Document document, Collection<ServiceRecord> records) {
        Element response = document.createElement("Response");

        Element status = document.createElement("Status");
        status.setTextContent("Success");
        response.appendChild(status);

        Element servicesElement = document.createElement("Services");

        for (ServiceRecord record : records) {
            Element serviceElement = document.createElement("Service");

            Element serviceId = document.createElement("ServiceId");
            serviceId.setTextContent(record.getServiceId());
            serviceElement.appendChild(serviceId);

            Element serviceType = document.createElement("ServiceType");
            serviceType.setTextContent(record.getServiceType());
            serviceElement.appendChild(serviceType);

            servicesElement.appendChild(serviceElement);
        }

        response.appendChild(servicesElement);

        return response;
    }

    private String getElementValue(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagNameNS("*", tagName);
        }

        if (nodes.getLength() > 0) {
            return nodes.item(0).getTextContent();
        }
        return null;
    }

    private Element getChildElement(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            nodes = parent.getElementsByTagNameNS("*", tagName);
        }

        if (nodes.getLength() > 0) {
            return (Element) nodes.item(0);
        }
        return null;
    }

}
