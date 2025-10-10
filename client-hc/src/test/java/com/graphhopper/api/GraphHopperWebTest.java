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

        // locale peut apparaître en "fr_CA" (underscore) ou "fr-CA" (tiret)
        assertTrue(
            url.contains("locale=fr_CA") || url.contains("locale=fr-CA"),
            "le locale devrait être dans l'URL (fr_CA ou fr-CA)"
        );

        // l’espace dans le profile peut être encodé en '+' ou '%20'
        assertTrue(
            url.contains("profile=my+profile") || url.contains("profile=my%20profile"),
            "le profile devrait être dans l'URL"
        );
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
    @Test
    void testrequeteFaker() {
        Faker faker = new Faker(new Locale("fr"));

        double lat = Double.parseDouble(faker.address().latitude());
        double lon = Double.parseDouble(faker.address().longitude());
        String city = faker.address().cityName();

        assertTrue(lat >= -90 && lat <= 90, "Latitude incorrect");
        assertTrue(lon >= -180 && lon <= 180, "Longitude incorrect");
        assertNotNull(city, "Ville ne doit pas être de nom vide.");

        System.out.println("Latitude: " + lat + ", Longitude: " + lon + ", Ville: " + city);
    }

    // 8 (Pour détecter des nouveaux mutants)
    @Test
    void testRequestToJson_includesOptionalArrays_whenNonEmpty() {
        GraphHopperWeb gh = new GraphHopperWeb("http://localhost:8080/route");
        GHRequest req = new GHRequest(new GHPoint(42.0, 1.0), new GHPoint(43.0, 2.0))
                .setProfile("car");
    
        // Rendre non vides toutes les listes optionnelles
        req.setPointHints(Arrays.asList("ph1", "ph2"));
        req.setHeadings(Arrays.asList(10.0, 90.0));
        req.setCurbsides(Arrays.asList("left", "right"));
        req.setSnapPreventions(Arrays.asList("ferry"));
        req.setPathDetails(Arrays.asList("road_class", "surface"));
    
        JsonNode json = gh.requestToJson(req);
    
        // Chaque tableau doit être présent et non vide
        assertTrue(json.has("point_hints") && json.get("point_hints").isArray() && json.get("point_hints").size() == 2);
        assertTrue(json.has("headings") && json.get("headings").isArray() && json.get("headings").size() == 2);
        assertTrue(json.has("curbsides") && json.get("curbsides").isArray() && json.get("curbsides").size() == 2);
        assertTrue(json.has("snap_preventions") && json.get("snap_preventions").isArray() && json.get("snap_preventions").size() == 1);
        assertTrue(json.has("details") && json.get("details").isArray() && json.get("details").size() == 2);
    
        // Quelques valeurs pour s'assurer que le contenu n'est pas vide
        assertEquals("ph1", json.get("point_hints").get(0).asText());
        assertEquals("left", json.get("curbsides").get(0).asText());
        assertEquals("road_class", json.get("details").get(0).asText());
    }

    // 9 (Pour détecter des nouveaux mutants)
    @Test
    void testCreateGetRequest_instructionsTrueCalcPointsFalse_throws() {
        GraphHopperWeb gh = new GraphHopperWeb("https://localhost:8000/route");
        GHRequest req = new GHRequest(new GHPoint(48.0, 2.0), new GHPoint(48.1, 2.1))
            .setProfile("car");
    
        // instructions=true et calc_points=false ➜ doit lancer IllegalStateException (ligne 310)
        req.getHints().putObject(com.graphhopper.util.Parameters.Routing.INSTRUCTIONS, true);
        req.getHints().putObject(com.graphhopper.util.Parameters.Routing.CALC_POINTS, false);
    
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> gh.createGetRequest(req));
        assertTrue(ex.getMessage().contains("Cannot calculate instructions without points"));
    }
}
