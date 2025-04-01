package com.example.clothingstore.aspect;

import com.example.clothingstore.annotation.EnableSoftDeleteFilter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SoftDeleteFilterAspect {

  private final EntityManager entityManager;

  @Around("@annotation(enableSoftDeleteFilter)")
  public Object applySoftDeleteFilter(ProceedingJoinPoint joinPoint, EnableSoftDeleteFilter enableSoftDeleteFilter) throws Throwable {
    Session session = entityManager.unwrap(Session.class);

    if (enableSoftDeleteFilter.value()) {
      // Enable filter if the annotation has a value of true
      Filter deletedFilter = session.enableFilter("deletedFilter");
      deletedFilter.setParameter("isDeleted", false);
    }

    try {
      return joinPoint.proceed();  // Gọi phương thức gốc
    } finally {
      if (enableSoftDeleteFilter.value()) {
        // Disable filter after the method is completed
        session.disableFilter("deletedFilter");
      }
    }
  }
}
