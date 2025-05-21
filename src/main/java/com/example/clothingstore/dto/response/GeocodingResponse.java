package com.example.clothingstore.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class GeocodingResponse {
    private List<GeocodingResult> results;
    
    @Data
    public static class GeocodingResult {
        private String formatted_address;
        private Geometry geometry;
        
        @Data
        public static class Geometry {
            private Location location;
            
            @Data
            public static class Location {
                private double lat;
                private double lng;
            }
        }
    }
} 