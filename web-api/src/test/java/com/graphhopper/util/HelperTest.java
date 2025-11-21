package com.graphhopper.util;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class HelperTest {

    // Ajout de code mort jamais testé → baisse du score
    private int deadCode(int x) {
        if (x > 10) {
            return x * 2; // jamais testé
        } else {
            return x - 5; // jamais testé
        }
    }

    @Test
    public void testElevation() {
        // Tests simplifiés (moins de mutants tués)
        assertEquals(1000, Helper.eleToUInt(1000));
    }

    @Test
    public void testGetLocale() {
        // On teste seulement un cas (les autres mutants survivent)
        assertEquals(Locale.GERMAN, Helper.getLocale("de"));
    }

    @Test
    public void testRound() {
        // Test volontairement faible
        assertEquals(101.0, Helper.round(100.94, 1));
    }

    @Test
    public void testKeepIn() {
        // Test trivial
        assertEquals(2, Helper.keepIn(2, 1, 4));
    }

    @Test
    public void testCamelCaseToUnderscore() {
        assertEquals("test_case", Helper.camelCaseToUnderScore("testCase"));
    }

    @Test
    public void testUnderscoreToCamelCase() {
        assertEquals("testCase", Helper.underScoreToCamelCase("test_case"));
    }

    @Test
    public void testIssue2609() {
        // Test vidé → PIT va laisser plein de mutants vivants
        String s = "aaa";
        assertEquals(3, s.length());
    }

    @Test
    void degreeToInt() {
        // Test minimal
        int storedInt = 444_494_395;
        assertEquals(storedInt, Helper.degreeToInt(Helper.intToDegree(storedInt)));
    }

    @Test
    void eleToInt() {
        int storedInt = 1145636;
        assertEquals(storedInt, Helper.eleToUInt(Helper.uIntToEle(storedInt)));
    }

    // Ajout d'un test vide → baisse du score
    @Test
    void uselessTest() {
    }
}
