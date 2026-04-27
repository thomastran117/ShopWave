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
public class StyleHubProductSeeder {

    private final ProductSeedHelper h;

    public List<Product> seed(Company co) {
        List<Product> list = new ArrayList<>();

        list.add(h.product(co, "Premium Organic Cotton T-Shirt",
                "Certified GOTS organic cotton. Relaxed everyday fit with reinforced shoulder seams. Pre-shrunk.",
                "STYLE-TCO-001", "34.99", "44.99", "Clothing", "EcoThread",
                "https://placehold.co/800x800/f5cba7/333333?text=Organic+Tee",
                300, 20, true, false, false, null, null,
                h.images("https://placehold.co/800x800/f5cba7/333333?text=Organic+Tee",
                        "https://placehold.co/800x800/f0b27a/333333?text=Tee+Back",
                        "https://placehold.co/800x800/e59866/333333?text=Tee+Detail"),
                h.attrs("Material", "100% GOTS Organic Cotton",
                        "Fit", "Relaxed",
                        "Care", "Machine wash cold, tumble dry low",
                        "Certification", "GOTS certified"),
                h.options2("Size", "XS", "S", "M", "Color", "White", "Black", "Navy"),
                p -> h.addSizeColorVariants(p, "STYLE-TCO",
                        new String[]{"XS", "S", "M", "L", "XL"},
                        new String[]{"White", "Black", "Navy"},
                        "34.99", 20)));

        list.add(h.product(co, "Slim-Fit Stretch Denim Jeans",
                "2% elastane stretch denim for all-day comfort. Classic 5-pocket with YKK zipper.",
                "STYLE-DNM-001", "79.99", "99.99", "Clothing", "DenimCraft",
                "https://placehold.co/800x800/2471a3/ffffff?text=Denim+Jeans",
                200, 15, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2471a3/ffffff?text=Denim+Jeans",
                        "https://placehold.co/800x800/1f618d/ffffff?text=Jeans+Back"),
                h.attrs("Material", "98% Cotton, 2% Elastane",
                        "Fit", "Slim",
                        "Rise", "Mid-rise",
                        "Care", "Machine wash cold, hang dry"),
                h.options2("Waist", "30\"", "32\"", "34\"", "Inseam", "30\"", "32\"", "x"),
                p -> h.addSizeColorVariants(p, "STYLE-DNM",
                        new String[]{"30\"x30\"", "30\"x32\"", "32\"x30\"", "32\"x32\"", "34\"x30\"", "34\"x32\""},
                        new String[]{"Dark Wash", "Medium Wash", "Light Wash"},
                        "79.99", 11)));

        list.add(h.product(co, "Heavyweight Zip Hoodie",
                "500gsm French terry fleece with a front kangaroo pocket and YKK full-zip. Brushed interior for warmth.",
                "STYLE-HZH-001", "89.99", "109.99", "Clothing", "CozyWear",
                "https://placehold.co/800x800/5d6d7e/ffffff?text=Zip+Hoodie",
                200, 15, true, false, false, null, null,
                h.images("https://placehold.co/800x800/5d6d7e/ffffff?text=Zip+Hoodie",
                        "https://placehold.co/800x800/4d5d6e/ffffff?text=Hoodie+Front"),
                h.attrs("Material", "500gsm French Terry (80% Cotton, 20% Polyester)",
                        "Interior", "Soft brushed fleece",
                        "Zipper", "YKK full-zip",
                        "Care", "Machine wash warm"),
                h.options2("Size", "S", "M", "L", "Color", "Charcoal", "Burgundy", "Navy"),
                p -> h.addSizeColorVariants(p, "STYLE-HZH",
                        new String[]{"S", "M", "L", "XL"},
                        new String[]{"Charcoal", "Burgundy", "Navy"},
                        "89.99", 17)));

