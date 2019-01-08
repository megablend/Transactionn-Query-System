package com.nibss.tqs.util;

import java.util.Random;

/**
 * Created by Emor on 7/9/16.
 */
public class PasswordUtil {

    private static final String ALPHA_CAPS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUM = "0123456789";
    private static final String SPL_CHARS = "!@#$%^&*_=+-/";

    public  String generateRandomPassword() {
        StringBuilder builder = new StringBuilder();

        Random rand = new Random();
        rand.setSeed(System.currentTimeMillis());
        int i = 0;
        while (i < 9) {
            builder.append(SPL_CHARS.charAt(rand.nextInt(SPL_CHARS.length())));
            ++i;
            builder.append(NUM.charAt(rand.nextInt(NUM.length())));
            ++i;
            builder.append(ALPHA_CAPS.charAt(rand.nextInt(ALPHA_CAPS.length()))); //UPPER CASE CHARACTERS
            ++i;
            builder.append(ALPHA.charAt(rand.nextInt(ALPHA.length()))); //LOWER CASE CHARACTERS
            ++i;
        }
        String password = builder.toString();
        if (!isComplexPassword(password)) {
            return generateRandomPassword();
        }
        return password;
    }

    public boolean isComplexPassword(String password) {
        int numOfSpecial = 0;
        int numOfLetters = 0;
        int numOfUpperLetters = 0;
        int numOfLowerLetters = 0;
        int numOfDigits = 0;
        boolean isComplex;

        if (password.length() >= 8) {
            for (int i = 0; i < password.length(); i++) {
                char tempChar = password.charAt(i);
                if (SPL_CHARS.contains("" + tempChar)) {
                    numOfSpecial++;
                }
                if (Character.isDigit(tempChar)) {
                    numOfDigits++;
                }
                if (Character.isLetter(tempChar)) {
                    numOfLetters++;
                }
                if (Character.isUpperCase(tempChar)) {
                    numOfUpperLetters++;
                }
                if (Character.isLowerCase(tempChar)) {
                    numOfLowerLetters++;
                }
            }
            isComplex = numOfLetters > 0
                    && numOfUpperLetters > 0 && numOfLowerLetters > 0 && numOfDigits > 0
                    && numOfSpecial > 0;
        } else {
            isComplex = false;
        }
        return isComplex;
    }
}
