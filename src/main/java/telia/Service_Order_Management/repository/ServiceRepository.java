package telia.Service_Order_Management.repository;

import org.springframework.stereotype.Repository;
import telia.Service_Order_Management.model.ServiceRecord;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class ServiceRepository {

    private final Map<String, ServiceRecord> serviceRecords = new HashMap<>();

    public ServiceRecord create(ServiceRecord serviceRecord) {
        if (serviceRecord == null || serviceRecord.getServiceId() == null) {
            throw new IllegalArgumentException("ServiceRecord and ServiceId cannot be null");
        }

        String serviceId = serviceRecord.getServiceId();
        if (serviceRecords.containsKey(serviceId)) {
            throw new IllegalArgumentException("ServiceId " + serviceId + " already exists");
        }

        serviceRecords.put(serviceId, serviceRecord);
        return serviceRecord;
    }

    public Optional<ServiceRecord> findById(String serviceId) {
        if (serviceId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(serviceRecords.get(serviceId));
    }

    public Collection<ServiceRecord> findAll() {
        return serviceRecords.values();
    }

    public ServiceRecord update(ServiceRecord serviceRecord) {
        if (serviceRecord == null || serviceRecord.getServiceId() == null) {
            throw new IllegalArgumentException("ServiceRecord and ServiceId cannot be null");
        }

        String serviceId = serviceRecord.getServiceId();
        if (!serviceRecords.containsKey(serviceId)) {
            throw new IllegalArgumentException("ServiceId " + serviceId + " not found");
        }

        serviceRecords.put(serviceId, serviceRecord);
        return serviceRecord;
    }

    public boolean deleteById(String serviceId) {
        if (serviceId == null) {
            return false;
        }
        return serviceRecords.remove(serviceId) != null;
    }

    public boolean existsById(String serviceId) {
        return serviceId != null && serviceRecords.containsKey(serviceId);
    }
}
