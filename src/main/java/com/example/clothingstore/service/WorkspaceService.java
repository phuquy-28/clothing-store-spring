package com.example.clothingstore.service;

import com.example.clothingstore.dto.request.LoginReqDTO;
import com.example.clothingstore.dto.response.LoginResDTO;

public interface WorkspaceService {
  
  LoginResDTO login(LoginReqDTO loginReqDTO);
}
