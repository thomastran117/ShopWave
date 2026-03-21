package backend.dtos.responses.upload;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignUploadResponse {
    /** Pre-signed S3 PUT URL. Use this to upload the file directly from the client. */
    private String uploadUrl;
    /** Permanent public URL of the file once uploaded. Store this value in the database. */
    private String fileUrl;
    /** S3 object key — for reference only. */
    private String key;
    /** Seconds until the uploadUrl expires. */
    private int expiresIn;
}
