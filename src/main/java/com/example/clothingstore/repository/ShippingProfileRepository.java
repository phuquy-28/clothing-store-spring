package com.example.clothingstore.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.clothingstore.entity.ShippingProfile;
import com.example.clothingstore.entity.User;

@Repository
public interface ShippingProfileRepository extends JpaRepository<ShippingProfile, Long> {

  List<ShippingProfile> findByUser(User user);

}
