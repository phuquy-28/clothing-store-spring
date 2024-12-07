package com.example.clothingstore.service;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.example.clothingstore.dto.request.CategoryReqDTO;
import com.example.clothingstore.dto.response.CategoryResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Category;

public interface CategoryService {

    CategoryResDTO createCategory(CategoryReqDTO categoryReqDTO);

    CategoryResDTO getCategoryById(Long id);

    CategoryResDTO updateCategory(CategoryReqDTO categoryReqDTO);

    void deleteCategory(Long id);

    ResultPaginationDTO getAllCategories(Specification<Category> spec, Pageable pageable);

    CategoryResDTO convertToCategoryResDTO(Category category);

}
