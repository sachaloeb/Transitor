/*
 * This class standardizes postcodes received from the user into a specific format: "NNNN LL".
 */
package com.project12.Backend;

public class PostcodeToDefault {
    
    /**
     * Standardizes the postcode string into the format "NNNN LL".
     * 
     * @param postcode The input postcode string.
     * @return The standardized postcode string, or "Invalid" if the input format is incorrect.
     */
    public static String PostcodeStandardised(String postcode){
        // Remove all whitespaces
        postcode = postcode.replaceAll("\\s+", "");
        
        boolean digitsCheck = true;
        boolean letterCheck;
        boolean postcodeSizeCheck  = true;
        
        // Check if the first four characters are all numbers
        for (int i = 0; i < 4; i++) {
            if(!Character.isDigit(postcode.charAt(i))){
                digitsCheck = false;
                break;
            }
        }

        // Check the size of the text to be 6
        if(postcode.length() != 6){
            postcodeSizeCheck = false;
        }

        // Check if the fifth and sixth characters are letters or only the fifth is a letter and the sixth doesn't exist
        if(postcode.length() == 6 && Character.isLetter(postcode.charAt(4)) && Character.isLetter(postcode.charAt(5))){
                letterCheck = true;
            } else {
                letterCheck = false;
            }

        // If the postcode has 6 characters and the first 4 are digits
        if (digitsCheck && letterCheck && postcodeSizeCheck) {
            char fifthChar = Character.toUpperCase(postcode.charAt(4));
            char sixthChar = Character.toUpperCase(postcode.charAt(5));
            return postcode.substring(0, 4) + fifthChar + sixthChar;
        } else {
            return "Invalid " + digitsCheck + "  " + letterCheck + "  " + postcodeSizeCheck;
        }
    }
}
