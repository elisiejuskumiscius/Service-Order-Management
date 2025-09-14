package telia.Service_Order_Management.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class ServiceDetails {

    private String planType;
    private String dataLimit;
    private Boolean roamingEnabled;
    private List<String> additionalServices;

}
