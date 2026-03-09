package backend.http;

public record ClientInfo(
        String ip,
        DeviceType deviceType,
        String browser,
        String os,
        String userAgent
) {}
