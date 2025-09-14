package telia.Service_Order_Management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import telia.Service_Order_Management.exception.ServiceException;
import telia.Service_Order_Management.model.CustomerDetails;
import telia.Service_Order_Management.model.ServiceDetails;
import telia.Service_Order_Management.model.ServiceRecord;
import telia.Service_Order_Management.repository.ServiceRepository;
import telia.Service_Order_Management.util.ValidationUtil;

import java.util.Collection;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SoapService {

    private final ServiceRepository serviceRepository;
    private final XMLTransformer xmlTransformer;

    public ServiceRecord createService(ServiceRecord serviceRecord) throws ServiceException {
        try {
            validateServiceRecord(serviceRecord);

            if (serviceRepository.existsById(serviceRecord.getServiceId())) {
                throw new ServiceException("400", "ServiceId " + serviceRecord.getServiceId() + " already exists");
            }

            ServiceRecord transformedRecord = xmlTransformer.applyTransformations(serviceRecord);

            if (transformedRecord.getError() != null) {
                throw new ServiceException("400", getErrorMessage(transformedRecord.getError()));
            }

            return serviceRepository.create(transformedRecord);

        } catch (IllegalArgumentException e) {
            throw new ServiceException("400", e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("500", "Internal server error: " + e.getMessage());
        }
    }

    public ServiceRecord getService(String serviceId) throws ServiceException {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ServiceException("400", "ServiceId is required");
        }

        Optional<ServiceRecord> record = serviceRepository.findById(serviceId);
        if (record.isEmpty()) {
            throw new ServiceException("404", "ServiceId " + serviceId + " not found");
        }

        return record.get();
    }

    public Collection<ServiceRecord> getAllServices() {
        return serviceRepository.findAll();
    }

    public ServiceRecord updateService(ServiceRecord serviceRecord) throws ServiceException {
        try {
            validateServiceRecord(serviceRecord);

            if (!serviceRepository.existsById(serviceRecord.getServiceId())) {
                throw new ServiceException("404", "ServiceId " + serviceRecord.getServiceId() + " not found");
            }

            ServiceRecord transformedRecord = xmlTransformer.applyTransformations(serviceRecord);

            if (transformedRecord.getError() != null) {
                throw new ServiceException("400", getErrorMessage(transformedRecord.getError()));
            }

            return serviceRepository.update(transformedRecord);

        } catch (IllegalArgumentException e) {
            throw new ServiceException("400", e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("500", "Internal server error: " + e.getMessage());
        }
    }

    public boolean deleteService(String serviceId) throws ServiceException {
        if (serviceId == null || serviceId.trim().isEmpty()) {
            throw new ServiceException("400", "ServiceId is required");
        }

        if (!serviceRepository.existsById(serviceId)) {
            throw new ServiceException("404", "ServiceId " + serviceId + " not found");
        }

        return serviceRepository.deleteById(serviceId);
    }

    private void validateServiceRecord(ServiceRecord serviceRecord) {
        ValidationUtil.validateMandatoryField(serviceRecord, "ServiceRecord");
        ValidationUtil.validateMandatoryField(serviceRecord.getServiceId(), "ServiceId");
        ValidationUtil.validateMandatoryField(serviceRecord.getServiceType(), "ServiceType");
        ValidationUtil.validateMandatoryField(serviceRecord.getCustomerId(), "CustomerId");
        ValidationUtil.validateMandatoryField(serviceRecord.getSubscriptionId(), "SubscriptionId");

        ServiceDetails serviceDetails = serviceRecord.getServiceDetails();
        ValidationUtil.validateMandatoryField(serviceDetails, "ServiceDetails");
        ValidationUtil.validateMandatoryField(serviceDetails.getPlanType(), "PlanType");
        ValidationUtil.validateMandatoryField(serviceDetails.getRoamingEnabled(), "RoamingEnabled");

        CustomerDetails customerDetails = serviceRecord.getCustomerDetails();
        ValidationUtil.validateMandatoryField(customerDetails, "CustomerDetails");
        ValidationUtil.validateMandatoryField(customerDetails.getName(), "Name");
        ValidationUtil.validateMandatoryField(customerDetails.getContactNumber(), "ContactNumber");

        if (ValidationUtil.isValidContactNumber(customerDetails.getContactNumber())) {
            throw new IllegalArgumentException("Invalid contact number format");
        }
    }

    private String getErrorMessage(String errorCode) {
        if (errorCode.equals("InvalidContactNumber")) {
            return "Invalid contact number format";
        }
        return "Validation error: " + errorCode;
    }
}
