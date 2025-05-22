package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.service.ArtistService;
import com.epi.artgallery.service.ArtworkService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;

@Controller
@RequestMapping("/artworks")
public class ArtworkController {
    private final ArtworkService artworkService;
    private final ArtistService artistService;

    public ArtworkController(ArtworkService artworkService, ArtistService artistService) {
        this.artworkService = artworkService;
        this.artistService = artistService;
    }

    @GetMapping
    public String listArtworks(Model model,
                               @RequestParam(required = false) String search,
                               @RequestParam(required = false) Long artistId,
                               @RequestParam(required = false) Double minPrice,
                               @RequestParam(required = false) Double maxPrice,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "6") int size) {
        // Add artists for filter dropdown
        model.addAttribute("artists", artistService.findAllArtists());

        try {
            // Create pageable with sorting
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

            // Use the new method that handles both pagination and filtering
            Page<Artwork> artworksPage = artworkService.findPaginatedWithFilters(search, artistId, minPrice, maxPrice, pageable);

            // Add pagination and filter data to model
            model.addAttribute("artworks", artworksPage.getContent());
            model.addAttribute("artworksPage", artworksPage);
            model.addAttribute("search", search);
            model.addAttribute("artistId", artistId);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            model.addAttribute("currentPage", page);
        } catch (Exception e) {
            // Fallback to non-paginated list if pagination fails
            model.addAttribute("artworks", artworkService.findAllArtworks());
            model.addAttribute("error", "Pagination error: " + e.getMessage());
        }

        return "artwork/list";
    }

    @GetMapping("/{id}")
    public String viewArtwork(@PathVariable Long id, Model model) {
        Artwork artwork = artworkService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid artwork Id:" + id));
        model.addAttribute("artwork", artwork);
        return "artwork/view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("artwork", new Artwork());
        model.addAttribute("artists", artistService.findAllArtists());
        return "artwork/form";
    }

    @PostMapping("/new")
    public String createArtwork(
            @Valid @ModelAttribute Artwork artwork,
            BindingResult result,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "priceString", required = false) String priceString,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("artists", artistService.findAllArtists());
            return "artwork/form";
        }

        try {
            // Parse the price string, removing commas
            if (priceString != null && !priceString.isEmpty()) {
                String cleanPrice = priceString.replace(",", "");
                try {
                    artwork.setPrice(new BigDecimal(cleanPrice));
                } catch (NumberFormatException e) {
                    model.addAttribute("artists", artistService.findAllArtists());
                    model.addAttribute("error", "Invalid price format");
                    return "artwork/form";
                }
            }

            artworkService.saveArtwork(artwork, image);
            redirectAttributes.addFlashAttribute("success", "Artwork created successfully");
            return "redirect:/artworks";
        } catch (IOException e) {
            model.addAttribute("artists", artistService.findAllArtists());
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            return "artwork/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Artwork artwork = artworkService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid artwork Id:" + id));
        model.addAttribute("artwork", artwork);
        model.addAttribute("artists", artistService.findAllArtists());
        return "artwork/form";
    }

    @PostMapping("/{id}/edit")
    public String updateArtwork(
            @PathVariable Long id,
            @Valid @ModelAttribute Artwork artwork,
            BindingResult result,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "priceString", required = false) String priceString,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("artists", artistService.findAllArtists());
            return "artwork/form";
        }

        try {
            // Parse the price string, removing commas
            if (priceString != null && !priceString.isEmpty()) {
                String cleanPrice = priceString.replace(",", "");
                try {
                    artwork.setPrice(new BigDecimal(cleanPrice));
                } catch (NumberFormatException e) {
                    model.addAttribute("artists", artistService.findAllArtists());
                    model.addAttribute("error", "Invalid price format");
                    return "artwork/form";
                }
            }

            artwork.setId(id);
            artworkService.saveArtwork(artwork, image);
            redirectAttributes.addFlashAttribute("success", "Artwork updated successfully");
            return "redirect:/artworks/" + id;
        } catch (IOException e) {
            model.addAttribute("artists", artistService.findAllArtists());
            model.addAttribute("error", "Failed to upload image: " + e.getMessage());
            return "artwork/form";
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteArtwork(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            artworkService.deleteArtwork(id);
            redirectAttributes.addFlashAttribute("success", "Artwork deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete artwork: " + e.getMessage());
        }
        return "redirect:/artworks";
    }
}
