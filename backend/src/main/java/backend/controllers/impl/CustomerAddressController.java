package backend.controllers.impl;

import backend.annotations.requireAuth.RequireAuth;
import backend.dtos.requests.address.CreateCustomerAddressRequest;
import backend.dtos.requests.address.UpdateCustomerAddressRequest;
import backend.dtos.responses.address.CustomerAddressResponse;
import backend.exceptions.http.AppHttpException;
import backend.exceptions.http.InternalServerErrorException;
import backend.services.intf.CustomerAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addresses")
@RequireAuth
public class CustomerAddressController {

    private final CustomerAddressService addressService;

    public CustomerAddressController(CustomerAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("")
    public ResponseEntity<List<CustomerAddressResponse>> listAddresses() {
        try {
            return ResponseEntity.ok(addressService.listAddresses(resolveUserId()));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerAddressResponse> getAddress(@PathVariable long id) {
        try {
            return ResponseEntity.ok(addressService.getAddress(resolveUserId(), id));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PostMapping("")
    public ResponseEntity<CustomerAddressResponse> createAddress(
            @Valid @RequestBody CreateCustomerAddressRequest request) {
        try {
            CustomerAddressResponse response = addressService.createAddress(resolveUserId(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerAddressResponse> updateAddress(
            @PathVariable long id,
            @Valid @RequestBody UpdateCustomerAddressRequest request) {
        try {
            return ResponseEntity.ok(addressService.updateAddress(resolveUserId(), id, request));
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable long id) {
        try {
            addressService.deleteAddress(resolveUserId(), id);
            return ResponseEntity.noContent().build();
        } catch (AppHttpException e) {
            throw e;
        } catch (Exception e) {
            throw new InternalServerErrorException();
        }
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<CustomerAddressResponse> setDefault(@PathVariable long id) {
        try {
            return ResponseEntity.ok(addressService.setDefault(resolveUserId(), id));
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
