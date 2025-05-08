package com.alibou.security.service.serviceimpl;

import com.alibou.security.dto.SignupRequest;
import com.alibou.security.entity.Agency;
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

    @Override
    public boolean createUser(SignupRequest request) {
        var agencyUsernameOptional = agencyRepository.findByUsername(request.getUsername());
        var agencyEmailOptional = agencyRepository.findByEmail(request.getEmail());

        if (agencyUsernameOptional.isPresent() || agencyEmailOptional.isPresent()) {
            return false;
        }

        Agency agency = new Agency();
        agency.setUsername(request.getUsername());
        agency.setEmail(request.getEmail());
        agency.setPassword(request.getPassword());
        agency.setPhone(request.getPhone());
        agency.setRole("Customer");
        var result = agencyRepository.save(agency);

        return result.getId() > 0;
    }
}
