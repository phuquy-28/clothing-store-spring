package com.example.clothingstore.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.clothingstore.dto.request.ReturnRequestProcessDTO;
import com.example.clothingstore.dto.request.ReturnRequestReqDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReturnRequestResDTO;
import com.example.clothingstore.entity.ReturnRequest;

public interface ReturnRequestService {

  ReturnRequestResDTO createReturnRequest(ReturnRequestReqDTO returnRequestReqDTO);

  ReturnRequestResDTO processReturnRequest(ReturnRequestProcessDTO returnRequestProcessDTO);

  ReturnRequestResDTO getReturnRequestById(Long id);

  ReturnRequestResDTO getReturnRequestByOrderId(Long id);

  ResultPaginationDTO getAllReturnRequests(Specification<ReturnRequest> spec, Pageable pageable);

  void deleteReturnRequest(Long id);
}
