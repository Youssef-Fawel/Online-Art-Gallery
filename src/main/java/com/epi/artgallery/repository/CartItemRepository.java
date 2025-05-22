package com.epi.artgallery.repository;

import com.epi.artgallery.model.CartItem;
import com.epi.artgallery.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);

    Optional<CartItem> findByUserAndArtworkId(User user, Long artworkId);
    void deleteByUser(User user);
    int countByUser(User user);
}
