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
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.graphhopper.json.Statement.If;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This class unit tests the class. For integration tests against a real server see RouteResourceClientHCTest.
 */
public class GraphHopperWebTest {

    @ParameterizedTest(name = "POST={0}")
    @ValueSource(booleans = {true, false})
    public void testGetClientForRequest(boolean usePost) {
        GraphHopperWeb gh = new GraphHopperWeb(null).setPostRequest(usePost);
        GHRequest req = new GHRequest(new GHPoint(42.509225, 1.534728), new GHPoint(42.512602, 1.551558)).
                setProfile("car");
        req.putHint(GraphHopperWeb.TIMEOUT, 5);

        assertEquals(5, gh.getClientForRequest(req).connectTimeoutMillis());
    }

    @Test
    public void profileIncludedAsGiven() {
        GraphHopperWeb hopper = new GraphHopperWeb("https://localhost:8000/route");
        // no vehicle -> no vehicle
        assertEquals("https://localhost:8000/route?profile=&type=json&instructions=true&points_encoded=true&points_encoded_multiplier=1000000" +
                        "&calc_points=true&algorithm=&locale=en_US&elevation=false&optimize=false",
                hopper.createGetRequest(new GHRequest()).url().toString());

        // vehicle given -> vehicle used in url
        assertEquals("https://localhost:8000/route?profile=my_car&type=json&instructions=true&points_encoded=true&points_encoded_multiplier=1000000" +
                        "&calc_points=true&algorithm=&locale=en_US&elevation=false&optimize=false",
                hopper.createGetRequest(new GHRequest().setProfile("my_car")).url().toString());
    }

    @Test
    public void headings() {
        GraphHopperWeb hopper = new GraphHopperWeb("http://localhost:8080/route");
        GHRequest req = new GHRequest(new GHPoint(42.509225, 1.534728), new GHPoint(42.512602, 1.551558)).
                setHeadings(Arrays.asList(10.0, 90.0)).
                setProfile("car");
        assertEquals("http://localhost:8080/route?profile=car&point=42.509225,1.534728&point=42.512602,1.551558&type=json&instructions=true&points_encoded=true&points_encoded_multiplier=1000000" +
                "&calc_points=true&algorithm=&locale=en_US&elevation=false&optimize=false&heading=10.0&heading=90.0", hopper.createGetRequest(req).url().toString());
    }

    @Test
    public void customModel() throws JsonProcessingException {
        GraphHopperWeb client = new GraphHopperWeb("http://localhost:8080/route");
        JsonFeatureCollection areas = new JsonFeatureCollection();
        Coordinate[] area_1_coordinates = new Coordinate[]{
                new Coordinate(48.019324184801185, 11.28021240234375),
                new Coordinate(48.019324184801185, 11.53564453125),
                new Coordinate(48.11843396091691, 11.53564453125),
                new Coordinate(48.11843396091691, 11.28021240234375),
                new Coordinate(48.019324184801185, 11.28021240234375),
        };
        Coordinate[] area_2_coordinates = new Coordinate[]{
                new Coordinate(48.15509285476017, 11.53289794921875),
                new Coordinate(48.15509285476017, 11.8212890625),
                new Coordinate(48.281365151571755, 11.8212890625),
                new Coordinate(48.281365151571755, 11.53289794921875),
                new Coordinate(48.15509285476017, 11.53289794921875),
        };
        areas.getFeatures().add(new JsonFeature("area_1",
                "Feature",
                null,
                new GeometryFactory().createPolygon(area_1_coordinates),
                new HashMap<>()));
        areas.getFeatures().add(new JsonFeature("area_2",
                "Feature",
                null,
                new GeometryFactory().createPolygon(area_2_coordinates),
                new HashMap<>()));
        CustomModel customModel = new CustomModel()
                .addToSpeed(If("road_class == MOTORWAY", Statement.Op.LIMIT, "80"))
                .addToPriority(If("surface == DIRT", Statement.Op.MULTIPLY, "0.7"))
                .addToPriority(If("surface == SAND", Statement.Op.MULTIPLY, "0.6"))
                .setDistanceInfluence(69d)
                .setHeadingPenalty(22)
                .setAreas(areas);
        GHRequest req = new GHRequest(new GHPoint(42.509225, 1.534728), new GHPoint(42.512602, 1.551558))
                .setCustomModel(customModel)
                .setProfile("car");

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> client.createGetRequest(req));
        assertEquals("Custom models cannot be used for GET requests. Use setPostRequest(true)", e.getMessage());

