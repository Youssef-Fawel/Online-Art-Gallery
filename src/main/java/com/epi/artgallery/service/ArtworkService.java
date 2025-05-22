package com.epi.artgallery.service;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.repository.ArtworkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArtworkService {
    private final ArtworkRepository artworkRepository;
    private final Path rootLocation = Paths.get("uploads/artworks");

    public ArtworkService(ArtworkRepository artworkRepository) {
        this.artworkRepository = artworkRepository;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public List<Artwork> findAllArtworks() {
        return artworkRepository.findAll();
    }

    public Optional<Artwork> findById(Long id) {
        return artworkRepository.findById(id);
    }

    public List<Artwork> searchArtworks(String query) {
        return artworkRepository.findByTitleContainingIgnoreCase(query);
    }

    public List<Artwork> findByArtist(Artist artist) {
        return artworkRepository.findByArtist(artist);
    }

    public Artwork saveArtwork(Artwork artwork, MultipartFile image) throws IOException {
        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), rootLocation.resolve(filename));
            artwork.setImageUrl(filename);
        }
        return artworkRepository.save(artwork);
    }

    public void deleteArtwork(Long id) {
        artworkRepository.deleteById(id);
    }

    public long countArtworksByUser(String username) {
        return artworkRepository.countByCreatedBy(username);
    }

    public long countArtworks() {
        return artworkRepository.count();
    }

    public List<Artwork> findRecentArtworks(int limit) {
        return artworkRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdDate"))).getContent();
    }

    public List<Artwork> findAvailableArtworks() {
        return artworkRepository.findByAvailableTrue();
    }

    public List<Artwork> findArtworksByFilters(String search, Long artistId, Double minPrice, Double maxPrice) {
        List<Artwork> filteredArtworks = findAllArtworks();
        if (search != null && !search.isEmpty()) {
            filteredArtworks = filteredArtworks.stream()
                    .filter(artwork -> (artwork.getTitle() != null && artwork.getTitle().toLowerCase().contains(search.toLowerCase())) ||
                            (artwork.getDescription() != null && artwork.getDescription().toLowerCase().contains(search.toLowerCase())))
                    .collect(Collectors.toList());
        }
        if (artistId != null) {
            filteredArtworks = filteredArtworks.stream()
                    .filter(artwork -> artwork.getArtist() != null && artwork.getArtist().getId().equals(artistId))
                    .collect(Collectors.toList());
        }
        if (minPrice != null) {
            BigDecimal minPriceBigDecimal = BigDecimal.valueOf(minPrice);
            filteredArtworks = filteredArtworks.stream()
                    .filter(artwork -> artwork.getPrice() != null && artwork.getPrice().compareTo(minPriceBigDecimal) >= 0)
                    .collect(Collectors.toList());
        }
        if (maxPrice != null) {
            BigDecimal maxPriceBigDecimal = BigDecimal.valueOf(maxPrice);
            filteredArtworks = filteredArtworks.stream()
                    .filter(artwork -> artwork.getPrice() != null && artwork.getPrice().compareTo(maxPriceBigDecimal) <= 0)
                    .collect(Collectors.toList());
        }
        return filteredArtworks;
    }

    public Page<Artwork> findPaginated(Pageable pageable) {
        return artworkRepository.findAll(pageable);
    }

    public Page<Artwork> findPaginatedWithFilters(String search, Long artistId, Double minPrice, Double maxPrice, Pageable pageable) {
        List<Artwork> filteredList = findArtworksByFilters(search, artistId, minPrice, maxPrice);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        if (start > filteredList.size()) {
            return new PageImpl<>(List.of(), pageable, filteredList.size());
        }
        List<Artwork> pageContent = filteredList.subList(start, end);
        return new PageImpl<>(pageContent, pageable, filteredList.size());
    }
}
