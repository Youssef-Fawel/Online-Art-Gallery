document.addEventListener('DOMContentLoaded', function() {
    // Get all "Add to Cart" buttons
    const addToCartButtons = document.querySelectorAll('.add-to-cart-btn');

    // Add event listeners to each button
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            // Get the artwork ID from the data attribute
            const artworkId = this.getAttribute('data-artwork-id');

            // Disable button while processing
            this.disabled = true;
            const originalText = this.innerHTML;
            this.innerHTML = '<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Adding...';

            // Get CSRF token - ensure these meta tags exist in your header
            const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

            // Prepare headers
            const headers = {
                'Content-Type': 'application/x-www-form-urlencoded'
            };

            // Add CSRF token if available
            if (csrfToken && csrfHeader) {
                headers[csrfHeader] = csrfToken;
            } else {
                console.warn('CSRF tokens not found. Request might fail if CSRF protection is enabled.');
            }

            // Send AJAX request to add item to cart
            fetch('/cart/add', {
                method: 'POST',
                headers: headers,
                body: 'artworkId=' + artworkId
            })
                .then(response => {
                    if (!response.ok) {
                        throw new Error(`HTTP error! Status: ${response.status}`);
                    }
                    return response.json();
                })
                .then(data => {
                    // Re-enable button
                    this.disabled = false;
                    this.innerHTML = originalText;

                    if (data.success) {
                        // Update cart count in the UI
                        const cartCountElement = document.querySelector('.card-body .display-4');
                        if (cartCountElement) {
                            cartCountElement.textContent = data.cartCount + ' Items';
                        }

                        // Show success message
                        showToast('Success', 'Artwork added to cart!', 'success');
                    } else {
                        showToast('Error', data.message || 'Error adding to cart', 'danger');
                    }
                })
                .catch(error => {
                    // Re-enable button
                    this.disabled = false;
                    this.innerHTML = originalText;

                    console.error('Error:', error);
                    showToast('Error', 'An error occurred while adding to cart', 'danger');
                });
        });
    });

    // Function to show Bootstrap toast
    function showToast(title, message, type) {
        // Create toast container if it doesn't exist
        let toastContainer = document.querySelector('.toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            document.body.appendChild(toastContainer);
        }

        // Create toast element
        const toastId = 'toast-' + Date.now();
        const toastEl = document.createElement('div');
        toastEl.id = toastId;
        toastEl.className = `toast align-items-center text-white bg-${type} border-0`;
        toastEl.setAttribute('role', 'alert');
        toastEl.setAttribute('aria-live', 'assertive');
        toastEl.setAttribute('aria-atomic', 'true');

        // Create toast content
        toastEl.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <strong>${title}:</strong> ${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        `;

        // Add toast to container
        toastContainer.appendChild(toastEl);

        // Try to use Bootstrap's Toast if available
        try {
            if (typeof bootstrap !== 'undefined' && bootstrap.Toast) {
                const toast = new bootstrap.Toast(toastEl, { autohide: true, delay: 3000 });
                toast.show();

                // Remove toast after it's hidden
                toastEl.addEventListener('hidden.bs.toast', function() {
                    toastEl.remove();
                });
            } else {
                // Fallback if Bootstrap JS is not loaded
                console.warn('Bootstrap JS not loaded. Using fallback toast.');
                toastEl.style.display = 'block';
                setTimeout(() => {
                    toastEl.remove();
                }, 3000);
            }
        } catch (error) {
            console.error('Error showing toast:', error);
            // Simple fallback
            alert(`${title}: ${message}`);
        }
    }
});
