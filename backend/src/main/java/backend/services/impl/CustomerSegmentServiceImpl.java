package backend.services.impl;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import backend.dtos.requests.segment.CreateCustomerSegmentRequest;
import backend.dtos.requests.segment.UpdateCustomerSegmentRequest;
import backend.dtos.responses.general.PagedResponse;
import backend.dtos.responses.segment.CustomerSegmentResponse;
import backend.exceptions.http.ConflictException;
import backend.exceptions.http.ResourceNotFoundException;
import backend.models.core.CustomerSegment;
import backend.models.core.User;
import backend.repositories.CustomerSegmentRepository;
import backend.repositories.UserRepository;
import backend.services.intf.CustomerSegmentService;

@Service
public class CustomerSegmentServiceImpl implements CustomerSegmentService {

    private final CustomerSegmentRepository segmentRepository;
    private final UserRepository userRepository;

    public CustomerSegmentServiceImpl(
            CustomerSegmentRepository segmentRepository,
            UserRepository userRepository) {
        this.segmentRepository = segmentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public PagedResponse<CustomerSegmentResponse> listSegments(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        return new PagedResponse<>(segmentRepository.findAll(pageable).map(this::toResponse));
    }

    @Override
    public CustomerSegmentResponse getSegment(long segmentId) {
        return toResponse(segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer segment not found with id: " + segmentId)));
    }

    @Override
    @Transactional
    public CustomerSegmentResponse createSegment(CreateCustomerSegmentRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (segmentRepository.existsByCodeIgnoreCase(code)) {
            throw new ConflictException("A customer segment with code '" + code + "' already exists");
        }

        CustomerSegment segment = new CustomerSegment();
        segment.setCode(code);
        segment.setName(request.getName());
        segment.setDescription(request.getDescription());

        return toResponse(segmentRepository.save(segment));
    }

    @Override
    @Transactional
    public CustomerSegmentResponse updateSegment(long segmentId, UpdateCustomerSegmentRequest request) {
        CustomerSegment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer segment not found with id: " + segmentId));

        if (request.getName() != null) segment.setName(request.getName());
        if (request.getDescription() != null) segment.setDescription(request.getDescription());

        return toResponse(segmentRepository.save(segment));
    }

    @Override
    @Transactional
    public void deleteSegment(long segmentId) {
        CustomerSegment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer segment not found with id: " + segmentId));
        segmentRepository.delete(segment);
    }

    @Override
    @Transactional
    public void assignSegmentToUser(long userId, long segmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        CustomerSegment segment = segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer segment not found with id: " + segmentId));
        user.getSegments().add(segment);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeSegmentFromUser(long userId, long segmentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.getSegments().removeIf(s -> s.getId() != null && s.getId() == segmentId);
        userRepository.save(user);
    }

    private CustomerSegmentResponse toResponse(CustomerSegment s) {
        return new CustomerSegmentResponse(
                s.getId(), s.getCode(), s.getName(), s.getDescription(),
                s.getCreatedAt(), s.getUpdatedAt());
    }
}
