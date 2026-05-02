package backend.controllers.impl.returns;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.return_.BuyerInitiateReturnRequest;
import backend.dtos.responses.return_.ReturnResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.returns.ReturnService;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping("/{orderId}/returns")
    @RequireAuth
    public ResponseEntity<ReturnResponse> requestReturn(
            @PathVariable long orderId,
            @RequestBody @Valid BuyerInitiateReturnRequest request) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.status(HttpStatus.CREATED).body(returnService.requestReturn(orderId, userId, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{orderId}/returns")
    @RequireAuth
    public ResponseEntity<List<ReturnResponse>> getReturnsByOrder(@PathVariable long orderId) {
        try {
            long userId = resolveUserId();
            return ResponseEntity.ok(returnService.getReturnsByOrder(orderId, userId));
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
