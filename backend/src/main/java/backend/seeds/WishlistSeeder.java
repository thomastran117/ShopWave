package backend.seeds;

import backend.models.core.Product;
import backend.models.core.User;
import backend.models.core.Wishlist;
import backend.models.core.WishlistItem;
import backend.repositories.WishlistRepository;
import backend.seeds.UserSeeder.SeededUsers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds wishlists for the 3 dev customer accounts.
 *
 * Alice  — "Dream Tech Setup" (public, 4 items) + "Gaming Corner" (private, 3 items)
 * Bob    — "Style Picks" (public, 4 items)
 * Carol  — "Wellness Goals" (private, 4 items)
 *
 * Product indices match TechGadgetsProductSeeder, StyleHubProductSeeder,
 * and WellnessWorldProductSeeder creation order.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class WishlistSeeder {

    private final WishlistRepository wishlistRepository;

    public void seed(SeededUsers users,
                     List<Product> tech, List<Product> style, List<Product> wellness) {
        seedAlice(users.alice(), tech);
        seedBob(users.bob(), style);
        seedCarol(users.carol(), wellness);
    }

    // =========================================================================
    // Alice Johnson — tech enthusiast
    //
    //  Tech indices used:
    //    1=Smart Watch  5=4K Webcam  7=Earbuds  11=Portable SSD
    //    20=Gaming Headset  9=Gaming Mouse  24=XXL Desk Mat
    // =========================================================================

    private void seedAlice(User user, List<Product> tech) {
        if (!wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) return;

        wishlist(user, "Dream Tech Setup", true,
                tech.get(1),   // Smart Watch Series X
                tech.get(5),   // 4K Webcam Pro
                tech.get(7),   // Noise-Cancelling Earbuds
                tech.get(11)); // Portable SSD 1TB

        wishlist(user, "Gaming Corner", false,
                tech.get(20),  // Gaming Headset
                tech.get(9),   // LED Gaming Mouse
                tech.get(24)); // XXL Desk Mat
    }

    // =========================================================================
    // Bob Martinez — style-conscious new customer
    //
    //  Style indices used:
    //    0=Organic Tee  1=Denim Jeans  4=Canvas Sneakers  13=Puffer Jacket
    // =========================================================================

    private void seedBob(User user, List<Product> style) {
        if (!wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) return;

        wishlist(user, "Style Picks", true,
                style.get(0),   // Premium Organic Cotton T-Shirt
                style.get(1),   // Slim-Fit Stretch Denim Jeans
                style.get(4),   // Classic Canvas Sneakers
                style.get(13)); // Puffer Jacket
    }

    // =========================================================================
    // Carol Chen — frequent wellness buyer
    //
    //  Wellness indices used:
    //    0=Whey Protein  5=Yoga Mat  10=Sleep Gummies  15=LED Face Mask
    // =========================================================================

    private void seedCarol(User user, List<Product> wellness) {
        if (!wishlistRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId()).isEmpty()) return;

        wishlist(user, "Wellness Goals", false,
                wellness.get(0),   // Whey Protein Powder
                wellness.get(5),   // Yoga Mat Premium
                wellness.get(10),  // Sleep Support Gummies
                wellness.get(15)); // LED Therapy Face Mask
    }

    // =========================================================================

    private Wishlist wishlist(User user, String name, boolean isPublic, Product... products) {
        Wishlist w = new Wishlist();
        w.setUser(user);
        w.setName(name);
        w.setPublic(isPublic);

        for (Product p : products) {
            WishlistItem item = new WishlistItem();
            item.setWishlist(w);
            item.setProduct(p);
            item.setVariant(null);
            w.getItems().add(item);
        }

        return wishlistRepository.save(w);
    }
}
