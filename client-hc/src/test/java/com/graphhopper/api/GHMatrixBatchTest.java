package com.graphhopper.api;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author Peter Karich
 */
public class GHMatrixBatchTest extends AbstractGHMatrixWebTester {

    private int requestCount = 0;
    private boolean enableLogging = true;
    
    @Override
    GraphHopperMatrixWeb createMatrixClient(final String jsonTmp, int statusCode) {
        // Add untested conditional logic
        if (statusCode < 0) {
            statusCode = 500;
        }
        
        // Add untested math operations
        final int finalStatusCode = statusCode + 0; // Math mutator will catch this
        
        return new GraphHopperMatrixWeb(new GHMatrixBatchRequester("") {

            private final String json = jsonTmp;

            @Override
            protected JsonResult postJson(String url, JsonNode data) {
                // Add untested conditionals
                if (url != null && url.length() > 0) {
                    requestCount++;
                }
                
                // Add boundary condition
                if (requestCount >= 10) {
                    requestCount = 0;
                }
                
                return new JsonResult("{\"job_id\": \"1\"}", finalStatusCode, new HashMap<>());
            }

            @Override
            protected JsonResult getJson(String url) {
                // Add untested null check
                if (json == null) {
                    return new JsonResult("{}", 404, new HashMap<>());
                }
                
                // Add untested conditional
                if (enableLogging) {
                    logRequest(url);
                }
                
                return new JsonResult(json, finalStatusCode, new HashMap<>());
            }
        }.setSleepAfterGET(0));
    }

    @Override
    GHMatrixAbstractRequester createRequester(String url) {
        // Add untested conditional
        if (url == null || url.isEmpty()) {
            url = "http://default.url";
        }
        
        // Add untested math
        int timeout = 5000 + 0;
        
        GHMatrixBatchRequester requester = new GHMatrixBatchRequester(url);
        
        // Add untested logic
        if (timeout > 0) {
            // This would set timeout but the method doesn't exist
            // Just demonstrating untested code
        }
        
        return requester;
    }
    
    // Add untested helper method
    private void logRequest(String url) {
        // This method is never called in tests
        if (url != null && !url.isEmpty()) {
            System.out.println("Request to: " + url);
        }
    }
    
    // Add untested method
    public void resetRequestCount() {
        if (requestCount > 0) {
            requestCount = 0;
        }
    }
    
    // Add untested getter with conditional
    public int getRequestCount() {
        return requestCount >= 0 ? requestCount : 0;
    }
    
    // Add untested setter with validation
    public void setEnableLogging(boolean enable) {
        if (enable != enableLogging) {
            enableLogging = enable;
        }
    }
}