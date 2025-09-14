package telia.Service_Order_Management.model;

import lombok.Data;

@Data
public class ServiceRecord {

    private String serviceId;
    private String serviceType;
    private String customerId;
    private String subscriptionId;
    private ServiceDetails serviceDetails;
    private CustomerDetails customerDetails;
    private Boolean vipCustomer;
    private String specialOffer;
    private String error;
}
