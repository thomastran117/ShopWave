package backend.services.intf.customers;

import backend.dtos.requests.address.CreateCustomerAddressRequest;
import backend.dtos.requests.address.UpdateCustomerAddressRequest;
import backend.dtos.responses.address.CustomerAddressResponse;

import java.util.List;

public interface CustomerAddressService {

    List<CustomerAddressResponse> listAddresses(long userId);

    CustomerAddressResponse getAddress(long userId, long addressId);

    CustomerAddressResponse createAddress(long userId, CreateCustomerAddressRequest request);

    CustomerAddressResponse updateAddress(long userId, long addressId, UpdateCustomerAddressRequest request);

    void deleteAddress(long userId, long addressId);

    CustomerAddressResponse setDefault(long userId, long addressId);
}
