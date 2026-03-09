package backend.http;

public record ClientInfo(
        String ip,
        DeviceType deviceType,
        String browser,
        String os,
        String userAgent
) {

    public static final String UNKNOWN_VALUE = "Unknown";

    public static final ClientInfo UNKNOWN = new ClientInfo(
            "0.0.0.0", DeviceType.UNKNOWN, UNKNOWN_VALUE, UNKNOWN_VALUE, ""
    );
}
