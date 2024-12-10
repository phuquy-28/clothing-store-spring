package com.example.clothingstore.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.clothingstore.constant.UrlConfig;
import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.DashboardResDTO;
import com.example.clothingstore.dto.response.LoginResDTO;
import com.example.clothingstore.service.WorkspaceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${api.version}")
@RequiredArgsConstructor
public class WorkspaceController {

  private final WorkspaceService workspaceService;

  @PostMapping(UrlConfig.WORKSPACE + UrlConfig.LOGIN)
  public ResponseEntity<LoginResDTO> login(@RequestBody @Valid LoginReqDTO loginReqDTO) {
    return ResponseEntity.ok(workspaceService.login(loginReqDTO));
  }

  @GetMapping(UrlConfig.WORKSPACE + UrlConfig.DASHBOARD)
  public ResponseEntity<DashboardResDTO> getDashboard() {
    return ResponseEntity.ok(workspaceService.getDashboard());
  }
}
