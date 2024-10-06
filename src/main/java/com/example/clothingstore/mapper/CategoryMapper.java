package com.example.clothingstore.mapper;

import org.mapstruct.Mapper;

import com.example.clothingstore.dto.response.CategoryResDTO;
import com.example.clothingstore.entity.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    CategoryResDTO toCategoryResDTO(Category category);
}
