package com.example.clothingstore.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.clothingstore.constant.ErrorMessage;
import com.example.clothingstore.dto.request.CategoryReqDTO;
import com.example.clothingstore.dto.response.CategoryResDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.entity.Category;
import com.example.clothingstore.exception.IdInvalidException;
import com.example.clothingstore.exception.ResourceAlreadyExistException;
import com.example.clothingstore.exception.ResourceNotFoundException;
import com.example.clothingstore.repository.CategoryRepository;
import com.example.clothingstore.repository.ProductRepository;
import com.example.clothingstore.service.CategoryService;
import com.example.clothingstore.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

  private final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

  private final CategoryRepository categoryRepository;

  private final CategoryMapper categoryMapper;

  private final ProductRepository productRepository;

  @Override
  public CategoryResDTO createCategory(CategoryReqDTO categoryReqDTO) {
    if (categoryRepository.findByName(categoryReqDTO.getName()).isPresent()) {
      log.error("Category already exists: {}", categoryReqDTO.getName());
      throw new ResourceAlreadyExistException(ErrorMessage.CATEGORY_ALREADY_EXISTS);
    }
    Category newCategory = new Category();
    newCategory.setName(categoryReqDTO.getName());
    newCategory.setImageUrl(categoryReqDTO.getImageUrl());
    log.debug("Creating category: {}", newCategory);
    return categoryMapper.toCategoryResDTO(categoryRepository.save(newCategory));
  }

  @Override
  public CategoryResDTO getCategoryById(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND));
    return categoryMapper.toCategoryResDTO(category);
  }

  @Override
  public CategoryResDTO updateCategory(CategoryReqDTO categoryReqDTO) {
    if (categoryReqDTO.getId() == null) {
      log.error("Category id is null");
      throw new IdInvalidException(ErrorMessage.ID_CANNOT_BE_NULL);
    }
    Category category = categoryRepository.findById(categoryReqDTO.getId())
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND));
    category.setName(categoryReqDTO.getName());
    category.setImageUrl(categoryReqDTO.getImageUrl());
    log.debug("Updating category: {}", category);
    return categoryMapper.toCategoryResDTO(categoryRepository.save(category));
  }

  @Override
  public void deleteCategory(Long id) {
    Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(ErrorMessage.CATEGORY_NOT_FOUND));
    category.getProducts().forEach(product -> {
      product.setCategory(null);
    });
    productRepository.saveAll(category.getProducts());
    categoryRepository.delete(category);
    log.debug("Deleting category: {}", category);
  }

  @Override
  public ResultPaginationDTO getAllCategories(Specification<Category> spec, Pageable pageable) {
    Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

    List<CategoryResDTO> categoryResDTOs = categoryPage.getContent().stream()
        .map(categoryMapper::toCategoryResDTO).collect(Collectors.toList());

    ResultPaginationDTO.Meta meta = ResultPaginationDTO.Meta.builder()
        .page((long) pageable.getPageNumber()).pageSize((long) pageable.getPageSize())
        .total(categoryPage.getTotalElements()).pages((long) categoryPage.getTotalPages()).build();

    return ResultPaginationDTO.builder().meta(meta).data(categoryResDTOs).build();
  }

  @Override
  public CategoryResDTO convertToCategoryResDTO(Category category) {
    return CategoryResDTO.builder().id(category.getId()).name(category.getName()).build();
  }

}
