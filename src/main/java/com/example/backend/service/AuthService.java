package com.example.backend.service;

import com.example.backend.dto.request.RegisterRequest;
import com.example.backend.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
}
