package com.example.backend.service.impl;

import com.example.backend.dto.request.AdditionalServiceRequest;
import com.example.backend.dto.response.AdditionalServiceResponse;
import com.example.backend.dto.response.PagedResponse;
import com.example.backend.entity.AdditionalService;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.AdditionalServiceRepository;
import com.example.backend.service.AdditionalServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdditionalServiceServiceImpl implements AdditionalServiceService {

    private final AdditionalServiceRepository repo;

    /* ─── Public ─── */

    @Override
    @Transactional(readOnly = true)
    public List<AdditionalServiceResponse> getActiveServices() {
        return repo.findByStatusOrderByNameAsc("ACTIVE")
                   .stream()
                   .map(AdditionalServiceResponse::from)
                   .toList();
    }

    /* ─── Admin ─── */

    @Override
    @Transactional(readOnly = true)
    public List<AdditionalServiceResponse> getAllServices() {
        return repo.findAllByOrderByCreatedAtDesc()
                   .stream()
                   .map(AdditionalServiceResponse::from)
                   .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AdditionalServiceResponse> searchServices(
            String status, String category, String keyword, int page, int size) {

        String st  = (status   != null && !status.isBlank())   ? status   : null;
        String cat = (category != null && !category.isBlank()) ? category : null;
        String kw  = (keyword  != null && !keyword.isBlank())  ? keyword  : null;

        Page<AdditionalService> p = repo.search(st, cat, kw, PageRequest.of(page, size));

        return PagedResponse.<AdditionalServiceResponse>builder()
                .content(p.getContent().stream().map(AdditionalServiceResponse::from).toList())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdditionalServiceResponse getById(Long id) {
        return AdditionalServiceResponse.from(findOrThrow(id));
    }

    @Override
    @Transactional
    public AdditionalServiceResponse create(AdditionalServiceRequest req) {
        AdditionalService entity = AdditionalService.builder()
                .name(req.getName())
                .category(req.getCategory())
                .price(req.getPrice())
                .unit(req.getUnit())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .status(req.getStatus() != null ? req.getStatus() : "ACTIVE")
                .build();
        return AdditionalServiceResponse.from(repo.save(entity));
    }

    @Override
    @Transactional
    public AdditionalServiceResponse update(Long id, AdditionalServiceRequest req) {
        AdditionalService entity = findOrThrow(id);
        entity.setName(req.getName());
        entity.setCategory(req.getCategory());
        entity.setPrice(req.getPrice());
        entity.setUnit(req.getUnit());
        entity.setDescription(req.getDescription());
        entity.setImageUrl(req.getImageUrl());
        if (req.getStatus() != null) entity.setStatus(req.getStatus());
        return AdditionalServiceResponse.from(repo.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy dịch vụ #" + id);
        }
        repo.deleteById(id);
    }

    /* ─── Helper ─── */
    private AdditionalService findOrThrow(Long id) {
        return repo.findById(id)
                   .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dịch vụ #" + id));
    }
}
