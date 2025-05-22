package com.epi.artgallery.service;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.repository.ArtistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArtistService {
    private final ArtistRepository artistRepository;
    private final Path rootLocation = Paths.get("uploads/artists");

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    public List<Artist> findAllArtists() {
        return artistRepository.findAll();
    }

    public Optional<Artist> findById(Long id) {
        return artistRepository.findById(id);
    }

    public List<Artist> searchArtists(String query) {
        return artistRepository.findByNameContainingIgnoreCase(query);
    }

    public Artist saveArtist(Artist artist, MultipartFile image) throws IOException {
        // Handle image upload if provided
        if (image != null && !image.isEmpty()) {
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Files.copy(image.getInputStream(), rootLocation.resolve(filename));
            artist.setImageUrl(filename);
        }
        return artistRepository.save(artist);
    }

    public void deleteArtist(Long id) {
        artistRepository.deleteById(id);
    }

    public Page<Artist> findByNameContaining(String keyword, Pageable pageable) {
        return artistRepository.findByNameContainingIgnoreCase(keyword, pageable);
    }

    public Page<Artist> findByCountry(String country, Pageable pageable) {
        return artistRepository.findByCountryIgnoreCase(country, pageable);
    }

    public Page<Artist> findAll(Pageable pageable) {
        return artistRepository.findAll(pageable);
    }

    public List<String> findAllCountries() {
        return artistRepository.findAll().stream()
                .map(Artist::getCountry)
                .filter(country -> country != null && !country.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public long countArtists() {
        return artistRepository.count();
    }

    public List<Artist> findRecentArtists(int limit) {
        return artistRepository.findAll(PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"))).getContent();
    }

    public List<Artist> findFeaturedArtists(PageRequest pageRequest) {
        return artistRepository.findByFeaturedTrue(pageRequest);
    }

    public void deleteById(Long id) {
        artistRepository.deleteById(id);
    }

    public void save(Artist artist) {
        artistRepository.save(artist);
    }
}
