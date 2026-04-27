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
public class TechGadgetsProductSeeder {

    private final ProductSeedHelper h;

    public List<Product> seed(Company co) {
        List<Product> list = new ArrayList<>();

        list.add(h.product(co, "Wireless Noise-Cancelling Headphones",
                "Studio-quality sound with 40-hour battery life and adaptive ANC. Premium over-ear design with memory foam cushions.",
                "TECH-WNC-HDR-001", "149.99", "199.99", "Electronics", "SoundCore",
                "https://placehold.co/800x800/1a1a2e/ffffff?text=WNC+Headphones",
                120, 10, true, false, false, null, null,
                h.images("https://placehold.co/800x800/1a1a2e/ffffff?text=WNC+Headphones",
                        "https://placehold.co/800x800/16213e/ffffff?text=WNC+Side",
                        "https://placehold.co/800x800/0f3460/ffffff?text=WNC+Case"),
                h.attrs("Driver Size", "40mm dynamic drivers",
                        "Battery Life", "40 hours ANC on / 60 hours off",
                        "Weight", "250g",
                        "Connectivity", "Bluetooth 5.3, USB-C, 3.5mm"),
                h.options1("Color", "Midnight Black", "Silver Grey"),
                p -> {
                    h.pv(p, "Midnight Black", "TECH-WNC-BLK", new BigDecimal("149.99"), 60);
                    h.pv(p, "Silver Grey",    "TECH-WNC-SLV", new BigDecimal("149.99"), 55);
                }));

        list.add(h.product(co, "Smart Watch Series X",
                "Advanced health monitoring with ECG, SpO2, GPS, and 18-day battery. Durable sapphire crystal display.",
                "TECH-SWX-001", "299.99", "349.99", "Electronics", "ChronoTech",
                "https://placehold.co/800x800/2d3561/ffffff?text=Smart+Watch",
                80, 8, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2d3561/ffffff?text=Smart+Watch",
                        "https://placehold.co/800x800/1a1a2e/ffffff?text=Watch+Band",
                        "https://placehold.co/800x800/16213e/ffffff?text=Watch+Face"),
                h.attrs("Display", "1.9\" AMOLED Always-On",
                        "Battery", "18-day typical, 60-hour GPS",
                        "Water Resistance", "5 ATM",
                        "Sensors", "ECG, SpO2, accelerometer, GPS"),
                h.options2("Size", "42mm", "46mm", "Color", "Midnight", "Starlight"),
                p -> {
                    h.pv2(p, "42mm", "Midnight",    "TECH-SWX-42M", new BigDecimal("299.99"), 20);
                    h.pv2(p, "42mm", "Starlight",   "TECH-SWX-42S", new BigDecimal("299.99"), 18);
                    h.pv2(p, "42mm", "Product Red", "TECH-SWX-42R", new BigDecimal("299.99"), 10);
                    h.pv2(p, "46mm", "Midnight",    "TECH-SWX-46M", new BigDecimal("319.99"), 15);
                    h.pv2(p, "46mm", "Starlight",   "TECH-SWX-46S", new BigDecimal("319.99"), 12);
                    h.pv2(p, "46mm", "Product Red", "TECH-SWX-46R", new BigDecimal("319.99"), 8);
                }));

