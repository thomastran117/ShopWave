package backend.controllers.impl;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.credit.IssueCreditRequest;
import backend.dtos.responses.credit.CreditBalanceResponse;
import backend.dtos.responses.credit.CreditEntryResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.CustomerCreditService;

@RestController
public class CustomerCreditController {

    private final CustomerCreditService creditService;

    public CustomerCreditController(CustomerCreditService creditService) {
        this.creditService = creditService;
    }

    @GetMapping("/me/credits")
    @RequireAuth
    public ResponseEntity<CreditBalanceResponse> getMyCredits() {
        try {
            return ResponseEntity.ok(creditService.getBalance(resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/support/customers/{userId}/credits")
    @RequireAuth
    public ResponseEntity<CreditBalanceResponse> getCustomerCredits(@PathVariable long userId) {
        try {
            return ResponseEntity.ok(creditService.getBalance(userId));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/support/customers/{userId}/credits")
    @RequireAuth
    public ResponseEntity<CreditEntryResponse> issueCredit(@PathVariable long userId,
                                                            @Valid @RequestBody IssueCreditRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(creditService.issueCredit(userId, request, resolveUserId(), null, null));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("/support/credits/{entryId}/reverse")
    @RequireAuth
    public ResponseEntity<CreditEntryResponse> reverseCredit(@PathVariable long entryId) {
        try {
            return ResponseEntity.ok(creditService.reverseCredit(entryId, resolveUserId()));
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
