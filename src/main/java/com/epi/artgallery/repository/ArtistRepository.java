package com.epi.artgallery.repository;

import com.epi.artgallery.model.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {
    List<Artist> findByNameContainingIgnoreCase(String name);
    Page<Artist> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Artist> findByCountryIgnoreCase(String country, Pageable pageable);
    List<Artist> findByFeaturedTrue(Pageable pageable);
    List<Artist> findTop5ByOrderByIdDesc();
}
