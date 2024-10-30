package com.example.clothingstore.service.strategy.factory;

import org.springframework.stereotype.Component;
import com.example.clothingstore.enumeration.PaymentMethod;
import com.example.clothingstore.service.strategy.PaymentStrategy;
import lombok.RequiredArgsConstructor;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> paymentStrategies;
    
    public PaymentStrategy getStrategy(PaymentMethod paymentMethod) {
        return paymentStrategies.get(paymentMethod.name().toLowerCase() + "PaymentStrategy");
    }
}
