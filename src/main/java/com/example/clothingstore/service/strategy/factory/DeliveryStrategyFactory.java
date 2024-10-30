package com.example.clothingstore.service.strategy.factory;

import java.util.Map;

import org.springframework.stereotype.Component;
import com.example.clothingstore.enumeration.DeliveryMethod;
import com.example.clothingstore.service.strategy.DeliveryStrategy;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DeliveryStrategyFactory {
    private final Map<String, DeliveryStrategy> deliveryStrategies;
    
    public DeliveryStrategy getStrategy(DeliveryMethod deliveryMethod) {
        return deliveryStrategies.get(deliveryMethod.name().toLowerCase() + "DeliveryStrategy");
    } 
}
