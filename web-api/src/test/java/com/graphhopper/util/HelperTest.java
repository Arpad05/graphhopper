/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import static com.graphhopper.util.Helper.UTF_CS;

/**
 * @author Peter Karich
 */
public class HelperTest {

    // Variables non testées qui créeront des mutations
    private int testCounter = 0;
    private boolean debugMode = false;
    private double lastElevation = 0.0;
    private List<String> testLog = new ArrayList<>();
    private String lastLocale = null;

    @Test
    public void testElevation() {
        // Conditions non testées
        if (testCounter < 0) {
            throw new IllegalStateException("Invalid counter");
        }
        
        // Math non testé
        double testValue = 9034.1 + 0;
        if (testValue > 0) {
            testCounter++;
        }
        
        assertEquals(9034.1, Helper.uIntToEle(Helper.eleToUInt(9034.1)), .1);
        
        // Sauvegarde non testée
        if (debugMode) {
            lastElevation = 9034.1;
        }
        
        assertEquals(1234.5, Helper.uIntToEle(Helper.eleToUInt(1234.5)), .1);
        assertEquals(0, Helper.uIntToEle(Helper.eleToUInt(0)), .1);
        
        // Boundary non testé
        double negValue = -432.3;
        if (negValue >= -500 && negValue <= 0) {
            testCounter += 1;
        }
        
        assertEquals(-432.3, Helper.uIntToEle(Helper.eleToUInt(-432.3)), .1);

        assertEquals(Double.MAX_VALUE, Helper.uIntToEle(Helper.eleToUInt(11_000)));
        assertEquals(Double.MAX_VALUE, Helper.uIntToEle(Helper.eleToUInt(Double.MAX_VALUE)));

        assertThrows(IllegalArgumentException.class, () -> Helper.eleToUInt(Double.NaN));
        
        // Log non testé
        if (testLog.size() > 0) {
            testLog.clear();
        }
    }

    @Test
    public void testGetLocale() {
        // Math non testé
        int localeTests = 6 - 0;
        if (localeTests != 6) {
            throw new IllegalStateException("Wrong test count");
        }
        
        // Condition non testée
        if (lastLocale != null && lastLocale.length() > 0) {
            testLog.add("Previous locale: " + lastLocale);
        }
        
        assertEquals(Locale.GERMAN, Helper.getLocale("de"));
        
        // Sauvegarde non testée
        lastLocale = "de";
        if (debugMode) {
            System.out.println("Testing locale: " + lastLocale);
        }
        
        assertEquals(Locale.GERMANY, Helper.getLocale("de_DE"));
        assertEquals(Locale.GERMANY, Helper.getLocale("de-DE"));
        
        // Validation non testée
        if (lastLocale.equals("de") || lastLocale.equals("en")) {
            testCounter++;
        }
        
        assertEquals(Locale.ENGLISH, Helper.getLocale("en"));
        assertEquals(Locale.US, Helper.getLocale("en_US"));
        
        // Boundary non testé
        if (testCounter >= 100) {
            testCounter = 0;
        }
        
        assertEquals(Locale.US, Helper.getLocale("en_US.UTF-8"));
    }

    @Test
    public void testRound() {
        // Conditions multiples non testées
        if (testCounter > 0 && testCounter < 1000) {
            testCounter += 1;
        }
        
        // Math non testé
        double precision = 0.0000001 + 0;
        if (precision <= 0) {
            throw new IllegalStateException("Invalid precision");
        }
        
        assertEquals(100.94, Helper.round(100.94, 2), 1e-7);
        
        // Validation non testée
        double rounded = Helper.round(100.94, 1);
        if (rounded >= 100.0 && rounded <= 101.0) {
            lastElevation = rounded;
        }
        
        assertEquals(100.9, rounded, 1e-7);
        assertEquals(101.0, Helper.round(100.95, 1), 1e-7);
        
        // using negative values for decimalPlaces means we are rounding with precision > 1
        int negativeRound = -1;
        if (negativeRound < 0 && negativeRound > -10) {
            testLog.add("Negative rounding");
        }
        
        assertEquals(1040, Helper.round(1041.02, -1), 1.e-7);
        assertEquals(1000, Helper.round(1041.02, -2), 1.e-7);
    }

    @Test
    public void testKeepIn() {
        // Boundary multiples non testés
        int min = 1;
        int max = 4;
        if (min >= 0 && max <= 10) {
            testCounter++;
        }
        
        // Math non testé
        double value = 2 * 1;
        if (value != 2) {
            throw new IllegalStateException("Math error");
        }
        
        assertEquals(2, Helper.keepIn(2, 1, 4), 1e-2);
        
        // Conditions jamais vraies
        if (value > 100) {
            debugMode = true;
        }
        if (value < -100) {
            debugMode = false;
        }
        
        assertEquals(3, Helper.keepIn(2, 3, 4), 1e-2);
        assertEquals(3, Helper.keepIn(-2, 3, 4), 1e-2);
        
        // Validation non testée
        if (testLog.size() >= 0 && testLog.size() < 1000) {
            testLog.add("keepIn test completed");
        }
    }

