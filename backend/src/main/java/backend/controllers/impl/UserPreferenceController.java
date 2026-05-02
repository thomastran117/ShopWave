package backend.controllers.impl;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.preference.SetTrackingOptOutRequest;
import backend.dtos.responses.preference.UserPreferenceResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.UserPreferenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/preferences")
@RequireAuth
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping
    public ResponseEntity<UserPreferenceResponse> getPreferences() {
        try {
            long userId = resolveUserId();
            boolean optedOut = userPreferenceService.isTrackingOptedOut(userId);
            return ResponseEntity.ok(new UserPreferenceResponse(optedOut));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PutMapping("/tracking")
    public ResponseEntity<Void> setTracking(@Valid @RequestBody SetTrackingOptOutRequest request) {
        try {
            userPreferenceService.setTrackingOptOut(resolveUserId(), request.getOptOut());
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    private long resolveUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((Number) auth.getPrincipal()).longValue();
    }
}