        list.add(h.product(co, "Portable Bluetooth Speaker",
                "360° immersive sound with IP67 waterproofing. 24-hour playtime and built-in power bank.",
                "TECH-PBS-001", "79.99", "99.99", "Electronics", "SoundCore",
                "https://placehold.co/800x800/1b4f72/ffffff?text=BT+Speaker",
                150, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1b4f72/ffffff?text=BT+Speaker",
                        "https://placehold.co/800x800/154360/ffffff?text=Speaker+Side"),
                h.attrs("Output Power", "30W",
                        "Battery", "24 hours",
                        "Water Rating", "IP67",
                        "Dimensions", "18 × 7.5 × 7.5 cm"),
                h.options1("Color", "Ocean Blue", "Charcoal", "Forest Green"),
                p -> {
                    h.pv(p, "Ocean Blue",    "TECH-PBS-BLU", new BigDecimal("79.99"), 55);
                    h.pv(p, "Charcoal",      "TECH-PBS-CHR", new BigDecimal("79.99"), 50);
                    h.pv(p, "Forest Green",  "TECH-PBS-GRN", new BigDecimal("79.99"), 45);
                }));

        list.add(h.productSingle(co, "USB-C 7-in-1 Hub",
                "Expand your laptop with 4K HDMI, 100W PD, 2× USB-A, SD, microSD, and Gigabit Ethernet.",
                "TECH-HUB-001", "49.99", "69.99", "Electronics", "NexPort",
                "https://placehold.co/800x800/212f3c/ffffff?text=USB+Hub",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/212f3c/ffffff?text=USB+Hub",
                        "https://placehold.co/800x800/1c2833/ffffff?text=Hub+Ports"),
                h.attrs("Ports", "HDMI 4K, 2× USB-A 3.0, SD, microSD, GbE, 100W PD",
                        "Max Video", "4K@60Hz",
                        "Cable Length", "20cm",
                        "Compatibility", "USB-C laptops, tablets")));

        list.add(h.product(co, "Mechanical Keyboard TKL",
                "Tenkeyless form factor with hot-swappable switches, per-key RGB, and anodized aluminum top case.",
                "TECH-MKB-001", "129.99", "159.99", "Electronics", "KeyForge",
                "https://placehold.co/800x800/1a252f/ffffff?text=Mech+Keyboard",
                90, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a252f/ffffff?text=Mech+Keyboard",
                        "https://placehold.co/800x800/17202a/ffffff?text=KB+Close"),
                h.attrs("Layout", "87-key TKL",
                        "Switches", "Hot-swappable 5-pin",
                        "Backlight", "Per-key RGB",
                        "Case", "Anodized aluminum top, ABS bottom"),
                h.options1("Switch", "Red (Linear)", "Brown (Tactile)", "Blue (Clicky)"),
                p -> {
                    h.pv(p, "Red (Linear)",    "TECH-MKB-RED", new BigDecimal("129.99"), 35);
                    h.pv(p, "Brown (Tactile)",  "TECH-MKB-BRN", new BigDecimal("129.99"), 30);
                    h.pv(p, "Blue (Clicky)",    "TECH-MKB-BLU", new BigDecimal("129.99"), 25);
                }));

        list.add(h.productSingle(co, "4K Webcam Pro",
                "Sony STARVIS sensor with 4K@30fps, auto-framing AI, dual noise-cancelling mics, and privacy shutter.",
                "TECH-WEB-001", "99.99", "129.99", "Electronics", "ClearCast",
                "https://placehold.co/800x800/2e4057/ffffff?text=4K+Webcam",
                75, 8, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2e4057/ffffff?text=4K+Webcam",
                        "https://placehold.co/800x800/273746/ffffff?text=Webcam+Side"),
                h.attrs("Sensor", "Sony STARVIS 1/2.8\"",
                        "Resolution", "4K@30fps / 1080p@60fps",
                        "Field of View", "90° adjustable",
                        "Mics", "Dual omni-directional with AI noise cancellation")));

        list.add(h.product(co, "Wireless Charging Pad",
                "15W fast Qi2 wireless charger compatible with all Qi-enabled devices. Ultra-slim 5mm profile.",
                "TECH-WCP-001", "39.99", "49.99", "Electronics", "ChargeFast",
                "https://placehold.co/800x800/1e272e/ffffff?text=Wireless+Pad",
                180, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1e272e/ffffff?text=Wireless+Pad",
                        "https://placehold.co/800x800/17202a/ffffff?text=Pad+Profile"),
                h.attrs("Output", "15W max (Qi2)",
                        "Thickness", "5mm",
                        "Diameter", "100mm",
                        "Cable", "USB-C, 1.5m"),
                h.options1("Color", "Matte Black", "Arctic White"),
                p -> {
                    h.pv(p, "Matte Black",  "TECH-WCP-BLK", new BigDecimal("39.99"), 90);
                    h.pv(p, "Arctic White", "TECH-WCP-WHT", new BigDecimal("39.99"), 85);
                }));

        list.add(h.product(co, "Noise-Cancelling Earbuds",
                "Active noise cancellation with 8-hour bud + 32-hour case battery. IPX5 sweat resistance.",
                "TECH-NCE-001", "89.99", "109.99", "Electronics", "SoundCore",
                "https://placehold.co/800x800/1a1a2e/ffffff?text=NC+Earbuds",
                130, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a1a2e/ffffff?text=NC+Earbuds",
                        "https://placehold.co/800x800/16213e/ffffff?text=Earbuds+Case"),
                h.attrs("Driver", "11mm dynamic",
                        "Battery", "8h buds + 32h case",
                        "ANC", "Hybrid feedforward + feedback",
                        "Water Rating", "IPX5"),
                h.options1("Color", "Jet Black", "Pearl White", "Navy Blue"),
                p -> {
                    h.pv(p, "Jet Black",   "TECH-NCE-BLK", new BigDecimal("89.99"), 45);
                    h.pv(p, "Pearl White", "TECH-NCE-WHT", new BigDecimal("89.99"), 50);
                    h.pv(p, "Navy Blue",   "TECH-NCE-NVY", new BigDecimal("89.99"), 35);
                }));

        list.add(h.productSingle(co, "Smart Home Hub",
                "Central control for 10,000+ compatible devices. Supports Matter, Zigbee, Z-Wave, Wi-Fi, and Bluetooth.",
                "TECH-SHH-001", "149.99", "179.99", "Electronics", "HomeIQ",
                "https://placehold.co/800x800/2c3e50/ffffff?text=Smart+Hub",
                60, 6, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2c3e50/ffffff?text=Smart+Hub",
                        "https://placehold.co/800x800/273746/ffffff?text=Hub+Ports"),
                h.attrs("Protocols", "Matter, Zigbee 3.0, Z-Wave Plus, Wi-Fi 6, BT 5.2",
                        "Compatible Devices", "10,000+",
                        "Ethernet", "Gigabit",
                        "Local Processing", "Yes, no cloud required")));

        list.add(h.product(co, "LED Gaming Mouse",
                "16,000 DPI optical sensor with 7 programmable buttons and 16.8M color RGB lighting. 1.2m braided cable.",
                "TECH-LGM-001", "59.99", "79.99", "Electronics", "PixelForce",
                "https://placehold.co/800x800/1c2833/ffffff?text=Gaming+Mouse",
                140, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1c2833/ffffff?text=Gaming+Mouse",
                        "https://placehold.co/800x800/17202a/ffffff?text=Mouse+Side"),
                h.attrs("Sensor", "Optical 16,000 DPI",
                        "Buttons", "7 programmable",
                        "Cable", "1.2m braided USB-A",
                        "Weight", "95g"),
                h.options1("Color", "Matte Black", "White"),
                p -> {
                    h.pv(p, "Matte Black", "TECH-LGM-BLK", new BigDecimal("59.99"), 70);
                    h.pv(p, "White",       "TECH-LGM-WHT", new BigDecimal("59.99"), 65);
                }));

        list.add(h.product(co, "Laptop Stand Adjustable",
                "Aluminum ergonomic stand with 6 height levels. Compatible with 10\"–17\" laptops. Foldable for travel.",
                "TECH-LSA-001", "44.99", "59.99", "Electronics", "ErgoPro",
                "https://placehold.co/800x800/707b7c/ffffff?text=Laptop+Stand",
                160, 15, false, false, false, null, null,
                h.images("https://placehold.co/800x800/707b7c/ffffff?text=Laptop+Stand",
                        "https://placehold.co/800x800/616a6b/ffffff?text=Stand+Folded"),
                h.attrs("Material", "Aircraft-grade aluminum",
                        "Height Range", "6 adjustable levels (15–25cm)",
                        "Compatibility", "10\"–17\" laptops",
                        "Weight Capacity", "10kg"),
                h.options1("Color", "Space Gray", "Silver"),
                p -> {
                    h.pv(p, "Space Gray", "TECH-LSA-GRY", new BigDecimal("44.99"), 80);
                    h.pv(p, "Silver",     "TECH-LSA-SLV", new BigDecimal("44.99"), 75);
                }));

        list.add(h.product(co, "Portable SSD 1TB",
                "1TB NVMe portable SSD with USB 3.2 Gen 2 delivering 1050MB/s read. IP55 dust and water resistant.",
                "TECH-SSD-001", "109.99", "139.99", "Electronics", "FlashVault",
                "https://placehold.co/800x800/1a252f/ffffff?text=Portable+SSD",
                100, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a252f/ffffff?text=Portable+SSD",
                        "https://placehold.co/800x800/17202a/ffffff?text=SSD+Side"),
                h.attrs("Capacity", "1TB",
                        "Interface", "USB 3.2 Gen 2 (10Gbps)",
                        "Read Speed", "1,050 MB/s",
                        "Protection", "IP55 dust & water resistant"),
                h.options1("Color", "Slate Gray", "Midnight Black"),
                p -> {
                    h.pv(p, "Slate Gray",     "TECH-SSD-GRY", new BigDecimal("109.99"), 48);
                    h.pv(p, "Midnight Black", "TECH-SSD-BLK", new BigDecimal("109.99"), 45);
                }));

        list.add(h.productSingle(co, "USB Microphone Condenser",
                "Cardioid condenser mic with 24-bit/192kHz recording, zero-latency monitoring, and plug-and-play USB.",
                "TECH-MIC-001", "79.99", "99.99", "Electronics", "ClearCast",
                "https://placehold.co/800x800/1b2631/ffffff?text=USB+Mic",
                85, 8, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1b2631/ffffff?text=USB+Mic",
                        "https://placehold.co/800x800/17202a/ffffff?text=Mic+Stand"),
                h.attrs("Pattern", "Cardioid",
                        "Sample Rate", "24-bit / 192kHz",
                        "Connection", "USB-C (cable included)",
                        "Frequency Response", "20Hz–20kHz")));

        list.add(h.product(co, "Smart LED Strip Lights",
                "16.4ft RGBIC addressable LEDs with scene modes, music sync, and app/voice control. Works with Alexa & Google.",
                "TECH-LED-001", "34.99", "44.99", "Electronics", "GlowTech",
                "https://placehold.co/800x800/1a5276/ffffff?text=LED+Strip",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a5276/ffffff?text=LED+Strip",
                        "https://placehold.co/800x800/154360/ffffff?text=Strip+Installed"),
                h.attrs("LED Type", "RGBIC addressable",
                        "Control", "App, voice (Alexa/Google)",
                        "Power", "24V DC adapter included",
                        "Compatibility", "Works with Alexa & Google Home"),
                h.options1("Length", "5m (16.4ft)", "10m (32.8ft)", "16.4ft Starter Kit"),
                p -> {
                    h.pv(p, "5m (16.4ft)",        "TECH-LED-5M",  new BigDecimal("34.99"), 80);
                    h.pv(p, "10m (32.8ft)",        "TECH-LED-10M", new BigDecimal("54.99"), 60);
                    h.pv(p, "16.4ft Starter Kit",  "TECH-LED-KIT", new BigDecimal("44.99"), 55);
                }));

        list.add(h.product(co, "Wireless Keyboard + Mouse Set",
                "Slim 2.4GHz wireless keyboard and mouse combo with 12-month battery life. Quiet scissor-switch keys.",
                "TECH-KMS-001", "69.99", "89.99", "Electronics", "KeyForge",
                "https://placehold.co/800x800/212f3c/ffffff?text=KB+Mouse+Set",
                120, 12, false, false, false, null, null,
                h.images("https://placehold.co/800x800/212f3c/ffffff?text=KB+Mouse+Set",
                        "https://placehold.co/800x800/1c2833/ffffff?text=KB+MS+Close"),
                h.attrs("Connectivity", "2.4GHz USB nano receiver",
                        "Battery Life", "12 months keyboard, 18 months mouse",
                        "Keys", "Quiet scissor-switch",
                        "Mouse DPI", "1000/1600/2400 switchable"),
                h.options1("Color", "Graphite", "White"),
                p -> {
                    h.pv(p, "Graphite", "TECH-KMS-GRP", new BigDecimal("69.99"), 60);
                    h.pv(p, "White",    "TECH-KMS-WHT", new BigDecimal("69.99"), 55);
                }));

        list.add(h.productSingle(co, "Monitor Arm Dual",
                "Fully articulating dual-monitor arm supporting 2× 17\"–32\" displays up to 9kg each. VESA 75×75 & 100×100.",
                "TECH-MAD-001", "89.99", "119.99", "Electronics", "ErgoPro",
                "https://placehold.co/800x800/2c3e50/ffffff?text=Monitor+Arm",
                55, 5, false, false, false, null, null,
                h.images("https://placehold.co/800x800/2c3e50/ffffff?text=Monitor+Arm",
                        "https://placehold.co/800x800/273746/ffffff?text=Arm+Extended"),
                h.attrs("Monitor Size", "17\"–32\"",
                        "Weight Capacity", "9kg per arm",
                        "VESA", "75×75 and 100×100mm",
                        "Adjustment", "Tilt ±45°, Swivel 360°, Rotate 90°")));

        list.add(h.product(co, "Cable Management Kit",
                "25-piece cable organizer kit including cable clips, sleeves, velcro ties, and cable boxes.",
                "TECH-CMK-001", "19.99", "24.99", "Electronics", "NexPort",
                "https://placehold.co/800x800/707b7c/ffffff?text=Cable+Kit",
                250, 25, false, false, false, null, null,
                h.images("https://placehold.co/800x800/707b7c/ffffff?text=Cable+Kit",
                        "https://placehold.co/800x800/616a6b/ffffff?text=Kit+Contents"),
                h.attrs("Pieces", "25 pcs: clips, sleeves, velcro, boxes",
                        "Material", "ABS plastic + nylon",
                        "Cable Box Size", "26 × 13 × 7 cm",
                        "Color Options", "Black or White"),
                h.options1("Color", "Black", "White"),
                p -> {
                    h.pv(p, "Black", "TECH-CMK-BLK", new BigDecimal("19.99"), 125);
                    h.pv(p, "White", "TECH-CMK-WHT", new BigDecimal("19.99"), 120);
                }));

        list.add(h.productSingle(co, "Smart Plug 4-Pack",
                "Wi-Fi smart plugs with energy monitoring, schedules, and voice control. No hub required.",
                "TECH-SPL-001", "29.99", "39.99", "Electronics", "HomeIQ",
                "https://placehold.co/800x800/1e272e/ffffff?text=Smart+Plug",
                200, 20, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1e272e/ffffff?text=Smart+Plug",
                        "https://placehold.co/800x800/17202a/ffffff?text=Plug+App"),
                h.attrs("Pack Size", "4 plugs",
                        "Max Load", "15A / 1800W",
                        "Standards", "Wi-Fi 2.4GHz, Works with Alexa/Google",
                        "Features", "Energy monitoring, schedules, timers")));

        list.add(h.product(co, "Mesh WiFi Router System",
                "Tri-band mesh system with Wi-Fi 6E support. Blanket 5,000–9,000 sq ft with seamless roaming.",
                "TECH-WFI-001", "199.99", "249.99", "Electronics", "NetBlast",
                "https://placehold.co/800x800/2e4057/ffffff?text=Mesh+WiFi",
                65, 6, true, false, false, null, null,
                h.images("https://placehold.co/800x800/2e4057/ffffff?text=Mesh+WiFi",
                        "https://placehold.co/800x800/273746/ffffff?text=WiFi+Coverage",
                        "https://placehold.co/800x800/212f3c/ffffff?text=WiFi+App"),
                h.attrs("Standard", "Wi-Fi 6E (802.11ax)",
                        "Bands", "Tri-band (2.4 + 5 + 6GHz)",
                        "Max Speed", "7.8 Gbps aggregate",
                        "Ports", "2.5GbE WAN + 2× 1GbE LAN per node"),
                h.options1("Size", "2-Node Kit (up to 5,000 sq ft)", "3-Node Kit (up to 9,000 sq ft)"),
                p -> {
                    h.pv(p, "2-Node Kit (up to 5,000 sq ft)", "TECH-WFI-2N", new BigDecimal("199.99"), 35);
                    h.pv(p, "3-Node Kit (up to 9,000 sq ft)", "TECH-WFI-3N", new BigDecimal("279.99"), 28);
                }));

        list.add(h.product(co, "Digital Photo Frame 10\"",
                "10.1\" IPS touchscreen frame with 32GB storage, Wi-Fi photo sharing, and motion-activated display.",
                "TECH-DPF-001", "79.99", "99.99", "Electronics", "MemoryBox",
                "https://placehold.co/800x800/1a1a2e/ffffff?text=Photo+Frame",
                90, 10, false, false, false, null, null,
                h.images("https://placehold.co/800x800/1a1a2e/ffffff?text=Photo+Frame",
                        "https://placehold.co/800x800/16213e/ffffff?text=Frame+Side"),
                h.attrs("Display", "10.1\" IPS 1280×800",
                        "Storage", "32GB internal",
                        "Connectivity", "Wi-Fi 2.4/5GHz",
                        "Features", "Motion sensor, auto-brightness, touchscreen"),
                h.options1("Color", "Walnut Wood", "Matte Black"),
                p -> {
                    h.pv(p, "Walnut Wood", "TECH-DPF-WAL", new BigDecimal("79.99"), 45);
                    h.pv(p, "Matte Black", "TECH-DPF-BLK", new BigDecimal("79.99"), 40);
                }));

        return list;
    }
}
