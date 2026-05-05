package backend.models.enums;

public enum UploadFolder {
    COMPANY_LOGO("company-logos"),
    PRODUCT_THUMBNAIL("product-thumbnails"),
    PRODUCT_IMAGE("product-images"),
    RETURN_EVIDENCE("return-evidence");

    private final String path;

    UploadFolder(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
