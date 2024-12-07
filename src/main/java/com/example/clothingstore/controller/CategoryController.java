package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.CategoryReqDTO;
import com.example.clothingstore.dto.response.CategoryResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Category;
import com.example.clothingstore.service.CategoryService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class CategoryController {

    private final Logger log = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    @PostMapping(UrlConfig.CATEGORY)
    public ResponseEntity<CategoryResDTO> createCategory(
            @RequestBody @Valid CategoryReqDTO categoryReqDTO) {
        log.debug("REST request to create category: {}", categoryReqDTO);
        CategoryResDTO createdCategory = categoryService.createCategory(categoryReqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping(UrlConfig.CATEGORY + UrlConfig.ID)
    public ResponseEntity<CategoryResDTO> getCategoryById(@PathVariable Long id) {
        log.debug("REST request to get category by id: {}", id);
        CategoryResDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping(UrlConfig.CATEGORY)
    public ResponseEntity<ResultPaginationDTO> getAllCategories(@Filter Specification<Category> spec, Pageable pageable) {
        log.debug("REST request to get all categories");
        ResultPaginationDTO categories = categoryService.getAllCategories(spec, pageable);
        return ResponseEntity.ok(categories);
    }

    @PutMapping(UrlConfig.CATEGORY)
    public ResponseEntity<CategoryResDTO> updateCategory(
            @RequestBody CategoryReqDTO categoryReqDTO) {
        log.debug("REST request to update category: {}", categoryReqDTO);
        CategoryResDTO updatedCategory = categoryService.updateCategory(categoryReqDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping(UrlConfig.CATEGORY + UrlConfig.ID)
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        log.debug("REST request to delete category by id: {}", id);
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
