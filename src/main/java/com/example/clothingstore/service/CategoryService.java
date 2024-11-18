package com.example.clothingstore.service;

import java.util.List;

import com.example.clothingstore.dto.request.CategoryReqDTO;
import com.example.clothingstore.dto.response.CategoryResDTO;
import com.example.clothingstore.entity.Category;

public interface CategoryService {

    CategoryResDTO createCategory(CategoryReqDTO categoryReqDTO);

    CategoryResDTO getCategoryById(Long id);

    CategoryResDTO updateCategory(CategoryReqDTO categoryReqDTO);

    void deleteCategory(Long id);

    List<CategoryResDTO> getAllCategories();

    CategoryResDTO convertToCategoryResDTO(Category category);

}
