package telia.Service_Order_Management.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+\\d{11,15}$");

    public static boolean isValidContactNumber(String contactNumber) {
        if (contactNumber == null || contactNumber.trim().isEmpty()) {
            return true;
        }
        return !PHONE_PATTERN.matcher(contactNumber.trim()).matches();
    }

    public static void validateMandatoryField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is mandatory and cannot be null or empty");
        }
    }

    public static void validateMandatoryField(Boolean value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is mandatory and cannot be null");
        }
    }

    public static void validateMandatoryField(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " is mandatory and cannot be null");
        }
    }

    public static boolean isVipCustomer(String customerId) {
        return "123456789".equals(customerId);
    }

    public static boolean qualifiesForSpecialOffer(String planType, String dataLimit) {
        return "5G".equalsIgnoreCase(planType) && (dataLimit == null || dataLimit.trim().isEmpty());
    }

    public static boolean shouldRemoveRoaming(String country) {
        return country != null && !"Sweden".equalsIgnoreCase(country.trim());
    }
}
