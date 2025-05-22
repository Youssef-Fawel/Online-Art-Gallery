package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.service.ArtistService;
import com.epi.artgallery.service.ArtworkService;
import com.epi.artgallery.service.FileStorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/artists")
public class ArtistController {
    private static final Logger logger = LoggerFactory.getLogger(ArtistController.class);

    private final ArtistService artistService;
    private final ArtworkService artworkService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ArtistController(ArtistService artistService, ArtworkService artworkService, FileStorageService fileStorageService) {
        this.artistService = artistService;
        this.artworkService = artworkService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping
    public String listArtists(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<Artist> artistPage;

        try {
            if (keyword != null && !keyword.isEmpty()) {
                artistPage = artistService.findByNameContaining(keyword, pageable);
                model.addAttribute("keyword", keyword);
            } else if (country != null && !country.isEmpty()) {
                artistPage = artistService.findByCountry(country, pageable);
                model.addAttribute("country", country);
            } else {
                artistPage = artistService.findAll(pageable);
            }

            model.addAttribute("artists", artistPage.getContent());
            model.addAttribute("artistPage", artistPage);
            model.addAttribute("currentPage", artistPage.getNumber());
            model.addAttribute("totalPages", artistPage.getTotalPages());
            model.addAttribute("totalItems", artistPage.getTotalElements());
            model.addAttribute("sort", sort);
            model.addAttribute("size", size);

            // Get list of countries for filter
            List<String> countries = artistService.findAllCountries();
            model.addAttribute("countries", countries);
        } catch (Exception e) {
            logger.error("Error fetching artist list", e);
            model.addAttribute("errorMessage", "An error occurred while fetching the artist list.");
        }

        return "artist/list";
    }

    @GetMapping("/{id}")
    public String viewArtist(@PathVariable Long id, Model model) {
        try {
            Optional<Artist> artist = artistService.findById(id);
            if (artist.isPresent()) {
                model.addAttribute("artist", artist.get());

                // Get artworks by this artist
                List<Artwork> artworks = artworkService.findByArtist(artist.get());
                model.addAttribute("artworks", artworks);

                return "artist/view";
            } else {
                return "redirect:/artists";
            }
        } catch (Exception e) {
            logger.error("Error viewing artist", e);
            model.addAttribute("errorMessage", "Error viewing artist details.");
            return "redirect:/artists";
        }
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCreateForm(Model model) {
        model.addAttribute("artist", new Artist());
        return "artist/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String createArtist(
            @Valid @org.springframework.web.bind.annotation.ModelAttribute Artist artist,
            BindingResult result,
            @RequestParam("profileImage") MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "artist/form";
        }

        try {
            // Handle file upload if a file was selected
            if (!profileImage.isEmpty()) {
                String fileName = fileStorageService.storeFile(profileImage, "artists");
                artist.setImageUrl(fileName);
            }

            artistService.save(artist);
            redirectAttributes.addFlashAttribute("successMessage", "Artist created successfully!");
            return "redirect:/artists";
        } catch (Exception e) {
            logger.error("Error creating artist", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create artist: " + e.getMessage());
            return "artist/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            Optional<Artist> artist = artistService.findById(id);
            if (artist.isPresent()) {
                model.addAttribute("artist", artist.get());
                return "artist/form";
            } else {
                return "redirect:/artists";
            }
        } catch (Exception e) {
            logger.error("Error fetching artist for editing", e);
            return "redirect:/artists";
        }
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateArtist(
            @PathVariable Long id,
            @Valid @org.springframework.web.bind.annotation.ModelAttribute Artist artist,
            BindingResult result,
            @RequestParam("profileImage") MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "artist/form";
        }

        try {
            Optional<Artist> existingArtist = artistService.findById(id);
            if (existingArtist.isPresent()) {
                Artist artistToUpdate = existingArtist.get();

                // Update fields
                artistToUpdate.setName(artist.getName());
                artistToUpdate.setBio(artist.getBio());
                artistToUpdate.setBirthYear(artist.getBirthYear());
                artistToUpdate.setDeathYear(artist.getDeathYear());
                artistToUpdate.setCountry(artist.getCountry());
                artistToUpdate.setWebsite(artist.getWebsite());

                // Handle file upload if a new file was selected
                if (!profileImage.isEmpty()) {
                    // Delete old image if exists
                    String currentImageUrl = artistToUpdate.getImageUrl();
                    if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                        fileStorageService.deleteFile(currentImageUrl, "artists");
                    }

                    String fileName = fileStorageService.storeFile(profileImage, "artists");
                    artistToUpdate.setImageUrl(fileName);
                }

                artistService.save(artistToUpdate);
                redirectAttributes.addFlashAttribute("successMessage", "Artist updated successfully!");
            }

            return "redirect:/artists/" + id;
        } catch (Exception e) {
            logger.error("Error updating artist", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update artist: " + e.getMessage());
            return "artist/form";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteArtist(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Optional<Artist> artist = artistService.findById(id);
            if (artist.isPresent()) {
                // Check if artist has artworks
                List<Artwork> artworks = artworkService.findByArtist(artist.get());
                if (!artworks.isEmpty()) {
                    redirectAttributes.addFlashAttribute("errorMessage",
                            "Cannot delete artist with associated artworks. Remove the artworks first.");
                    return "redirect:/artists/" + id;
                }

                // Delete profile image if exists
                String imageUrl = artist.get().getImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    try {
                        fileStorageService.deleteFile(imageUrl, "artists");
                    } catch (Exception e) {
                        logger.error("Error deleting profile image", e);
                    }
                }

                artistService.deleteById(id);
                redirectAttributes.addFlashAttribute("successMessage", "Artist deleted successfully!");
            }

            return "redirect:/artists";
        } catch (Exception e) {
            logger.error("Error deleting artist", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete artist: " + e.getMessage());
            return "redirect:/artists";
        }
    }

    @GetMapping("/featured")
    @ResponseBody
    public List<Artist> getFeaturedArtists() {
        try {
            return artistService.findFeaturedArtists(PageRequest.of(0, 3));
        } catch (Exception e) {
            logger.error("Error fetching featured artists", e);
            return List.of();
        }
    }
}
