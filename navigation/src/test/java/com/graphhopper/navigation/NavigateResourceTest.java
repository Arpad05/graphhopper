package com.graphhopper.navigation;

import org.junit.jupiter.api.Test;

import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.util.TranslationMap;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class NavigateResourceTest {


    @Test
    public void voiceInstructionsTest() {

        List<Double> bearings = NavigateResource.getBearing("");
        assertEquals(0, bearings.size());
        assertEquals(Collections.EMPTY_LIST, bearings);

        bearings = NavigateResource.getBearing("100,1");
        assertEquals(1, bearings.size());
        assertEquals(100, bearings.get(0), .1);

        bearings = NavigateResource.getBearing(";100,1;;");
        assertEquals(4, bearings.size());
        assertEquals(100, bearings.get(1), .1);
    }

    // 1
    @Test
    void doGet_rejectsNonPolyline6() {
        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap tr = mock(TranslationMap.class);
        NavigateResource r = new NavigateResource(hopper, tr, new GraphHopperConfig());

        HttpServletRequest req = mock(HttpServletRequest.class);
        UriInfo uri = mock(UriInfo.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            r.doGet(req, uri, rc,
                /* steps */ true,
                /* voice_instructions */ true,
                /* banner_instructions */ true,
                /* roundabout_exits */ true,
                /* voice_units */ "metric",
                /* overview */ "simplified",
                /* geometries */ "polyline",  // <- invalide
                /* bearings */ "",
                /* language */ "en",
                /* profile */ "driving")
        );
        assertTrue(ex.getMessage().contains("polyline6"));
    }

    // 2 
    @Test
    void doGet_bearingsWaypointsMismatch_throws() {
        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap tr = mock(TranslationMap.class);
        NavigateResource r = new NavigateResource(hopper, tr, new GraphHopperConfig());
    
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(
            "/navigate/directions/v5/gh/driving/-73.6,45.5;-73.7,45.6"
        );
    
        UriInfo uri = mock(UriInfo.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);
    
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            r.doGet(req, uri, rc,
                true,  // steps
                true,  // voice_instructions
                true,  // banner_instructions
                true,  // roundabout_exits
                "metric",
                "simplified",
                "polyline6",
                "100,1", // <- 1 bearing seulement
                "en",
                "driving")
        );
        assertTrue(ex.getMessage().contains("bearings and waypoints"));
    }  

    // 3 
    @Test
    void doGet_incorrectUrlPrefix_throws() {
        GraphHopper hopper = mock(GraphHopper.class);
        TranslationMap tr = mock(TranslationMap.class);
        NavigateResource r = new NavigateResource(hopper, tr, new GraphHopperConfig());
    
        HttpServletRequest req = mock(HttpServletRequest.class);
        // Le profile passé à doGet est "driving", mais l'URL dit "walking"
        when(req.getRequestURI()).thenReturn(
            "/navigate/directions/v5/gh/walking/-73.6,45.5;-73.7,45.6"
        );
    
        UriInfo uri = mock(UriInfo.class);
        ContainerRequestContext rc = mock(ContainerRequestContext.class);
    
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            r.doGet(req, uri, rc,
                true, true, true, true,
                "metric",
                "simplified",
                "polyline6",
                "",     // pas de bearings → on évite d’autres erreurs
                "en",
                "driving") // <- profile différent de l'URL
        );
        assertTrue(ex.getMessage().contains("Incorrect URL"));
    }
}
