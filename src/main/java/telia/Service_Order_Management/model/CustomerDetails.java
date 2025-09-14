package telia.Service_Order_Management.model;

import lombok.Data;

@Data
public class CustomerDetails {

    private String name;
    private Address address;
    private String contactNumber;
}
