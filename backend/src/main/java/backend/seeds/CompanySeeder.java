package backend.seeds;

import backend.models.core.Company;
import backend.models.core.InventoryLocation;
import backend.models.core.User;
import backend.models.enums.CompanyStatus;
import backend.repositories.CompanyRepository;
import backend.repositories.InventoryLocationRepository;
import backend.seeds.UserSeeder.SeededUsers;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class CompanySeeder {

    private final CompanyRepository companyRepository;
    private final InventoryLocationRepository inventoryLocationRepository;

    public record SeededCompanies(Company tech, Company style, Company wellness) {}

    public SeededCompanies seed(SeededUsers users) {
        Company tech = company(users.techMerchant(), "TechGadgets Co.", "Electronics & Technology",
                "500 Market St", "San Francisco", "US", "94105",
                "+14155550100", "contact@techgadgets.dev",
                "https://techgadgets.dev", "Premium consumer electronics and smart home devices.", 2018, 45);

        Company style = company(users.styleMerchant(), "StyleHub", "Fashion & Apparel",
                "350 Fifth Ave", "New York", "US", "10118",
                "+12125550200", "hello@stylehub.dev",
                "https://stylehub.dev", "Curated fashion-forward apparel for modern wardrobes.", 2020, 22);

        Company wellness = company(users.wellnessMerchant(), "WellnessWorld", "Health & Wellness",
                "220 Congress Ave", "Austin", "US", "78701",
                "+15125550300", "info@wellnessworld.dev",
                "https://wellnessworld.dev", "Science-backed health, beauty, and wellness products.", 2019, 30);

        invLoc(tech,     "Main Warehouse",       "TECH-MAIN",    "500 Market St",    "San Francisco", "US");
        invLoc(tech,     "East Coast Hub",        "TECH-EAST",    "101 Commerce Dr",  "Newark",        "US");
        invLoc(style,    "Downtown Fulfillment",  "STYLE-NYC",    "350 Fifth Ave",    "New York",      "US");
        invLoc(style,    "West Coast Center",     "STYLE-LA",     "800 Alameda St",   "Los Angeles",   "US");
        invLoc(wellness, "Central Distribution",  "WELL-CENTRAL", "220 Congress Ave", "Austin",        "US");
        invLoc(wellness, "South Hub",             "WELL-SOUTH",   "400 Commerce St",  "Houston",       "US");

        return new SeededCompanies(tech, style, wellness);
    }

    private Company company(User owner, String name, String industry, String address,
                             String city, String country, String postal, String phone,
                             String email, String website, String description,
                             int foundedYear, int employeeCount) {
        return companyRepository.findByNameAndOwnerId(name, owner.getId()).orElseGet(() -> {
            Company c = new Company();
            c.setOwner(owner);
            c.setName(name);
            c.setIndustry(industry);
            c.setAddress(address);
            c.setCity(city);
            c.setCountry(country);
            c.setPostalCode(postal);
            c.setPhoneNumber(phone);
            c.setEmail(email);
            c.setWebsite(website);
            c.setDescription(description);
            c.setFoundedYear(foundedYear);
            c.setEmployeeCount(employeeCount);
            c.setStatus(CompanyStatus.ACTIVE);
            return companyRepository.save(c);
        });
    }

    private void invLoc(Company co, String name, String code, String address, String city, String country) {
        if (inventoryLocationRepository.existsByCodeAndCompanyId(code, co.getId())) return;
        InventoryLocation loc = new InventoryLocation();
        loc.setCompany(co);
        loc.setName(name);
        loc.setCode(code);
        loc.setAddress(address);
        loc.setCity(city);
        loc.setCountry(country);
        loc.setActive(true);
        inventoryLocationRepository.save(loc);
    }
}
