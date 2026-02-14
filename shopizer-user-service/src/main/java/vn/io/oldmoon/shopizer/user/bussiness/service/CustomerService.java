package vn.io.oldmoon.shopizer.user.bussiness.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.io.oldmoon.shopizer.user.infra.model.CustomerProfile;
import vn.io.oldmoon.shopizer.user.infra.repository.CustomerProfileRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CustomerService {
    private final CustomerProfileRepository profileRepo;

    public CustomerProfile createProfile(CustomerProfile profile) {
        CustomerProfile createdProfile = profileRepo.save(profile);
        log.info("Profile Created: id={}, userId={}",
                createdProfile.getId(),
                createdProfile.getUserId()
        );
        return createdProfile;
    }
}
