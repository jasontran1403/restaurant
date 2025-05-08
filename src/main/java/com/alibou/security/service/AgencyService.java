package com.alibou.security.service;

import com.alibou.security.dto.SignupRequest;

public interface AgencyService {
    boolean isAuthenticated(String username, String passsword);
    boolean createUser(SignupRequest request);
}