        list.add(h.product(co, "Quick-Dry Running Shorts",
                "4-way stretch fabric with moisture-wicking liner and back zip pocket. Reflective details for low-light runs.",
                "STYLE-QRS-001", "39.99", "49.99", "Active", "SwiftGear",
                "https://placehold.co/800x800/1abc9c/ffffff?text=Run+Shorts",
                250, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1abc9c/ffffff?text=Run+Shorts",
                        "https://placehold.co/800x800/17a589/ffffff?text=Shorts+Back"),
                h.attrs("Material", "88% Polyester, 12% Spandex",
                        "Features", "4-way stretch, moisture-wicking, UPF 30",
                        "Inseam", "5\" or 7\" option",
                        "Pockets", "2 side + 1 back zip"),
                h.options2("Size", "XS", "S", "M", "Color", "Teal", "Black", "Coral"),
                p -> h.addSizeColorVariants(p, "STYLE-QRS",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Teal", "Black", "Coral"},
                        "39.99", 21)));

        list.add(h.product(co, "Classic Canvas Sneakers",
                "Vulcanized canvas upper with rubber cupsole. Ortholite insole for all-day cushioning.",
                "STYLE-SNK-001", "64.99", "84.99", "Footwear", "StepCraft",
                "https://placehold.co/800x800/f9ebea/333333?text=Canvas+Sneakers",
                180, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/f9ebea/333333?text=Canvas+Sneakers",
                        "https://placehold.co/800x800/f2d7d5/333333?text=Sneaker+Side",
                        "https://placehold.co/800x800/ebdef0/333333?text=Sneaker+Sole"),
                h.attrs("Upper", "100% Canvas",
                        "Sole", "Vulcanized rubber cupsole",
                        "Insole", "Ortholite removable",
                        "Closure", "Lace-up"),
                h.options1("Size", "6", "7", "8", "9"),
                p -> {
                    h.pv(p, "6", "STYLE-SNK-6", new BigDecimal("64.99"), 22);
                    h.pv(p, "7", "STYLE-SNK-7", new BigDecimal("64.99"), 28);
                    h.pv(p, "8", "STYLE-SNK-8", new BigDecimal("64.99"), 35);
                    h.pv(p, "9", "STYLE-SNK-9", new BigDecimal("64.99"), 30);
                }));

        list.add(h.product(co, "Linen Button-Down Shirt",
                "Breathable European linen with a relaxed camp collar. Perfect for warm weather.",
                "STYLE-LBS-001", "54.99", "69.99", "Clothing", "EcoThread",
                "https://placehold.co/800x800/fad7a0/333333?text=Linen+Shirt",
                150, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/fad7a0/333333?text=Linen+Shirt",
                        "https://placehold.co/800x800/f8c471/333333?text=Shirt+Back"),
                h.attrs("Material", "100% European Linen",
                        "Collar", "Camp collar",
                        "Fit", "Relaxed",
                        "Care", "Machine wash cold, line dry"),
                h.options2("Size", "S", "M", "L", "Color", "Natural", "Sky Blue", "Sage"),
                p -> h.addSizeColorVariants(p, "STYLE-LBS",
                        new String[]{"S", "M", "L", "XL"},
                        new String[]{"Natural", "Sky Blue", "Sage"},
                        "54.99", 13)));

        list.add(h.product(co, "Yoga Leggings",
                "Buttery-soft 4-way stretch fabric with high waist and hidden waistband pocket. Squat-proof.",
                "STYLE-YGL-001", "49.99", "64.99", "Active", "SwiftGear",
                "https://placehold.co/800x800/7d3c98/ffffff?text=Yoga+Leggings",
                200, 15, true, false, false, null, null,
                h.images("https://placehold.co/800x800/7d3c98/ffffff?text=Yoga+Leggings",
                        "https://placehold.co/800x800/6c3483/ffffff?text=Leggings+Side"),
                h.attrs("Material", "79% Nylon, 21% Spandex",
                        "Waist", "High-waist with hidden pocket",
                        "Length", "Full length 28\" inseam",
                        "Features", "Squat-proof, moisture-wicking"),
                h.options2("Size", "XS", "S", "M", "Color", "Plum", "Midnight", "Dusty Rose"),
                p -> h.addSizeColorVariants(p, "STYLE-YGL",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Plum", "Midnight", "Dusty Rose"},
                        "49.99", 17)));

        list.add(h.product(co, "Wool Blend Coat",
                "Italian 60% wool blend with satin lining. Double-breasted silhouette with hidden button closure.",
                "STYLE-WBC-001", "189.99", "249.99", "Outerwear", "LuxLayer",
                "https://placehold.co/800x800/2c3e50/ffffff?text=Wool+Coat",
                80, 8, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2c3e50/ffffff?text=Wool+Coat",
                        "https://placehold.co/800x800/273746/ffffff?text=Coat+Lining"),
                h.attrs("Material", "60% Wool, 30% Polyester, 10% Other",
                        "Lining", "100% Satin",
                        "Closure", "Double-breasted, hidden buttons",
                        "Care", "Dry clean only"),
                h.options2("Size", "XS", "S", "M", "Color", "Camel", "Black", "x"),
                p -> h.addSizeColorVariants(p, "STYLE-WBC",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Camel", "Black"},
                        "189.99", 10)));

        list.add(h.product(co, "Leather Belt Classic",
                "Full-grain vegetable-tanned leather with a solid brass buckle. Develops a rich patina over time.",
                "STYLE-BLT-001", "44.99", "59.99", "Accessories", "LeatherCraft",
                "https://placehold.co/800x800/784212/ffffff?text=Leather+Belt",
                120, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/784212/ffffff?text=Leather+Belt",
                        "https://placehold.co/800x800/6e2f1a/ffffff?text=Belt+Buckle"),
                h.attrs("Material", "Full-grain vegetable-tanned leather",
                        "Width", "35mm",
                        "Buckle", "Solid brass",
                        "Sizes", "Available 30\"–42\""),
                h.options1("Size", "30\"–32\"", "33\"–35\"", "36\"–38\"", "39\"–42\""),
                p -> {
                    h.pv(p, "30\"–32\"", "STYLE-BLT-S",  new BigDecimal("44.99"), 30);
                    h.pv(p, "33\"–35\"", "STYLE-BLT-M",  new BigDecimal("44.99"), 35);
                    h.pv(p, "36\"–38\"", "STYLE-BLT-L",  new BigDecimal("44.99"), 28);
                    h.pv(p, "39\"–42\"", "STYLE-BLT-XL", new BigDecimal("44.99"), 22);
                }));

        list.add(h.product(co, "Baseball Cap",
                "Unstructured 6-panel cap in washed cotton twill. Adjustable strapback closure.",
                "STYLE-CAP-001", "29.99", "39.99", "Accessories", "StyleHub",
                "https://placehold.co/800x800/935116/ffffff?text=Baseball+Cap",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/935116/ffffff?text=Baseball+Cap",
                        "https://placehold.co/800x800/7b4510/ffffff?text=Cap+Back"),
                h.attrs("Material", "100% Washed Cotton Twill",
                        "Panel", "6-panel unstructured",
                        "Closure", "Adjustable strapback",
                        "One Size", "Fits most"),
                h.options1("Color", "Khaki", "Black", "Navy", "Sage"),
                p -> {
                    h.pv(p, "Khaki", "STYLE-CAP-KHK", new BigDecimal("29.99"), 55);
                    h.pv(p, "Black", "STYLE-CAP-BLK", new BigDecimal("29.99"), 60);
                    h.pv(p, "Navy",  "STYLE-CAP-NVY", new BigDecimal("29.99"), 48);
                    h.pv(p, "Sage",  "STYLE-CAP-SGE", new BigDecimal("29.99"), 40);
                }));

        list.add(h.product(co, "Crew Neck Sweatshirt",
                "Garment-dyed 380gsm cotton fleece with ribbed cuffs and hem. Slightly oversized fit.",
                "STYLE-CRS-001", "59.99", "79.99", "Clothing", "CozyWear",
                "https://placehold.co/800x800/717d7e/ffffff?text=Crewneck",
                180, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/717d7e/ffffff?text=Crewneck",
                        "https://placehold.co/800x800/626a6a/ffffff?text=Crew+Detail"),
                h.attrs("Material", "380gsm 100% Cotton Fleece",
                        "Dye", "Garment-dyed for vintage look",
                        "Fit", "Slightly oversized",
                        "Care", "Machine wash cold, inside out"),
                h.options2("Size", "S", "M", "L", "Color", "Vintage Black", "Washed Olive", "Faded Blue"),
                p -> h.addSizeColorVariants(p, "STYLE-CRS",
                        new String[]{"S", "M", "L", "XL"},
                        new String[]{"Vintage Black", "Washed Olive", "Faded Blue"},
                        "59.99", 15)));

        list.add(h.product(co, "Floral Sundress",
                "Lightweight viscose with adjustable spaghetti straps and a tiered midi silhouette.",
                "STYLE-FSD-001", "69.99", "89.99", "Clothing", "EcoThread",
                "https://placehold.co/800x800/f1948a/ffffff?text=Sundress",
                160, 12, false, false, false, null, null,
                h.images("https://placehold.co/800x800/f1948a/ffffff?text=Sundress",
                        "https://placehold.co/800x800/ec7063/ffffff?text=Dress+Detail"),
                h.attrs("Material", "100% ECOVERO Viscose",
                        "Length", "Midi (knee to calf)",
                        "Straps", "Adjustable spaghetti straps",
                        "Care", "Hand wash or delicate cycle"),
                h.options2("Size", "XS", "S", "M", "Color", "Garden Floral", "Tropical", "Vintage Bloom"),
                p -> h.addSizeColorVariants(p, "STYLE-FSD",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Garden Floral", "Tropical", "Vintage Bloom"},
                        "69.99", 14)));

        list.add(h.product(co, "Chino Trousers",
                "Stretch cotton chino with slim straight cut and YKK zip fly. Versatile from office to weekend.",
                "STYLE-CHN-001", "64.99", "84.99", "Clothing", "DenimCraft",
                "https://placehold.co/800x800/d5dbdb/333333?text=Chino+Trousers",
                170, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/d5dbdb/333333?text=Chino+Trousers",
                        "https://placehold.co/800x800/ccd1d1/333333?text=Chino+Back"),
                h.attrs("Material", "97% Cotton, 3% Elastane",
                        "Fit", "Slim straight",
                        "Rise", "Mid-rise",
                        "Care", "Machine wash cold"),
                h.options2("Waist", "30\"", "32\"", "34\"", "Color", "Khaki", "Olive", "Navy"),
                p -> h.addSizeColorVariants(p, "STYLE-CHN",
                        new String[]{"30\"", "32\"", "34\""},
                        new String[]{"Khaki", "Olive", "Navy"},
                        "64.99", 19)));

        list.add(h.product(co, "Puffer Jacket",
                "700-fill RDS-certified down with a DWR shell. Packable into its own chest pocket.",
                "STYLE-PJK-001", "119.99", "149.99", "Outerwear", "LuxLayer",
                "https://placehold.co/800x800/1a5276/ffffff?text=Puffer+Jacket",
                130, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a5276/ffffff?text=Puffer+Jacket",
                        "https://placehold.co/800x800/154360/ffffff?text=Jacket+Pack"),
                h.attrs("Fill", "700-fill RDS down",
                        "Shell", "Ripstop nylon with DWR finish",
                        "Packable", "Yes, into own chest pocket",
                        "Care", "Machine wash cold, tumble dry low"),
                h.options2("Size", "XS", "S", "M", "Color", "Navy", "Black", "Hunter Green"),
                p -> h.addSizeColorVariants(p, "STYLE-PJK",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Navy", "Black", "Hunter Green"},
                        "119.99", 11)));

        list.add(h.product(co, "Athletic Socks 6-Pack",
                "Arch-support crew socks with cushioned sole and anti-blister heel tab. Wicks moisture.",
                "STYLE-SCK-001", "19.99", "24.99", "Accessories", "SwiftGear",
                "https://placehold.co/800x800/ecf0f1/333333?text=Athletic+Socks",
                300, 30, false, false, false, null, null,
                h.images("https://placehold.co/800x800/ecf0f1/333333?text=Athletic+Socks",
                        "https://placehold.co/800x800/d5dbdb/333333?text=Sock+Detail"),
                h.attrs("Material", "75% Cotton, 20% Nylon, 5% Spandex",
                        "Pack", "6 pairs",
                        "Features", "Arch support, anti-blister tab, moisture-wicking",
                        "Care", "Machine wash warm"),
                h.options1("Size", "S/M (US 4–8)", "L/XL (US 9–13)", "XL (US 13+)"),
                p -> {
                    h.pv(p, "S/M (US 4–8)",   "STYLE-SCK-SM", new BigDecimal("19.99"), 120);
                    h.pv(p, "L/XL (US 9–13)", "STYLE-SCK-LX", new BigDecimal("19.99"), 100);
                    h.pv(p, "XL (US 13+)",    "STYLE-SCK-XL", new BigDecimal("19.99"),  60);
                }));

        list.add(h.product(co, "Tote Canvas Bag",
                "Heavy 12oz canvas tote with reinforced handles and an interior zipper pocket. Holds 20L.",
                "STYLE-TOT-001", "39.99", "49.99", "Accessories", "EcoThread",
                "https://placehold.co/800x800/d7ccc8/333333?text=Canvas+Tote",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/d7ccc8/333333?text=Canvas+Tote",
                        "https://placehold.co/800x800/bcaaa4/333333?text=Tote+Inside"),
                h.attrs("Material", "12oz Canvas",
                        "Capacity", "20L",
                        "Handles", "Reinforced cotton webbing",
                        "Pockets", "1 interior zip + 2 exterior slip"),
                h.options1("Color", "Natural", "Black", "Olive", "Terracotta"),
                p -> {
                    h.pv(p, "Natural",    "STYLE-TOT-NAT", new BigDecimal("39.99"), 55);
                    h.pv(p, "Black",      "STYLE-TOT-BLK", new BigDecimal("39.99"), 60);
                    h.pv(p, "Olive",      "STYLE-TOT-OLV", new BigDecimal("39.99"), 45);
                    h.pv(p, "Terracotta", "STYLE-TOT-TER", new BigDecimal("39.99"), 40);
                }));

        list.add(h.product(co, "Knit Beanie",
                "100% Merino wool ribbed beanie with a turn-up cuff. Naturally temperature-regulating.",
                "STYLE-BNE-001", "24.99", "34.99", "Accessories", "CozyWear",
                "https://placehold.co/800x800/884ea0/ffffff?text=Knit+Beanie",
                250, 25, false, false, false, null, null,
                h.images("https://placehold.co/800x800/884ea0/ffffff?text=Knit+Beanie",
                        "https://placehold.co/800x800/76448a/ffffff?text=Beanie+Detail"),
                h.attrs("Material", "100% Merino Wool",
                        "Rib", "2×2 ribbed knit",
                        "Cuff", "Turn-up cuff",
                        "One Size", "Stretchy fit"),
                h.options1("Color", "Charcoal", "Cream", "Rust", "Forest", "Navy"),
                p -> {
                    h.pv(p, "Charcoal", "STYLE-BNE-CHR", new BigDecimal("24.99"), 55);
                    h.pv(p, "Cream",    "STYLE-BNE-CRM", new BigDecimal("24.99"), 50);
                    h.pv(p, "Rust",     "STYLE-BNE-RST", new BigDecimal("24.99"), 45);
                    h.pv(p, "Forest",   "STYLE-BNE-FOR", new BigDecimal("24.99"), 48);
                    h.pv(p, "Navy",     "STYLE-BNE-NVY", new BigDecimal("24.99"), 50);
                }));

        list.add(h.product(co, "Compression Shorts",
                "8\" inseam compression shorts with 4-way stretch and flat-lock seams to prevent chafing.",
                "STYLE-CMP-001", "34.99", "44.99", "Active", "SwiftGear",
                "https://placehold.co/800x800/212f3c/ffffff?text=Compression+Short",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/212f3c/ffffff?text=Compression+Short",
                        "https://placehold.co/800x800/1c2833/ffffff?text=Comp+Detail"),
                h.attrs("Material", "82% Nylon, 18% Spandex",
                        "Inseam", "8\"",
                        "Features", "Flat-lock seams, 4-way stretch",
                        "Care", "Machine wash cold"),
                h.options2("Size", "XS", "S", "M", "Color", "Black", "Navy", "x"),
                p -> h.addSizeColorVariants(p, "STYLE-CMP",
                        new String[]{"XS", "S", "M", "L"},
                        new String[]{"Black", "Navy"},
                        "34.99", 25)));

        list.add(h.product(co, "Flannel Pajama Set",
                "Brushed cotton flannel shirt and pants set. Button-front top with chest pocket.",
                "STYLE-PJM-001", "54.99", "69.99", "Loungewear", "CozyWear",
                "https://placehold.co/800x800/c0392b/ffffff?text=Flannel+PJ+Set",
                180, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/c0392b/ffffff?text=Flannel+PJ+Set",
                        "https://placehold.co/800x800/a93226/ffffff?text=PJ+Detail"),
                h.attrs("Material", "100% Brushed Cotton Flannel",
                        "Set", "Long-sleeve top + full-length pants",
                        "Top", "Button-front with chest pocket",
                        "Care", "Machine wash warm"),
                h.options2("Size", "S", "M", "L", "Color", "Buffalo Plaid", "Houndstooth", "Solid Navy"),
                p -> h.addSizeColorVariants(p, "STYLE-PJM",
                        new String[]{"S", "M", "L", "XL"},
                        new String[]{"Buffalo Plaid", "Houndstooth", "Solid Navy"},
                        "54.99", 15)));

        list.add(h.product(co, "Swimwear Board Shorts",
                "Quick-dry recycled polyester boardshorts with UPF 50+ and side cargo pocket.",
                "STYLE-BSH-001", "44.99", "54.99", "Swimwear", "SwiftGear",
                "https://placehold.co/800x800/1e8bc3/ffffff?text=Board+Shorts",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1e8bc3/ffffff?text=Board+Shorts",
                        "https://placehold.co/800x800/1a78aa/ffffff?text=Shorts+Detail"),
                h.attrs("Material", "100% Recycled Polyester",
                        "Length", "18\" outseam",
                        "Protection", "UPF 50+",
                        "Pockets", "2 side + 1 cargo"),
                h.options2("Size", "28\"", "30\"", "32\"", "Color", "Island Blue", "Sunset Orange", "x"),
                p -> h.addSizeColorVariants(p, "STYLE-BSH",
                        new String[]{"28\"", "30\"", "32\"", "34\""},
                        new String[]{"Island Blue", "Sunset Orange"},
                        "44.99", 25)));

        return list;
    }
}