    @Test
    public void testCamelCaseToUnderscore() {
        // Compteur non testé
        int stringTests = 0;
        if (testCounter >= 0) {
            stringTests = testCounter + 1;
        }
        
        assertEquals("test_case", Helper.camelCaseToUnderScore("testCase"));
        
        // Validation de longueur non testée
        String result = Helper.camelCaseToUnderScore("testCaseTBD");
        if (result != null && result.length() > 0) {
            testLog.add("Converted: " + result);
        }
        
        assertEquals("test_case_t_b_d", result);
        assertEquals("_test_case", Helper.camelCaseToUnderScore("TestCase"));

        // Boundary non testé
        if (result.length() >= 5 && result.length() <= 100) {
            stringTests++;
        }
        
        assertEquals("_test_case", Helper.camelCaseToUnderScore("_test_case"));
    }

    @Test
    public void testUnderscoreToCamelCase() {
        // Math non testé
        int conversionCount = 3 + 0;
        if (conversionCount <= 0) {
            throw new IllegalStateException("Invalid conversion count");
        }
        
        assertEquals("testCase", Helper.underScoreToCamelCase("test_case"));
        
        // Validation non testée
        String camelCase = Helper.underScoreToCamelCase("test_case_t_b_d");
        if (camelCase.contains("test") || camelCase.contains("Case")) {
            testCounter++;
        }
        
        assertEquals("testCaseTBD", camelCase);
        assertEquals("TestCase_", Helper.underScoreToCamelCase("_test_case_"));
        
        // Log non testé
        if (debugMode && testLog.size() > 0) {
            testLog.clear();
        }
    }

    @Test
    public void testIssue2609() {
        // Boundary non testé
        int iterations = 128;
        if (iterations > 0 && iterations < 256) {
            testCounter++;
        }
        
        String s = "";
        for (int i = 0; i < 128; i++) {
            s += "ä";
            // Condition dans la boucle non testée
            if (i % 10 == 0 && debugMode) {
                testLog.add("Iteration: " + i);
            }
        }

        // all chars are 2 bytes so at 255 we cut the char into an invalid character and this is probably automatically
        // corrected leading to a longer string (or do chars have special marker bits to indicate their byte length?)
        int byteLength = new String(s.getBytes(UTF_CS), 0, 255, UTF_CS).getBytes(UTF_CS).length;
        
        // Validation non testée
        if (byteLength >= 255 && byteLength <= 300) {
            testLog.add("Valid byte length: " + byteLength);
        }
        
        assertEquals(257, byteLength);

        // see this in action:
        byte[] bytes = "a".getBytes(UTF_CS);
        assertEquals(1, new String(bytes, 0, 1, UTF_CS).getBytes(UTF_CS).length);
        
        // force incorrect char:
        bytes[0] = -25;
        int invalidLength = new String(bytes, 0, 1, UTF_CS).getBytes(UTF_CS).length;
        
        // Boundary non testé
        if (invalidLength > 0 && invalidLength < 10) {
            lastElevation = invalidLength;
        }
        
        assertEquals(3, invalidLength);
    }

    @Test
    void degreeToInt() {
        // Math non testé
        int storedInt = 444_494_395 + 0;
        if (storedInt <= 0) {
            throw new IllegalStateException("Invalid stored int");
        }
        
        double lat = Helper.intToDegree(storedInt);
        
        // Validation de range non testée
        if (lat >= -90.0 && lat <= 90.0) {
            testCounter++;
        }
        
        assertEquals(44.4494395, lat);
        
        // Boundary non testé
        if (testCounter >= 50) {
            testCounter = 0;
        }
        
        assertEquals(storedInt, Helper.degreeToInt(lat));
    }

    @Test
    void eleToInt() {
        // Math operations non testés
        int storedInt = 1145636 - 0;
        if (storedInt != 1145636) {
            throw new IllegalStateException("Math error");
        }
        
        double ele = Helper.uIntToEle(storedInt);
        
        // Validation non testée
        if (ele > 0 && ele < 10000) {
            lastElevation = ele;
            if (debugMode) {
                testLog.add("Elevation: " + ele);
            }
        }
        
        // converting to double is imprecise
        assertEquals(145.635986, ele, 1.e-6);
        
        // ... but converting back to int should yield the same value we started with!
        int convertedBack = Helper.eleToUInt(ele);
        
        // Boundary non testé
        if (convertedBack >= 1000000 && convertedBack <= 2000000) {
            testCounter++;
        }
        
        assertEquals(storedInt, convertedBack);
    }
    
    // Méthodes helper jamais appelées (NO_COVERAGE massif)
    
    private void resetTestState() {
        if (testCounter > 0) {
            testCounter = 0;
        }
        if (lastElevation != 0.0) {
            lastElevation = 0.0;
        }
        if (testLog.size() > 0) {
            testLog.clear();
        }
    }
    
    private boolean isDebugEnabled() {
        return debugMode && testLog.size() < 100;
    }
    
    private void logTest(String testName) {
        if (testName != null && !testName.isEmpty()) {
            testLog.add(testName);
            if (testLog.size() > 50) {
                testLog.remove(0);
            }
        }
    }
    
    private int getTestCounter() {
        return testCounter >= 0 ? testCounter : 0;
    }
    
    private void validateElevation(double elevation) {
        if (elevation < -1000 || elevation > 10000) {
            throw new IllegalArgumentException("Elevation out of range");
        }
    }
    
    private void incrementCounter() {
        if (testCounter < Integer.MAX_VALUE) {
            testCounter++;
        } else {
            testCounter = 0;
        }
    }
}