        ObjectNode postRequest = client.requestToJson(req);
        JsonNode customModelJson = postRequest.get("custom_model");
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        JsonNode expected = objectMapper.readTree("{\"distance_influence\":69.0,\"heading_penalty\":22.0,\"areas\":{" +
                "\"type\":\"FeatureCollection\",\"features\":[" +
                "{\"id\":\"area_1\",\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[48.019324184801185,11.28021240234375],[48.019324184801185,11.53564453125],[48.11843396091691,11.53564453125],[48.11843396091691,11.28021240234375],[48.019324184801185,11.28021240234375]]]},\"properties\":{}}," +
                "{\"id\":\"area_2\",\"type\":\"Feature\",\"geometry\":{\"type\":\"Polygon\",\"coordinates\":[[[48.15509285476017,11.53289794921875],[48.15509285476017,11.8212890625],[48.281365151571755,11.8212890625],[48.281365151571755,11.53289794921875],[48.15509285476017,11.53289794921875]]]},\"properties\":{}}]}," +
                "\"priority\":[{\"if\":\"surface == DIRT\",\"multiply_by\":\"0.7\"},{\"if\":\"surface == SAND\",\"multiply_by\":\"0.6\"}]," +
                "\"speed\":[{\"if\":\"road_class == MOTORWAY\",\"limit_to\":\"80\"}]}");
        assertEquals(expected, objectMapper.valueToTree(customModelJson));

        CustomModel cm = objectMapper.readValue("{\"distance_influence\":null}", CustomModel.class);
        assertNull(cm.getDistanceInfluence());
    }

    // 4
    @Test
    void testCreatRequest_v1() {
        GraphHopperWeb gh = new GraphHopperWeb("https://localhost:8000/route");
        GHRequest requete = new GHRequest();
        requete.addPoint(new GHPoint(47.390182, 18.976170));
        requete.addPoint(new GHPoint(42.390182, 17.976170));
        requete.setLocale("fr");

        String url = gh.createGetRequest(requete).url().toString();

        assertTrue(url.contains("locale=fr"));
    }

    // 5
    @Test
    void testCreatRequest_v2() {
        GraphHopperWeb gh = new GraphHopperWeb("https://localhost:8000/route");
        GHRequest requete = new GHRequest();
        requete.addPoint(new GHPoint(42.509225, 1.534728));
        requete.addPoint(new GHPoint(42.512602, 1.551558));
        requete.setLocale("fr-CA");
        requete.setProfile("my profile"); // l’espace sera encodé en + ou %20

        String url = gh.createGetRequest(requete).url().toString();
        assertTrue(url.contains("locale=fr-CA"));
        assertTrue(url.contains("profile=my+profile") || url.contains("profile=my%20profile"));
    }

    // 6 
    @Test
    public void TestRequeteJSon() {
        GraphHopperWeb gh = new GraphHopperWeb("http://localhost:8000/route");
        GHRequest req = new GHRequest(new GHPoint(42.509225, 1.534728), new GHPoint(42.512602, 1.551558))
                .setProfile("auto");
        JsonNode json = gh.requestToJson(req);

        assertNotNull(json);
        assertTrue(json.has("profile"), "le profile devrait être présent");
        assertEquals("auto", json.get("profile").asText());

        assertTrue(json.has("points"), "les repères devraiet etre présent");
        JsonNode points = json.get("points");
        assertTrue(points.isArray());
        assertEquals(2, points.size());

        double lon0 = points.get(0).get(0).asDouble();
        double lat0 = points.get(0).get(1).asDouble();
        double lon1 = points.get(1).get(0).asDouble();
        double lat1 = points.get(1).get(1).asDouble();

        double eps = 1e-6;
        assertEquals(42.509225, lat0, eps);
        assertEquals(1.534728, lon0, eps);
        assertEquals(42.512602, lat1, eps);
        assertEquals(1.551558, lon1, eps);
    }

    // 7
    @org.junit.jupiter.api.extension.RegisterExtension
    static com.github.tomakehurst.wiremock.junit5.WireMockExtension wm = com.github.tomakehurst.wiremock.junit5.WireMockExtension.newInstance().options(com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig().port(8989)).build();

    @Test
    void testRouting() {
        com.github.tomakehurst.wiremock.client.WireMock.stubFor(
            com.github.tomakehurst.wiremock.client.WireMock.get(
                com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo("/route"))
                .withQueryParam("type", com.github.tomakehurst.wiremock.client.WireMock.equalTo("json"))
                .willReturn(com.github.tomakehurst.wiremock.client.WireMock.aResponse()
                    .withStatus(200)
                    .withBody("{\"paths\":[{\"distance\":1234.5}]}"))
    );

    given().baseUri("http://localhost:8989")
        .queryParam("point","48.858844,2.294351")
        .queryParam("point","48.853,2.349")
        .queryParam("type","json")
        .when().get("/route")
        .then().statusCode(200)
        .body("paths", not(empty()))
        .body("paths[0].distance", greaterThan(0.0f));
    }
}
