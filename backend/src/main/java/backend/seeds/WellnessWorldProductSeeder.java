package backend.seeds;

import backend.models.core.Company;
import backend.models.core.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class WellnessWorldProductSeeder {

    private final ProductSeedHelper h;

    public List<Product> seed(Company co) {
        List<Product> list = new ArrayList<>();

        list.add(h.product(co, "Whey Protein Powder",
                "Cold-processed whey isolate with 27g protein per serving. No artificial sweeteners. Informed Sport certified.",
                "WELL-WPP-001", "54.99", "69.99", "Nutrition", "NutriForge",
                "https://placehold.co/800x800/d6eaf8/333333?text=Whey+Protein",
                150, 10, true, true, false, "MONTHLY:1,WEEKLY:4", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/d6eaf8/333333?text=Whey+Protein",
                        "https://placehold.co/800x800/c2d9f0/333333?text=Protein+Label"),
                h.attrs("Protein", "27g per serving",
                        "Servings", "30 per container",
                        "Certification", "Informed Sport",
                        "Sweetener", "Stevia, no artificial sweeteners"),
                h.options2("Flavor", "Chocolate", "Vanilla", "Strawberry", "Size", "1lb", "2lb", "5lb"),
                p -> {
                    h.pv2(p, "Chocolate",  "1lb", "WELL-WPP-CH1", new BigDecimal("34.99"), 50);
                    h.pv2(p, "Chocolate",  "2lb", "WELL-WPP-CH2", new BigDecimal("54.99"), 45);
                    h.pv2(p, "Chocolate",  "5lb", "WELL-WPP-CH5", new BigDecimal("99.99"), 30);
                    h.pv2(p, "Vanilla",    "1lb", "WELL-WPP-VA1", new BigDecimal("34.99"), 40);
                    h.pv2(p, "Vanilla",    "2lb", "WELL-WPP-VA2", new BigDecimal("54.99"), 35);
                    h.pv2(p, "Vanilla",    "5lb", "WELL-WPP-VA5", new BigDecimal("99.99"), 25);
                    h.pv2(p, "Strawberry", "1lb", "WELL-WPP-ST1", new BigDecimal("34.99"), 30);
                    h.pv2(p, "Strawberry", "2lb", "WELL-WPP-ST2", new BigDecimal("54.99"), 25);
                    h.pv2(p, "Strawberry", "5lb", "WELL-WPP-ST5", new BigDecimal("99.99"), 15);
                }));

        list.add(h.product(co, "Vitamin C Brightening Serum",
                "15% L-ascorbic acid with ferulic acid and vitamin E. Brightens skin tone and fights free radicals.",
                "WELL-VCS-001", "39.99", "54.99", "Skincare", "GlowLab",
                "https://placehold.co/800x800/fef9e7/333333?text=Vit+C+Serum",
                120, 10, false, true, false, "MONTHLY:1", new BigDecimal("15.00"),
                h.images("https://placehold.co/800x800/fef9e7/333333?text=Vit+C+Serum",
                        "https://placehold.co/800x800/fdf2d0/333333?text=Serum+Drop"),
                h.attrs("Active", "15% L-Ascorbic Acid",
                        "Additional Actives", "Ferulic Acid, Vitamin E",
                        "pH", "3.0–3.5 (active range)",
                        "Packaging", "Amber glass to prevent oxidation"),
                h.options1("Size", "30ml", "60ml"),
                p -> {
                    h.pv(p, "30ml", "WELL-VCS-30", new BigDecimal("39.99"), 60);
                    h.pv(p, "60ml", "WELL-VCS-60", new BigDecimal("64.99"), 55);
                }));

        list.add(h.product(co, "HEPA Air Purifier 360°",
                "True HEPA H13 filter removes 99.97% of particles ≥0.1µm. Auto mode adjusts to real-time air quality.",
                "WELL-APR-001", "199.99", "249.99", "Home Wellness", "PureAir",
                "https://placehold.co/800x800/eaf4f4/333333?text=Air+Purifier",
                60, 5, true, false, false, null, null,
                h.images("https://placehold.co/800x800/eaf4f4/333333?text=Air+Purifier",
                        "https://placehold.co/800x800/d0ece7/333333?text=Purifier+Filter"),
                h.attrs("Filter", "True HEPA H13",
                        "Coverage", "S:300, M:600, L:900 sq ft",
                        "CADR", "S:200, M:400, L:600 CFM",
                        "Noise", "As low as 24dB on sleep mode"),
                h.options1("Room Size", "Small (up to 300 sq ft)", "Medium (up to 600 sq ft)", "Large (up to 900 sq ft)"),
                p -> {
                    h.pv(p, "Small (up to 300 sq ft)",  "WELL-APR-SM", new BigDecimal("149.99"), 25);
                    h.pv(p, "Medium (up to 600 sq ft)", "WELL-APR-MD", new BigDecimal("199.99"), 20);
                    h.pv(p, "Large (up to 900 sq ft)",  "WELL-APR-LG", new BigDecimal("249.99"), 15);
                }));

        list.add(h.productSingle(co, "Pour-Over Coffee Maker Set",
                "Borosilicate glass dripper, stainless gooseneck kettle, and bamboo server. Makes 1–4 cups.",
                "WELL-PCM-001", "54.99", "74.99", "Kitchen", "BrewCraft",
                "https://placehold.co/800x800/f9f3e3/333333?text=Pour+Over+Set",
                100, 10, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/f9f3e3/333333?text=Pour+Over+Set",
                        "https://placehold.co/800x800/f0e6d3/333333?text=Coffee+Pour"),
                h.attrs("Carafe", "600ml borosilicate glass",
                        "Kettle", "600ml gooseneck stainless",
                        "Filter", "Reusable stainless mesh",
                        "Set Includes", "Dripper, carafe, kettle, stand, 50 paper filters")));

        list.add(h.product(co, "Soy Wax Candle Collection",
                "100% soy wax with cotton wicks and essential oil fragrance blends. 55-hour burn time per candle.",
                "WELL-SWC-001", "29.99", "39.99", "Home Wellness", "WellnessWorld",
                "https://placehold.co/800x800/fdebd0/333333?text=Soy+Candle",
                200, 20, false, true, false, "MONTHLY:1,WEEKLY:4", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/fdebd0/333333?text=Soy+Candle",
                        "https://placehold.co/800x800/fad7a0/333333?text=Candle+Lit"),
                h.attrs("Wax", "100% Natural Soy",
                        "Wick", "Lead-free cotton",
                        "Burn Time", "55 hours",
                        "Volume", "8 oz jar"),
                h.options1("Scent", "Lavender & Cedar", "Citrus & Sage", "Vanilla & Sandalwood", "Eucalyptus & Mint"),
                p -> {
                    h.pv(p, "Lavender & Cedar",      "WELL-SWC-LAV", new BigDecimal("29.99"), 55);
                    h.pv(p, "Citrus & Sage",          "WELL-SWC-CIT", new BigDecimal("29.99"), 50);
                    h.pv(p, "Vanilla & Sandalwood",   "WELL-SWC-VAN", new BigDecimal("29.99"), 60);
                    h.pv(p, "Eucalyptus & Mint",      "WELL-SWC-EUC", new BigDecimal("29.99"), 45);
                }));

        list.add(h.product(co, "Yoga Mat Premium",
                "6mm natural rubber mat with microfiber suede top. Superior grip wet or dry. Alignment printed guides.",
                "WELL-YGM-001", "69.99", "89.99", "Fitness", "ZenFit",
                "https://placehold.co/800x800/7dcea0/ffffff?text=Yoga+Mat",
                100, 8, true, false, false, null, null,
                h.images("https://placehold.co/800x800/7dcea0/ffffff?text=Yoga+Mat",
                        "https://placehold.co/800x800/58d68d/ffffff?text=Mat+Texture"),
                h.attrs("Material", "Natural rubber base + microfiber suede top",
                        "Thickness", "6mm",
                        "Size", "72\" × 24\"",
                        "Weight", "2.5kg"),
                h.options1("Color", "Forest Green", "Lavender", "Terracotta", "Charcoal"),
                p -> {
                    h.pv(p, "Forest Green", "WELL-YGM-GRN", new BigDecimal("69.99"), 28);
                    h.pv(p, "Lavender",     "WELL-YGM-LAV", new BigDecimal("69.99"), 25);
                    h.pv(p, "Terracotta",   "WELL-YGM-TER", new BigDecimal("69.99"), 22);
                    h.pv(p, "Charcoal",     "WELL-YGM-CHR", new BigDecimal("69.99"), 20);
                }));

        list.add(h.product(co, "Collagen Peptides",
                "Hydrolyzed marine collagen with 10g collagen per serving. Unflavored and flavored varieties.",
                "WELL-COL-001", "49.99", "64.99", "Nutrition", "NutriForge",
                "https://placehold.co/800x800/fef5e7/333333?text=Collagen",
                120, 10, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/fef5e7/333333?text=Collagen",
                        "https://placehold.co/800x800/fdebd0/333333?text=Collagen+Label"),
                h.attrs("Source", "Hydrolyzed marine collagen",
                        "Collagen", "10g per serving",
                        "Servings", "30 per container",
                        "Features", "Paleo, gluten-free, dissolves easily"),
                h.options2("Flavor", "Unflavored", "Mixed Berry", "Lemon", "Size", "200g", "400g", "x"),
                p -> {
                    h.pv2(p, "Unflavored",   "200g", "WELL-COL-UF2", new BigDecimal("34.99"), 40);
                    h.pv2(p, "Unflavored",   "400g", "WELL-COL-UF4", new BigDecimal("49.99"), 35);
                    h.pv2(p, "Mixed Berry",  "200g", "WELL-COL-MB2", new BigDecimal("34.99"), 30);
                    h.pv2(p, "Mixed Berry",  "400g", "WELL-COL-MB4", new BigDecimal("49.99"), 25);
                    h.pv2(p, "Lemon",        "200g", "WELL-COL-LM2", new BigDecimal("34.99"), 28);
                    h.pv2(p, "Lemon",        "400g", "WELL-COL-LM4", new BigDecimal("49.99"), 22);
                }));

        list.add(h.productSingle(co, "Bamboo Cutting Board Set",
                "3-piece organic bamboo cutting board set with juice grooves and hanging holes.",
                "WELL-BCB-001", "39.99", "54.99", "Kitchen", "BrewCraft",
                "https://placehold.co/800x800/f5cba7/333333?text=Bamboo+Board",
                130, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/f5cba7/333333?text=Bamboo+Board",
                        "https://placehold.co/800x800/f0b27a/333333?text=Board+Set"),
                h.attrs("Material", "Organic bamboo",
                        "Set", "3-piece (S/M/L)",
                        "Features", "Juice grooves, hanging holes, anti-slip feet",
                        "Care", "Hand wash, apply food-safe oil periodically")));

        list.add(h.product(co, "Essential Oil Diffuser",
                "400ml ultrasonic diffuser with 7-color ambient light, timer, and auto-off safety.",
                "WELL-EOD-001", "44.99", "59.99", "Home Wellness", "PureAir",
                "https://placehold.co/800x800/d5e8d4/333333?text=Oil+Diffuser",
                110, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/d5e8d4/333333?text=Oil+Diffuser",
                        "https://placehold.co/800x800/c3d9c1/333333?text=Diffuser+Mist"),
                h.attrs("Capacity", "400ml",
                        "Coverage", "Up to 30 sq meters",
                        "Run Time", "Up to 13 hours continuous",
                        "Lights", "7 ambient colors"),
                h.options1("Color", "Ivory", "Blush", "Sage"),
                p -> {
                    h.pv(p, "Ivory", "WELL-EOD-IVR", new BigDecimal("44.99"), 38);
                    h.pv(p, "Blush", "WELL-EOD-BLS", new BigDecimal("44.99"), 35);
                    h.pv(p, "Sage",  "WELL-EOD-SGE", new BigDecimal("44.99"), 30);
                }));

        list.add(h.product(co, "Resistance Band Set",
                "5-band latex resistance set from 10–50lbs with handles, door anchor, and ankle straps.",
                "WELL-RBS-001", "29.99", "39.99", "Fitness", "ZenFit",
                "https://placehold.co/800x800/e74c3c/ffffff?text=Resistance+Bands",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/e74c3c/ffffff?text=Resistance+Bands",
                        "https://placehold.co/800x800/cb4335/ffffff?text=Band+Set"),
                h.attrs("Bands", "5 bands: 10, 20, 30, 40, 50 lbs",
                        "Material", "Natural latex",
                        "Accessories", "Handles, door anchor, ankle straps, carry bag",
                        "Resistance", "10–150 lbs combined"),
                h.options1("Type", "5-Band Starter Set", "5-Band Pro Set", "10-Band Complete Set"),
                p -> {
                    h.pv(p, "5-Band Starter Set",    "WELL-RBS-STR", new BigDecimal("29.99"), 75);
                    h.pv(p, "5-Band Pro Set",         "WELL-RBS-PRO", new BigDecimal("39.99"), 55);
                    h.pv(p, "10-Band Complete Set",   "WELL-RBS-CPL", new BigDecimal("54.99"), 35);
                }));

        list.add(h.product(co, "Sleep Support Gummies",
                "3mg melatonin with L-theanine and chamomile. Vegan, non-GMO, strawberry flavor.",
                "WELL-SSG-001", "34.99", "44.99", "Supplements", "NutriForge",
                "https://placehold.co/800x800/f9ebee/333333?text=Sleep+Gummies",
                150, 15, true, true, false, "MONTHLY:1", new BigDecimal("15.00"),
                h.images("https://placehold.co/800x800/f9ebee/333333?text=Sleep+Gummies",
                        "https://placehold.co/800x800/f2d7d5/333333?text=Gummies+Label"),
                h.attrs("Melatonin", "3mg per serving",
                        "Additional", "L-Theanine 100mg, Chamomile 50mg",
                        "Flavour", "Strawberry",
                        "Diet", "Vegan, Non-GMO, Gluten-free"),
                h.options1("Count", "60 gummies (30-day)", "120 gummies (60-day)"),
                p -> {
                    h.pv(p, "60 gummies (30-day)",  "WELL-SSG-60",  new BigDecimal("34.99"), 70);
                    h.pv(p, "120 gummies (60-day)", "WELL-SSG-120", new BigDecimal("59.99"), 55);
                }));

        list.add(h.product(co, "Foam Roller Deep Tissue",
                "EVA foam roller with textured surface for trigger point release. Available in standard and compact.",
                "WELL-FRL-001", "39.99", "49.99", "Fitness", "ZenFit",
                "https://placehold.co/800x800/2e86c1/ffffff?text=Foam+Roller",
                130, 12, false, false, false, null, null,
                h.images("https://placehold.co/800x800/2e86c1/ffffff?text=Foam+Roller",
                        "https://placehold.co/800x800/2874a6/ffffff?text=Roller+Texture"),
                h.attrs("Material", "High-density EVA foam",
                        "Surface", "Multi-density textured zones",
                        "Weight Capacity", "136kg",
                        "Density", "Firm"),
                h.options1("Size", "Standard (33cm)", "Compact (30cm)"),
                p -> {
                    h.pv(p, "Standard (33cm)", "WELL-FRL-STD", new BigDecimal("39.99"), 65);
                    h.pv(p, "Compact (30cm)",  "WELL-FRL-CMP", new BigDecimal("34.99"), 60);
                }));

        list.add(h.product(co, "Matcha Green Tea Powder",
                "Ceremonial-grade Japanese matcha stone-ground from first-harvest tencha. 70mg caffeine per 2g serving.",
                "WELL-MGT-001", "24.99", "34.99", "Nutrition", "BrewCraft",
                "https://placehold.co/800x800/a9cce3/333333?text=Matcha+Powder",
                160, 15, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/a9cce3/333333?text=Matcha+Powder",
                        "https://placehold.co/800x800/93c6e0/333333?text=Matcha+Bowl"),
                h.attrs("Grade", "Ceremonial, first harvest",
                        "Origin", "Uji, Japan",
                        "Caffeine", "~70mg per 2g serving",
                        "Serving", "30 servings per tin"),
                h.options1("Size", "30g tin", "60g tin"),
                p -> {
                    h.pv(p, "30g tin", "WELL-MGT-30", new BigDecimal("24.99"), 80);
                    h.pv(p, "60g tin", "WELL-MGT-60", new BigDecimal("44.99"), 70);
                }));

        list.add(h.product(co, "Stainless Insulated Tumbler",
                "Triple-wall vacuum insulation keeps drinks cold 24h or hot 12h. Sweat-free exterior and leak-proof lid.",
                "WELL-SIT-001", "34.99", "44.99", "Kitchen", "BrewCraft",
                "https://placehold.co/800x800/1a5276/ffffff?text=Insulated+Tumbler",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a5276/ffffff?text=Insulated+Tumbler",
                        "https://placehold.co/800x800/154360/ffffff?text=Tumbler+Lid"),
                h.attrs("Insulation", "Triple-wall vacuum",
                        "Cold", "24 hours",
                        "Hot", "12 hours",
                        "BPA Free", "Yes, food-grade 18/8 stainless"),
                h.options2("Color", "Slate", "Rose Gold", "Sage", "Size", "20oz", "32oz", "x"),
                p -> {
                    h.pv2(p, "Slate",     "20oz", "WELL-SIT-SL20", new BigDecimal("29.99"), 40);
                    h.pv2(p, "Slate",     "32oz", "WELL-SIT-SL32", new BigDecimal("34.99"), 35);
                    h.pv2(p, "Rose Gold", "20oz", "WELL-SIT-RG20", new BigDecimal("29.99"), 38);
                    h.pv2(p, "Rose Gold", "32oz", "WELL-SIT-RG32", new BigDecimal("34.99"), 32);
                    h.pv2(p, "Sage",      "20oz", "WELL-SIT-SG20", new BigDecimal("29.99"), 35);
                    h.pv2(p, "Sage",      "32oz", "WELL-SIT-SG32", new BigDecimal("34.99"), 30);
                }));

        list.add(h.product(co, "Probiotics 60 Billion CFU",
                "20-strain probiotic blend with prebiotics for gut health and immune support. Shelf-stable, no refrigeration needed.",
                "WELL-PRB-001", "44.99", "59.99", "Supplements", "NutriForge",
                "https://placehold.co/800x800/e8f8f5/333333?text=Probiotics",
                130, 12, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/e8f8f5/333333?text=Probiotics",
                        "https://placehold.co/800x800/d5f5e3/333333?text=Probiotic+Label"),
                h.attrs("CFU", "60 Billion per serving",
                        "Strains", "20 probiotic strains",
                        "Prebiotics", "Organic inulin",
                        "Storage", "Shelf-stable, no refrigeration"),
                h.options1("Count", "30 capsules (30-day)", "60 capsules (60-day)"),
                p -> {
                    h.pv(p, "30 capsules (30-day)", "WELL-PRB-30", new BigDecimal("44.99"), 65);
                    h.pv(p, "60 capsules (60-day)", "WELL-PRB-60", new BigDecimal("79.99"), 50);
                }));

        list.add(h.productSingle(co, "LED Therapy Face Mask",
                "7-color photon LED mask targeting anti-aging, acne, and pigmentation. FDA-cleared, clinic-grade.",
                "WELL-LED-001", "89.99", "119.99", "Skincare", "GlowLab",
                "https://placehold.co/800x800/f8d7da/333333?text=LED+Face+Mask",
                55, 5, true, false, false, null, null,
                h.images("https://placehold.co/800x800/f8d7da/333333?text=LED+Face+Mask",
                        "https://placehold.co/800x800/f5c6cb/333333?text=Mask+Modes"),
                h.attrs("LEDs", "150 individual LEDs, 7 wavelengths",
                        "Colors", "Red, Blue, Green, Yellow, Cyan, Purple, White",
                        "FDA", "FDA-cleared Class II",
                        "Session Time", "10–20 minutes")));

        list.add(h.product(co, "Meditation Cushion",
                "Buckwheat hull zafu meditation cushion with organic cotton cover. Removable and washable.",
                "WELL-MED-001", "49.99", "64.99", "Fitness", "ZenFit",
                "https://placehold.co/800x800/d7bde2/333333?text=Meditation+Cushion",
                100, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/d7bde2/333333?text=Meditation+Cushion",
                        "https://placehold.co/800x800/c39bd3/333333?text=Cushion+Set"),
                h.attrs("Fill", "Organic buckwheat hulls",
                        "Cover", "Organic cotton, removable",
                        "Height", "~15cm when filled",
                        "Diameter", "33cm"),
                h.options1("Color", "Indigo", "Natural", "Terracotta", "Sage"),
                p -> {
                    h.pv(p, "Indigo",     "WELL-MED-IND", new BigDecimal("49.99"), 28);
                    h.pv(p, "Natural",    "WELL-MED-NAT", new BigDecimal("49.99"), 25);
                    h.pv(p, "Terracotta", "WELL-MED-TER", new BigDecimal("49.99"), 22);
                    h.pv(p, "Sage",       "WELL-MED-SGE", new BigDecimal("49.99"), 20);
                }));

        list.add(h.product(co, "Shea Butter Body Lotion",
                "Rich whipped shea butter lotion with jojoba and vitamin E. Absorbs quickly, no greasy residue.",
                "WELL-SBL-001", "24.99", "32.99", "Skincare", "GlowLab",
                "https://placehold.co/800x800/fef9e7/333333?text=Shea+Lotion",
                200, 20, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/fef9e7/333333?text=Shea+Lotion",
                        "https://placehold.co/800x800/fdf2d0/333333?text=Lotion+Pour"),
                h.attrs("Key Ingredients", "Shea butter, jojoba oil, vitamin E",
                        "Size", "250ml",
                        "Skin Type", "All skin types",
                        "Fragrance", "Light natural scent"),
                h.options1("Scent", "Unscented", "Lavender", "Vanilla Coconut"),
                p -> {
                    h.pv(p, "Unscented",       "WELL-SBL-UNS", new BigDecimal("24.99"), 70);
                    h.pv(p, "Lavender",        "WELL-SBL-LAV", new BigDecimal("24.99"), 65);
                    h.pv(p, "Vanilla Coconut", "WELL-SBL-VAN", new BigDecimal("24.99"), 60);
                }));

        list.add(h.product(co, "Cold Process Soap Bar Set",
                "4-pack artisan cold-process soap with plant-based oils. Palm-oil free, natural fragrance.",
                "WELL-SOP-001", "19.99", "26.99", "Skincare", "GlowLab",
                "https://placehold.co/800x800/fdebd0/333333?text=Soap+Bars",
                200, 20, false, true, false, "MONTHLY:1", new BigDecimal("10.00"),
                h.images("https://placehold.co/800x800/fdebd0/333333?text=Soap+Bars",
                        "https://placehold.co/800x800/fad7a0/333333?text=Soap+Detail"),
                h.attrs("Process", "Cold process",
                        "Base Oils", "Olive, coconut, castor (palm-free)",
                        "Pack", "4 bars, 100g each",
                        "Shelf Life", "12 months"),
                h.options1("Scent", "Lavender & Oat", "Mint & Charcoal", "Rose Clay", "Citrus Burst"),
                p -> {
                    h.pv(p, "Lavender & Oat",  "WELL-SOP-LAV", new BigDecimal("19.99"), 55);
                    h.pv(p, "Mint & Charcoal", "WELL-SOP-MNT", new BigDecimal("19.99"), 50);
                    h.pv(p, "Rose Clay",       "WELL-SOP-RSE", new BigDecimal("19.99"), 48);
                    h.pv(p, "Citrus Burst",    "WELL-SOP-CIT", new BigDecimal("19.99"), 45);
                }));

        list.add(h.product(co, "Multivitamin Daily Pack",
                "Comprehensive 23-nutrient daily pack with methylated B vitamins and chelated minerals. AM/PM pouches.",
                "WELL-MVI-001", "39.99", "54.99", "Supplements", "NutriForge",
                "https://placehold.co/800x800/e8f4f8/333333?text=Multivitamin",
                160, 15, true, true, false, "MONTHLY:1", new BigDecimal("15.00"),
                h.images("https://placehold.co/800x800/e8f4f8/333333?text=Multivitamin",
                        "https://placehold.co/800x800/d6eaf8/333333?text=Vitamin+Pack"),
                h.attrs("Nutrients", "23 vitamins and minerals",
                        "Form", "AM + PM pouch system",
                        "B Vitamins", "Methylated forms (more bioavailable)",
                        "Diet", "Non-GMO, gluten-free, no artificial colors"),
                h.options1("Count", "30-day supply", "60-day supply"),
                p -> {
                    h.pv(p, "30-day supply", "WELL-MVI-30", new BigDecimal("39.99"), 80);
                    h.pv(p, "60-day supply", "WELL-MVI-60", new BigDecimal("69.99"), 65);
                }));

        return list;
    }
}
