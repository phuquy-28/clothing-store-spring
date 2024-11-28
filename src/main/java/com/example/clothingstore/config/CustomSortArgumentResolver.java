package com.example.clothingstore.config;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

public class CustomSortArgumentResolver extends SortHandlerMethodArgumentResolver {

    @Override
    public Sort resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory) {

        String sortParam = webRequest.getParameter("sort");
        String orderParam = webRequest.getParameter("order");

        if (sortParam != null && orderParam != null) {
            Direction direction = Direction.fromString(orderParam);
            return Sort.by(new Order(direction, sortParam));
        }

        // Fallback to default behavior if parameters are not provided
        return super.resolveArgument(parameter, mavContainer, webRequest, binderFactory);
    }
}
