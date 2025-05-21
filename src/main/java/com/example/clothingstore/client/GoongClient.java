package com.example.clothingstore.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.clothingstore.dto.response.DistanceMatrixResponse;
import com.example.clothingstore.dto.response.GeocodingResponse;

@FeignClient(name = "goongClient", url = "${goong.api.base-url}")
public interface GoongClient {
    @GetMapping("/geocode")
    GeocodingResponse geocodeAddress(@RequestParam("address") String address, 
                                    @RequestParam("api_key") String apiKey);
                                    
    @GetMapping("/DistanceMatrix")
    DistanceMatrixResponse calculateDistance(@RequestParam("origins") String origins,
                                           @RequestParam("destinations") String destinations,
                                           @RequestParam("vehicle") String vehicle,
                                           @RequestParam("api_key") String apiKey);
} 