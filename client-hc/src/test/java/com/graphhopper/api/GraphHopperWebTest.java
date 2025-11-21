package com.graphhopper.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.GHRequest;

import com.graphhopper.jackson.Jackson;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import com.graphhopper.util.JsonFeature;
import com.graphhopper.util.JsonFeatureCollection;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import com.github.javafaker.Faker;

import java.util.Locale;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.graphhopper.json.Statement.If;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class unit tests the class. For integration tests against a real server see RouteResourceClientHCTest.
 */
public class GraphHopperWebTest {

}
