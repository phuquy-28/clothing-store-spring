package com.example.clothingstore.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.annotation.ApiMessage;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.CashBackUpdateDTO;
import com.example.clothingstore.dto.request.ReturnRequestProcessDTO;
import com.example.clothingstore.dto.request.ReturnRequestReqDTO;
import com.example.clothingstore.dto.request.UploadImageReqDTO;
import com.example.clothingstore.dto.response.ResultPaginationDTO;
import com.example.clothingstore.dto.response.ReturnRequestResDTO;
import com.example.clothingstore.dto.response.UploadImageResDTO;
import com.example.clothingstore.entity.ReturnRequest;
import com.example.clothingstore.service.CloudStorageService;
import com.example.clothingstore.service.ReturnRequestService;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class ReturnRequestController {

  private final Logger log = LoggerFactory.getLogger(ReturnRequestController.class);

  private final ReturnRequestService returnRequestService;

  private final CloudStorageService cloudStorageService;

  @PostMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.RETURN_REQUESTS_USER)
  @ApiMessage("Return request created successfully")
  public ResponseEntity<ReturnRequestResDTO> createReturnRequest(
      @RequestBody @Valid ReturnRequestReqDTO returnRequestReqDTO) {
    log.debug("REST request to create return request: {}", returnRequestReqDTO);
    ReturnRequestResDTO result = returnRequestService.createReturnRequest(returnRequestReqDTO);
    return ResponseEntity.ok(result);
  }

  @GetMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.ID)
  public ResponseEntity<ReturnRequestResDTO> getReturnRequestById(@PathVariable Long id) {
    log.debug("REST request to get return request by id: {}", id);
    ReturnRequestResDTO result = returnRequestService.getReturnRequestById(id);
    return ResponseEntity.ok(result);
  }

  @GetMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.ORDERS + UrlConfig.ID)
  public ResponseEntity<ReturnRequestResDTO> getReturnRequestByOrderId(@PathVariable Long id) {
    log.debug("REST request to get return request by order id: {}", id);
    ReturnRequestResDTO result = returnRequestService.getReturnRequestByOrderId(id);
    return ResponseEntity.ok(result);
  }

  @DeleteMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.ID)
  @ApiMessage("Return request deleted successfully")
  public ResponseEntity<Void> deleteReturnRequest(@PathVariable Long id) {
    log.debug("REST request to delete return request by id: {}", id);
    returnRequestService.deleteReturnRequest(id);
    return ResponseEntity.ok().build();
  }

  @PostMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.UPLOAD_IMAGES)
  public ResponseEntity<UploadImageResDTO> uploadReturnImage(
      @RequestBody UploadImageReqDTO uploadImageReqDTO) {
    log.debug("REST request to upload return request image: {}", uploadImageReqDTO);
    UploadImageResDTO result =
        cloudStorageService.createSignedUrlWithDirectory(uploadImageReqDTO, "return-images");
    return ResponseEntity.ok(result);
  }

  @GetMapping(UrlConfig.RETURN_REQUESTS)
  public ResponseEntity<ResultPaginationDTO> getAllReturnRequests(
      @Filter Specification<ReturnRequest> spec, Pageable pageable) {
    log.debug("REST request to get all return requests");
    ResultPaginationDTO result = returnRequestService.getAllReturnRequests(spec, pageable);
    return ResponseEntity.ok(result);
  }

  @PutMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.PROCESS)
  @ApiMessage("Return request processed successfully")
  public ResponseEntity<ReturnRequestResDTO> processReturnRequest(
      @RequestBody @Valid ReturnRequestProcessDTO returnRequestProcessDTO) {
    log.debug("REST request to process return request: {}", returnRequestProcessDTO);
    ReturnRequestResDTO result = returnRequestService.processReturnRequest(returnRequestProcessDTO);
    return ResponseEntity.ok(result);
  }

  @PutMapping(UrlConfig.RETURN_REQUESTS + UrlConfig.CASHBACK)
  @ApiMessage("Cashback status updated successfully")
  public ResponseEntity<ReturnRequestResDTO> updateCashBackStatus(
      @RequestBody @Valid CashBackUpdateDTO cashBackUpdateDTO) {
    log.debug("REST request to update cashback status: {}", cashBackUpdateDTO);
    ReturnRequestResDTO result = returnRequestService.updateCashBackStatus(cashBackUpdateDTO);
    return ResponseEntity.ok(result);
  }

}
