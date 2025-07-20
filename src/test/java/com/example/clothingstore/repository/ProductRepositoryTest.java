package com.example.clothingstore.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.example.clothingstore.entity.Category;
import com.example.clothingstore.entity.LineItem;
import com.example.clothingstore.entity.Order;
import com.example.clothingstore.entity.Product;
import com.example.clothingstore.entity.ProductVariant;
import com.example.clothingstore.enumeration.Color;
import com.example.clothingstore.enumeration.OrderStatus;
import com.example.clothingstore.enumeration.Size;

@DataJpaTest
public class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private ProductVariant variant1;
    private ProductVariant variant2;
    private Order order;
    private LineItem lineItem1;
    private LineItem lineItem2;

    @BeforeEach
    void setUp() {
        // Create category
        Category category = new Category();
        category.setName("Test Category");
        category = entityManager.persist(category);

        // Create products
        product1 = new Product();
        product1.setName("Test Product 1");
        product1.setSlug("test-product-1");
        product1.setDescription("Test Description 1");
        product1.setCategory(category);
        product1.setDeleted(false);
        product1 = entityManager.persist(product1);

        product2 = new Product();
        product2.setName("Test Product 2");
        product2.setSlug("test-product-2");
        product2.setDescription("Test Description 2");
        product2.setCategory(category);
        product2.setDeleted(false);
        product2 = entityManager.persist(product2);

        // Create variants
        variant1 = new ProductVariant();
        variant1.setProduct(product1);
        variant1.setSku("SKU-1");
        variant1.setColor(Color.BLACK);
        variant1.setSize(Size.M);
        variant1.setQuantity(50);
        variant1.setDifferencePrice(100.0);
        variant1 = entityManager.persist(variant1);

        variant2 = new ProductVariant();
        variant2.setProduct(product2);
        variant2.setSku("SKU-2");
        variant2.setColor(Color.WHITE);
        variant2.setSize(Size.L);
        variant2.setQuantity(30);
        variant2.setDifferencePrice(150.0);
        variant2 = entityManager.persist(variant2);

        // Create order
        order = new Order();
        order.setStatus(OrderStatus.DELIVERED);
        order.setOrderDate(Instant.now());
        order = entityManager.persist(order);

        // Create line items
        lineItem1 = new LineItem();
        lineItem1.setOrder(order);
        lineItem1.setProductVariant(variant1);
        lineItem1.setQuantity(5L);
        lineItem1.setUnitPrice(100.0);
        lineItem1.setDiscountAmount(10.0);
        lineItem1 = entityManager.persist(lineItem1);

        lineItem2 = new LineItem();
        lineItem2.setOrder(order);
        lineItem2.setProductVariant(variant2);
        lineItem2.setQuantity(3L);
        lineItem2.setUnitPrice(150.0);
        lineItem2.setDiscountAmount(15.0);
        lineItem2 = entityManager.persist(lineItem2);

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("Should count sold quantity by product ID")
    void countSoldQuantityByProductId_ShouldReturnCorrectCount() {
        // When
        Long soldQuantity = productRepository.countSoldQuantityByProductId(product1.getId(), OrderStatus.DELIVERED);

        // Then
        assertThat(soldQuantity).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should count sold quantity by variant ID")
    void countSoldQuantityByVariantId_ShouldReturnCorrectCount() {
        // When
        Long soldQuantity = productRepository.countSoldQuantityByVariantId(variant1.getId());

        // Then
        assertThat(soldQuantity).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should find all products with variants")
    void findAllWithVariants_ShouldReturnProductsWithVariants() {
        // When
        List<Product> products = productRepository.findAllWithVariants();

        // Then
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getVariants()).isNotEmpty();
        assertThat(products.get(1).getVariants()).isNotEmpty();
    }

    @Test
    @DisplayName("Should find top selling products with order status")
    void findTopSellingProducts_WithOrderStatus_ShouldReturnOrderedByQuantity() {
        // When
        List<Product> products = productRepository.findTopSellingProducts(OrderStatus.DELIVERED, PageRequest.of(0, 10));

        // Then
        assertThat(products).hasSize(2);
        // First product should be the one with more sold quantity
        assertThat(products.get(0).getId()).isEqualTo(product1.getId());
    }

    @Test
    @DisplayName("Should find top selling products with date range")
    void findTopSellingProducts_WithDateRange_ShouldReturnOrderedByTotalSales() {
        // Given
        Instant startDate = Instant.now().minusSeconds(3600); // 1 hour ago
        Instant endDate = Instant.now().plusSeconds(3600); // 1 hour from now

        // When
        Page<Object[]> result = productRepository.findTopSellingProducts(
            startDate, 
            endDate, 
            null, 
            PageRequest.of(0, 10)
        );

        // Then
        assertThat(result.getContent()).isNotEmpty();
        Object[] firstProduct = result.getContent().get(0);
        
        // Verify the structure of returned data
        assertThat(firstProduct[0]).isEqualTo(product1.getId()); // id
        assertThat(firstProduct[1]).isEqualTo(product1.getName()); // name
        assertThat(((Number) firstProduct[3]).longValue()).isEqualTo(5L); // quantitySold
        // totalSales = (unitPrice * quantity) - (discountAmount * quantity)
        // = (100 * 5) - (10 * 5) = 450
        assertThat(((Number) firstProduct[4]).doubleValue()).isEqualTo(450.0); // totalSales
    }
}
