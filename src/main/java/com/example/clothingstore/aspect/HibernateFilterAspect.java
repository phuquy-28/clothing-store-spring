package com.example.clothingstore.aspect;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.example.clothingstore.annotation.ApplyDeletedFilter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.Filter;
import org.hibernate.Session;

@Aspect
@Component
@RequiredArgsConstructor
public class HibernateFilterAspect {

  private final EntityManager entityManager;

  @Before("@annotation(applyDeletedFilter)")
  public void enableFilter(ApplyDeletedFilter applyDeletedFilter) {
    Session session = entityManager.unwrap(Session.class);
    if (applyDeletedFilter.value()) {
      Filter filter = session.enableFilter("deletedFilter");
      filter.setParameter("isDeleted", false);
    } else {
      session.disableFilter("deletedFilter");
    }
  }

  @Before("execution(* com.example.clothingstore..*(..))" + 
         " && !@annotation(com.example.clothingstore.annotation.ApplyDeletedFilter)")
  public void enableDefaultFilter() {
    Session session = entityManager.unwrap(Session.class);
    Filter filter = session.enableFilter("deletedFilter");
    filter.setParameter("isDeleted", false);
  }

  @After("@annotation(applyDeletedFilter)")
  public void disableFilter(ApplyDeletedFilter applyDeletedFilter) {
    Session session = entityManager.unwrap(Session.class);
    session.disableFilter("deletedFilter");
  }

  @After("execution(* com.example.clothingstore..*(..))" + 
        " && !@annotation(com.example.clothingstore.annotation.ApplyDeletedFilter)")
  public void disableDefaultFilter() {
    Session session = entityManager.unwrap(Session.class);
    session.disableFilter("deletedFilter");
  }
}

