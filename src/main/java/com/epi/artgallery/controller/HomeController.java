package com.epi.artgallery.controller;

import com.epi.artgallery.model.Artist;
import com.epi.artgallery.model.Artwork;
import com.epi.artgallery.service.ArtistService;
import com.epi.artgallery.service.ArtworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    private final ArtworkService artworkService;
    private final ArtistService artistService;

    @Autowired
    public HomeController(ArtworkService artworkService, ArtistService artistService) {
        this.artworkService = artworkService;
        this.artistService = artistService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Artwork> artworks = artworkService.findAllArtworks(); // Fetch all artworks
        List<Artist> artists = artistService.findAllArtists(); // Fetch all artists

        model.addAttribute("artworks", artworks);
        model.addAttribute("artists", artists);

        return "home";
    }
}
