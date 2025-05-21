package com.example.clothingstore.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class DistanceMatrixResponse {
    private List<Row> rows;
    
    @Data
    public static class Row {
        private List<Element> elements;
        
        @Data
        public static class Element {
            private String status;
            private Distance distance;
            private Duration duration;
            
            @Data
            public static class Distance {
                private String text;
                private int value;  // distance in meters
            }
            
            @Data
            public static class Duration {
                private String text;
                private int value;  // duration in seconds
            }
        }
    }
} 