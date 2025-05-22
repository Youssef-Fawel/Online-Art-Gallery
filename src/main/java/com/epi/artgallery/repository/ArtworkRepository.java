package com.epi.artgallery.repository;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findByTitleContainingIgnoreCase(String title);
    List<Artwork> findByArtist(Artist artist);
    List<Artwork> findByMediumContainingIgnoreCase(String medium);
    List<Artwork> findByYearBetween(int startYear, int endYear);
    List<Artwork> findByPriceLessThanEqual(double maxPrice);
    List<Artwork> findByPriceGreaterThanEqual(double minPrice);
    List<Artwork> findByAvailableTrue();
    List<Artwork> findTop5ByOrderByCreatedDateDesc();
    long countByCreatedBy(String username);
}
