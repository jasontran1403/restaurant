package com.alibou.security.service.serviceimpl;

import com.alibou.security.repository.AgencyRepository;
import com.alibou.security.service.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgencyServiceImpl implements AgencyService {
    private final AgencyRepository agencyRepository;


    @Override
    public boolean isAuthenticated(String username, String passsword) {
        var agencyOptional = agencyRepository.findByUsername(username);
        return agencyOptional.map(agency -> agency.getPassword().equals(passsword)).orElse(false);
    }
}
