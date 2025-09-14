# Java SOAP Server Application - Implementation Guide

## Project Overview

This comprehensive Java SOAP server application implements all the required functionality including:

✅ **Basic Server Setup** - Spring Boot with embedded Tomcat  
✅ **SOAP Request Handling** - Spring Web Services endpoints  
✅ **CRUD Operations** - In-memory HashMap storage  
✅ **XML Transformations** - Dynamic modifications based on business rules  
✅ **Error Handling** - Custom exceptions and SOAP fault responses  
✅ **SOAP Response Generation** - Success and error responses  

## Architecture

The application follows a layered architecture:

```
┌─────────────────────────────────────────┐
│           SOAP Controller Layer         │ ← Handles SOAP requests/responses
├─────────────────────────────────────────┤
│           Business Service Layer        │ ← Validation & business logic
├─────────────────────────────────────────┤
│           Data Repository Layer         │ ← In-memory CRUD operations
├─────────────────────────────────────────┤
│           Transformation Layer          │ ← XML transformations
└─────────────────────────────────────────┘
```

## Key Components

### 1. Maven Dependencies (pom.xml)
- Spring Boot Web Services Starter
- WSDL4J for WSDL generation
- JAXB for XML binding
- Xalan for XML transformations

### 2. Main Application (TelecomSOAPServer.java)
- Spring Boot application with @EnableWs
- Configures MessageDispatcherServlet
- Exposes WSDL at `/ws/telecomService.wsdl`

### 3. Data Models
- **ServiceRecord.java** - Main data structure
- **ServiceDetails.java** - Plan and service information
- **CustomerDetails.java** - Customer information
- **Address.java** - Customer address

### 4. Repository Layer (ServiceRepository.java)
- In-memory HashMap storage
- Full CRUD operations
- Thread-safe operations

### 5. Business Logic (TelecomService.java)
- Validation of mandatory fields
- Business rule enforcement
- Exception handling

### 6. SOAP Controller (TelecomSOAPController.java)
- Handles all SOAP operations (Create, Read, Update, Delete)
- XML parsing and response generation
- Error handling and fault responses

### 7. XML Transformation (XMLTransformer.java)
Implements all transformation rules:
- **Rule 1**: Add VIPCustomer=true if CustomerId is 123456789
- **Rule 2**: Add SpecialOffer="ExtraData" if PlanType is 5G and DataLimit is missing
- **Rule 3**: Remove RoamingEnabled if Country is not Sweden
- **Rule 4**: Add Error="InvalidContactNumber" if ContactNumber is invalid

### 8. Validation (ValidationUtil.java)
- Phone number regex validation: `^\+\d{11,15}$`
- Mandatory field validation
- Business rule checks

## Running the Application

### Prerequisites
- Java 21 or higher
- Maven 3.6+

### Build and Run
```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

## SOAP Operations

### 1. Create Service
**Endpoint**: `/ws`  
**Action**: Create  
**Request**: See example SOAP request in requirements  
**Response**: Success or Error with appropriate status

### 2. Get Service
**Endpoint**: `/ws`  
**Action**: Get  
**Parameters**: ServiceId  
**Response**: Service data or Error

### 3. Update Service
**Endpoint**: `/ws`  
**Action**: Update  
**Request**: Similar to Create with updated data  
**Response**: Success or Error

### 4. Delete Service
**Endpoint**: `/ws`  
**Action**: Delete  
**Parameters**: ServiceId  
**Response**: Success or Error

### 5. Get All Services
**Endpoint**: `/ws`  
**Action**: GetAll  
**Response**: List of all services

## Testing

1. **Create new SOAP project** with WSDL: `http://localhost:8080/telecom/ws/telecomService.wsdl`
2. **Test Create operation** with the provided example request
3. **Verify transformations** are applied correctly
4. **Test error scenarios** (invalid phone numbers, missing fields)

## Example Requests

### Valid Create Request
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tel="test">
   <soapenv:Header/>
   <soapenv:Body>
      <tel:Create>
         <tel:ServiceId>001</tel:ServiceId>
         <tel:ServiceType>MobileDataService</tel:ServiceType>
         <tel:CustomerId>123456789</tel:CustomerId>
         <tel:SubscriptionId>987654321</tel:SubscriptionId>
         <tel:ServiceDetails>
            <tel:PlanType>4G</tel:PlanType>
            <tel:DataLimit>10GB</tel:DataLimit>
            <tel:RoamingEnabled>true</tel:RoamingEnabled>
         </tel:ServiceDetails>
         <tel:CustomerDetails>
            <tel:Name>John Doe</tel:Name>
            <tel:ContactNumber>+46701234567</tel:ContactNumber>
         </tel:CustomerDetails>
      </tel:Create>
   </soapenv:Body>
</soapenv:Envelope>
```

### Success Response
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Header/>
   <soapenv:Body>
      <Response>
         <Status>Success</Status>
         <Message>Service activated successfully</Message>
      </Response>
   </soapenv:Body>
</soapenv:Envelope>
```

### Error Response
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
   <soapenv:Header/>
   <soapenv:Body>
      <Response>
         <Status>Error</Status>
         <ErrorCode>400</ErrorCode>
         <ErrorMessage>Invalid contact number format</ErrorMessage>
      </Response>
   </soapenv:Body>
</soapenv:Envelope>
```

## Validation Rules

### Mandatory Fields
- ServiceId (String, Unique)
- ServiceType (String)
- CustomerId (String)  
- SubscriptionId (String)
- ServiceDetails.PlanType (String)
- ServiceDetails.RoamingEnabled (Boolean)
- CustomerDetails.Name (String)
- CustomerDetails.ContactNumber (String)

### Validation Logic
- **Contact Number**: Must match regex `^\+\d{11,15}$`
- **Service ID**: Must be unique across all records
- **All mandatory fields**: Cannot be null or empty

## Transformation Examples

### VIP Customer (CustomerId = 123456789)
Input: CustomerId = "123456789"  
Output: Adds `<tel:VIPCustomer>true</tel:VIPCustomer>`

### Special Offer (5G without DataLimit)
Input: PlanType = "5G", DataLimit = null  
Output: Adds `<tel:SpecialOffer>ExtraData</tel:SpecialOffer>`

### Remove Roaming (Country ≠ Sweden)
Input: Country = "Lithuania"  
Output: Removes `<tel:RoamingEnabled>` element

### Invalid Contact Number
Input: ContactNumber = "invalid"  
Output: Adds `<tel:Error>InvalidContactNumber</tel:Error>`

## Error Codes

| Code | Description |
|------|-------------|
| 400  | Bad Request - Validation errors, missing fields |
| 404  | Not Found - Service ID not found |
| 500  | Internal Server Error - Unexpected errors |

