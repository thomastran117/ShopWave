package backend.seeds;

import backend.models.core.Product;
import backend.models.core.ProductReview;
import backend.models.core.User;
import backend.models.enums.ReviewStatus;
import backend.repositories.ProductReviewRepository;
import backend.seeds.UserSeeder.SeededUsers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class ReviewSeeder {

    private final ProductReviewRepository productReviewRepository;

    public void seed(List<Product> tech, List<Product> style, List<Product> wellness, SeededUsers users) {
        // TechGadgets — Headphones (index 0) and Smart Watch (index 1)
        review(tech.get(0), users.alice(), 5, "Best headphones I've owned",
                "The ANC is incredible — blocks out my entire open-plan office. Sound is warm and detailed. Battery life is exactly as advertised.");
        review(tech.get(0), users.bob(), 4, "Great but slightly tight fit",
                "Sound quality is top-notch and the ANC works brilliantly. Clamping force is a bit strong at first but loosened after a week.");
        review(tech.get(1), users.alice(), 5, "Life-changing smartwatch",
                "The health features alone are worth every penny. ECG and SpO2 have been spot-on compared to my doctor's equipment.");
        review(tech.get(1), users.carol(), 4, "Excellent watch, minor software quirks",
                "Hardware is beautiful and battery lasts 2 weeks easily. A couple of minor software bugs but nothing that ruins the experience.");

        // StyleHub — T-Shirt (index 0) and Zip Hoodie (index 2)
        review(style.get(0), users.bob(), 5, "Softest tee I've ever worn",
                "The organic cotton is incredibly soft right out of the bag. Fit is true to size, washes well, and still looks new after months.");
        review(style.get(0), users.carol(), 5, "Worth every cent",
                "I ordered three different colors. The fabric quality is noticeably better than anything at this price point. Highly recommend.");
        review(style.get(2), users.alice(), 4, "Heavyweight and warm",
                "This hoodie is thick and cozy — perfect for chilly evenings. The zip is smooth and the fit is relaxed without being boxy.");
        review(style.get(2), users.bob(), 5, "My new favourite piece",
                "I've been wearing this almost every day since it arrived. The French terry is plush, the colours are rich, and it's held its shape perfectly.");

        // WellnessWorld — Whey Protein (index 0) and Yoga Mat (index 5)
        review(wellness.get(0), users.carol(), 5, "Best-tasting protein powder",
                "The chocolate flavor is genuinely delicious — tastes like a milkshake, not chalky at all. Mixes perfectly with just a shaker.");
        review(wellness.get(0), users.alice(), 5, "Clean and effective",
                "Love that there are no artificial sweeteners. Mixes cleanly, sits well in my stomach, and the macros are excellent.");
        review(wellness.get(5), users.carol(), 5, "Superior grip, zero slip",
                "I've tried many yoga mats and this is by far the best. The suede top grips perfectly even in hot yoga. The alignment guides are super helpful.");
        review(wellness.get(5), users.bob(), 4, "Great mat, takes time to break in",
                "Grip is excellent once broken in. First few sessions had a slight rubber smell but it disappeared after airing out. Very durable.");
    }

    private void review(Product product, User reviewer, int rating, String title, String body) {
        if (productReviewRepository.existsByProductIdAndReviewerId(product.getId(), reviewer.getId())) return;
        ProductReview r = new ProductReview();
        r.setProduct(product);
        r.setReviewer(reviewer);
        r.setRating(rating);
        r.setTitle(title);
        r.setBody(body);
        r.setStatus(ReviewStatus.PUBLISHED);
        productReviewRepository.save(r);
    }
}
