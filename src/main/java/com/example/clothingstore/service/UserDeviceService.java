package com.example.clothingstore.service;

public interface UserDeviceService {

  void saveFCMToken(String deviceToken, String email);

  void deleteFCMToken(String deviceToken, String email);

}
