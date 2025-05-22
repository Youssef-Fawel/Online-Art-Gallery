package com.epi.artgallery.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long artworkId;
    private String title;
    private String imageUrl;
    private BigDecimal price;
    private int quantity;
    private String artistName; // Added artist name field

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // Default constructor
    public CartItem() {}

    // Constructor for session-based cart
    public CartItem(Artwork artwork, int quantity) {
        this.artworkId = artwork.getId();
        this.title = artwork.getTitle();
        this.imageUrl = artwork.getImageUrl();
        this.price = artwork.getPrice();
        this.quantity = quantity;
        // Set artist name if available
        if (artwork.getArtist() != null) {
            this.artistName = artwork.getArtist().getName();
        } else {
            this.artistName = "Unknown Artist";
        }
    }

    // Constructor for database-persistent cart
    public CartItem(Artwork artwork, User user, int quantity) {
        this(artwork, quantity);
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getArtworkId() {
        return artworkId;
    }

    public void setArtworkId(Long artworkId) {
        this.artworkId = artworkId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getSubtotal() {
        return price.multiply(new BigDecimal(quantity));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        // For database persistence, compare by ID if it's not null
        if (id != null && cartItem.id != null) {
            return id.equals(cartItem.id);
        }
        // For session storage, compare by artwork ID
        return artworkId != null && artworkId.equals(cartItem.artworkId);
    }

    @Override
    public int hashCode() {
        // For database persistence, use ID if it's not null
        if (id != null) {
            return id.hashCode();
        }
        // For session storage, use artwork ID
        return artworkId != null ? artworkId.hashCode() : 0;
    }
}
