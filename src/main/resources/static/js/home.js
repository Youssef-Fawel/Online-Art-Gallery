document.addEventListener('DOMContentLoaded', function() {
    // ===== ARTWORK SLIDER =====
    const artworkItems = document.querySelectorAll('.artwork-item');
    const totalArtworks = artworkItems.length;
    let currentArtworkIndex = 0;
    const prevArtworksBtn = document.getElementById('prevArtworks');
    const nextArtworksBtn = document.getElementById('nextArtworks');
    let artworkAutoSlideInterval;
    let artworkTransitioning = false;

    // Check if there are any artworks
    if (totalArtworks > 0) {
        // Create indicators for artworks
        const artworkIndicatorsContainer = document.querySelector('.artwork-indicators');
        if (artworkIndicatorsContainer) {
            // Clear any existing indicators first
            artworkIndicatorsContainer.innerHTML = '';
            for (let i = 0; i < totalArtworks; i++) {
                const indicator = document.createElement('div');
                indicator.classList.add('carousel-indicator');
                if (i === 0) indicator.classList.add('active');
                indicator.dataset.index = i;
                indicator.addEventListener('click', function() {
                    if (artworkTransitioning) return;
                    if (currentArtworkIndex !== i) {
                        showArtwork(i);
                        resetArtworkAutoSlide();
                    }
                });
                artworkIndicatorsContainer.appendChild(indicator);
            }
        }

        // Initial state
        updateArtworkPaginationButtons();

        // Manual navigation
        if (prevArtworksBtn) {
            prevArtworksBtn.addEventListener('click', function() {
                if (artworkTransitioning) return;
                const newIndex = (currentArtworkIndex - 1 + totalArtworks) % totalArtworks;
                showArtwork(newIndex);
                resetArtworkAutoSlide();
            });
        }
        if (nextArtworksBtn) {
            nextArtworksBtn.addEventListener('click', function() {
                if (artworkTransitioning) return;
                const newIndex = (currentArtworkIndex + 1) % totalArtworks;
                showArtwork(newIndex);
                resetArtworkAutoSlide();
            });
        }

        /**
         * Show the artwork at the specified index
         * @param {number} newIndex - The index of the artwork to show
         */
        function showArtwork(newIndex) {
            if (artworkTransitioning || newIndex === currentArtworkIndex) return;
            artworkTransitioning = true;

            // Fade out current artwork
            artworkItems[currentArtworkIndex].classList.add('fade-out');

            // After fade out, hide current and show new
            setTimeout(function() {
                artworkItems[currentArtworkIndex].classList.add('d-none');
                artworkItems[currentArtworkIndex].classList.remove('fade-out');

                // Show new artwork
                artworkItems[newIndex].classList.remove('d-none');

                // Force a reflow to ensure the transition works
                void artworkItems[newIndex].offsetWidth;

                // Update current index
                currentArtworkIndex = newIndex;
                updateArtworkPaginationButtons();
                updateArtworkIndicators();

                // End transition state
                setTimeout(function() {
                    artworkTransitioning = false;
                }, 800); // Match the CSS transition duration
            }, 800); // Match the CSS transition duration
        }

        /**
         * Update the state of the pagination buttons
         */
        function updateArtworkPaginationButtons() {
            // For a circular carousel, buttons are always enabled
            if (prevArtworksBtn) {
                prevArtworksBtn.disabled = false;
                prevArtworksBtn.classList.remove('opacity-50');
            }
            if (nextArtworksBtn) {
                nextArtworksBtn.disabled = false;
                nextArtworksBtn.classList.remove('opacity-50');
            }
        }

        /**
         * Update the active state of the indicators
         */
        function updateArtworkIndicators() {
            const artworkIndicatorsContainer = document.querySelector('.artwork-indicators');
            if (!artworkIndicatorsContainer) return;

            const indicators = artworkIndicatorsContainer.querySelectorAll('.carousel-indicator');
            indicators.forEach(function(indicator, index) {
                if (index === currentArtworkIndex) {
                    indicator.classList.add('active');
                } else {
                    indicator.classList.remove('active');
                }
            });
        }

        /**
         * Start the auto-slide functionality for artworks
         */
        function startArtworkAutoSlide() {
            // Clear any existing interval first
            if (artworkAutoSlideInterval) {
                clearInterval(artworkAutoSlideInterval);
            }
            artworkAutoSlideInterval = setInterval(function() {
                if (!artworkTransitioning) {
                    const newIndex = (currentArtworkIndex + 1) % totalArtworks;
                    showArtwork(newIndex);
                }
            }, 5000); // Change slide every 5 seconds
        }

        /**
         * Reset the auto-slide timer
         */
        function resetArtworkAutoSlide() {
            clearInterval(artworkAutoSlideInterval);
            startArtworkAutoSlide();
        }

        // Start auto-sliding for artworks
        if (totalArtworks > 1) {
            startArtworkAutoSlide();

            // Pause auto-sliding when user hovers over the carousel
            const artworkCarousel = document.querySelector('.artwork-carousel');
            if (artworkCarousel) {
                artworkCarousel.addEventListener('mouseenter', function() {
                    clearInterval(artworkAutoSlideInterval);
                });
                artworkCarousel.addEventListener('mouseleave', function() {
                    if (totalArtworks > 1) {
                        startArtworkAutoSlide();
                    }
                });
            }
        }
    } else {
        // Hide navigation buttons if no artworks
        if (prevArtworksBtn) prevArtworksBtn.style.display = 'none';
        if (nextArtworksBtn) nextArtworksBtn.style.display = 'none';
    }

    // ===== ARTIST SLIDER =====
    const artistItems = document.querySelectorAll('.artist-item');
    const totalArtists = artistItems.length;
    let currentArtistIndex = 0;
    const prevArtistsBtn = document.getElementById('prevArtists');
    const nextArtistsBtn = document.getElementById('nextArtists');
    let artistAutoSlideInterval;
    let artistTransitioning = false;

    // Check if there are any artists
    if (totalArtists > 0) {
        // Create indicators for artists
        const artistIndicatorsContainer = document.querySelector('.artist-indicators');
        if (artistIndicatorsContainer) {
            // Clear any existing indicators first
            artistIndicatorsContainer.innerHTML = '';
            for (let i = 0; i < totalArtists; i++) {
                const indicator = document.createElement('div');
                indicator.classList.add('carousel-indicator');
                if (i === 0) indicator.classList.add('active');
                indicator.dataset.index = i;
                indicator.addEventListener('click', function() {
                    if (artistTransitioning) return;
                    if (currentArtistIndex !== i) {
                        showArtist(i);
                        resetArtistAutoSlide();
                    }
                });
                artistIndicatorsContainer.appendChild(indicator);
            }
        }

        // Initial state
        updateArtistPaginationButtons();

        // Manual navigation
        if (prevArtistsBtn) {
            prevArtistsBtn.addEventListener('click', function() {
                if (artistTransitioning) return;
                const newIndex = (currentArtistIndex - 1 + totalArtists) % totalArtists;
                showArtist(newIndex);
                resetArtistAutoSlide();
            });
        }
        if (nextArtistsBtn) {
            nextArtistsBtn.addEventListener('click', function() {
                if (artistTransitioning) return;
                const newIndex = (currentArtistIndex + 1) % totalArtists;
                showArtist(newIndex);
                resetArtistAutoSlide();
            });
        }

        /**
         * Show the artist at the specified index
         * @param {number} newIndex - The index of the artist to show
         */
        function showArtist(newIndex) {
            if (artistTransitioning || newIndex === currentArtistIndex) return;
            artistTransitioning = true;

            // Fade out current artist
            artistItems[currentArtistIndex].classList.add('fade-out');

            // After fade out, hide current and show new
            setTimeout(function() {
                artistItems[currentArtistIndex].classList.add('d-none');
                artistItems[currentArtistIndex].classList.remove('fade-out');

                // Show new artist
                artistItems[newIndex].classList.remove('d-none');

                // Force a reflow to ensure the transition works
                void artistItems[newIndex].offsetWidth;

                // Update current index
                currentArtistIndex = newIndex;
                updateArtistPaginationButtons();
                updateArtistIndicators();

                // End transition state
                setTimeout(function() {
                    artistTransitioning = false;
                }, 800); // Match the CSS transition duration
            }, 800); // Match the CSS transition duration
        }

        /**
         * Update the state of the pagination buttons
         */
        function updateArtistPaginationButtons() {
            // For a circular carousel, buttons are always enabled
            if (prevArtistsBtn) {
                prevArtistsBtn.disabled = false;
                prevArtistsBtn.classList.remove('opacity-50');
            }
            if (nextArtistsBtn) {
                nextArtistsBtn.disabled = false;
                nextArtistsBtn.classList.remove('opacity-50');
            }
        }

        /**
         * Update the active state of the indicators
         */
        function updateArtistIndicators() {
            const artistIndicatorsContainer = document.querySelector('.artist-indicators');
            if (!artistIndicatorsContainer) return;

            const indicators = artistIndicatorsContainer.querySelectorAll('.carousel-indicator');
            indicators.forEach(function(indicator, index) {
                if (index === currentArtistIndex) {
                    indicator.classList.add('active');
                } else {
                    indicator.classList.remove('active');
                }
            });
        }

        /**
         * Start the auto-slide functionality for artists
         */
        function startArtistAutoSlide() {
            // Clear any existing interval first
            if (artistAutoSlideInterval) {
                clearInterval(artistAutoSlideInterval);
            }
            artistAutoSlideInterval = setInterval(function() {
                if (!artistTransitioning) {
                    const newIndex = (currentArtistIndex + 1) % totalArtists;
                    showArtist(newIndex);
                }
            }, 5000); // Change slide every 5 seconds
        }

        /**
         * Reset the auto-slide timer
         */
        function resetArtistAutoSlide() {
            clearInterval(artistAutoSlideInterval);
            startArtistAutoSlide();
        }

        // Start auto-sliding for artists
        if (totalArtists > 1) {
            startArtistAutoSlide();

            // Pause auto-sliding when user hovers over the carousel
            const artistCarousel = document.querySelector('.artist-carousel');
            if (artistCarousel) {
                artistCarousel.addEventListener('mouseenter', function() {
                    clearInterval(artistAutoSlideInterval);
                });
                artistCarousel.addEventListener('mouseleave', function() {
                    if (totalArtists > 1) {
                        startArtistAutoSlide();
                    }
                });
            }
        }
    } else {
        // Hide navigation buttons if no artists
        if (prevArtistsBtn) prevArtistsBtn.style.display = 'none';
        if (nextArtistsBtn) nextArtistsBtn.style.display = 'none';
    }

    // Add page load animation
    document.body.classList.add('page-loaded');

    // Initialize any tooltips if Bootstrap is available
    if (typeof bootstrap !== 'undefined' && bootstrap.Tooltip) {
        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
        tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl);
        });
    }

    // Handle any errors with artwork images
    document.querySelectorAll('.artwork-image-container img').forEach(img => {
        img.addEventListener('error', function() {
            this.src = '/images/artwork-placeholder.jpg';
        });
    });

    // Handle any errors with artist images
    document.querySelectorAll('.artist-portrait').forEach(img => {
        img.addEventListener('error', function() {
            this.src = '/images/artist-placeholder.jpg';
        });
    });

    // Back to top button functionality
    const backToTopBtn = document.getElementById('backToTop');
    if (backToTopBtn) {
        window.addEventListener('scroll', function() {
            if (window.pageYOffset > 300) {
                backToTopBtn.classList.add('show');
            } else {
                backToTopBtn.classList.remove('show');
            }
        });

        backToTopBtn.addEventListener('click', function() {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // Initialize particles.js if available
    if (typeof particlesJS !== 'undefined') {
        const particlesContainer = document.getElementById('particles-js');
        if (particlesContainer) {
            particlesJS('particles-js', {
                particles: {
                    number: { value: 80, density: { enable: true, value_area: 800 } },
                    color: { value: "#ffffff" },
                    opacity: { value: 0.5, random: false },
                    size: { value: 3, random: true },
                    line_linked: { enable: true, distance: 150, color: "#ffffff", opacity: 0.4, width: 1 }
                },
                interactivity: {
                    detect_on: "canvas",
                    events: {
                        onhover: { enable: true, mode: "repulse" },
                        onclick: { enable: true, mode: "push" }
                    }
                }
            });
        }
    }

    // Newsletter form submission
    const newsletterForm = document.querySelector('.newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', function(e) {
            e.preventDefault();
            const emailInput = this.querySelector('input[type="email"]');
            if (emailInput && emailInput.value) {
                // Here you would typically send the email to your backend
                // For now, just show a success message
                const formGroup = emailInput.closest('.input-group');
                formGroup.innerHTML = '<div class="alert alert-success w-100 mb-0">Thank you for subscribing!</div>';

                // Reset after 3 seconds
                setTimeout(() => {
                    formGroup.innerHTML = `
                        <input type="email" class="form-control" placeholder="Your email address" aria-label="Your email address" required>
                        <button class="btn btn-primary" type="submit">Subscribe</button>
                    `;
                }, 3000);
            }
        });
    }

    // Lazy loading for images
    if ('loading' in HTMLImageElement.prototype) {
        // Browser supports native lazy loading
        document.querySelectorAll('img[loading="lazy"]').forEach(img => {
            img.src = img.dataset.src;
        });
    } else {
        // Fallback for browsers that don't support lazy loading
        const lazyImages = document.querySelectorAll('img[loading="lazy"]');

        if ('IntersectionObserver' in window) {
            const imageObserver = new IntersectionObserver((entries, observer) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) {
                        const image = entry.target;
                        image.src = image.dataset.src;
                        observer.unobserve(image);
                    }
                });
            });

            lazyImages.forEach(img => imageObserver.observe(img));
        } else {
            // Fallback for older browsers without IntersectionObserver
            let active = false;

            const lazyLoad = function() {
                if (active === false) {
                    active = true;

                    setTimeout(() => {
                        lazyImages.forEach(lazyImage => {
                            if ((lazyImage.getBoundingClientRect().top <= window.innerHeight &&
                                    lazyImage.getBoundingClientRect().bottom >= 0) &&
                                getComputedStyle(lazyImage).display !== "none") {

                                lazyImage.src = lazyImage.dataset.src;

                                lazyImages = lazyImages.filter(image => image !== lazyImage);

                                if (lazyImages.length === 0) {
                                    document.removeEventListener("scroll", lazyLoad);
                                    window.removeEventListener("resize", lazyLoad);
                                    window.removeEventListener("orientationchange", lazyLoad);
                                }
                            }
                        });

                        active = false;
                    }, 200);
                }
            };

            document.addEventListener("scroll", lazyLoad);
            window.addEventListener("resize", lazyLoad);
            window.addEventListener("orientationchange", lazyLoad);
            lazyLoad();
        }
    }

    // Animate elements when they come into view
    if ('IntersectionObserver' in window) {
        const animatedElements = document.querySelectorAll('.animate-on-scroll');

        const animationObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.classList.add('animated');
                    animationObserver.unobserve(entry.target);
                }
            });
        }, { threshold: 0.1 });

        animatedElements.forEach(el => animationObserver.observe(el));
    } else {
        // Fallback for browsers that don't support IntersectionObserver
        document.querySelectorAll('.animate-on-scroll').forEach(el => {
            el.classList.add('animated');
        });
    }

    // Filter functionality for categories
    const categoryFilters = document.querySelectorAll('.category-filter');
    if (categoryFilters.length > 0) {
        categoryFilters.forEach(filter => {
            filter.addEventListener('click', function(e) {
                e.preventDefault();

                // Remove active class from all filters
                categoryFilters.forEach(f => f.classList.remove('active'));

                // Add active class to clicked filter
                this.classList.add('active');

                const category = this.dataset.category;
                const artworkCards = document.querySelectorAll('.filtered-artwork');

                if (category === 'all') {
                    // Show all artworks
                    artworkCards.forEach(card => {
                        card.style.display = 'block';
                        setTimeout(() => {
                            card.style.opacity = '1';
                        }, 50);
                    });
                } else {
                    // Filter artworks by category
                    artworkCards.forEach(card => {
                        if (card.dataset.category === category) {
                            card.style.display = 'block';
                            setTimeout(() => {
                                card.style.opacity = '1';
                            }, 50);
                        } else {
                            card.style.opacity = '0';
                            setTimeout(() => {
                                card.style.display = 'none';
                            }, 500);
                        }
                    });
                }
            });
        });
    }

    // Mobile menu toggle
    const mobileMenuToggle = document.getElementById('mobileMenuToggle');
    const mobileMenu = document.getElementById('mobileMenu');

    if (mobileMenuToggle && mobileMenu) {
        mobileMenuToggle.addEventListener('click', function() {
            mobileMenu.classList.toggle('show');
            document.body.classList.toggle('menu-open');
        });

        // Close mobile menu when clicking outside
        document.addEventListener('click', function(e) {
            if (mobileMenu.classList.contains('show') &&
                !mobileMenu.contains(e.target) &&
                e.target !== mobileMenuToggle &&
                !mobileMenuToggle.contains(e.target)) {
                mobileMenu.classList.remove('show');
                document.body.classList.remove('menu-open');
            }
        });
    }
});

