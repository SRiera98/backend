package com.um.cloudfixum.cloudfixum.service;

import com.um.cloudfixum.cloudfixum.common.GenericServiceImpl;
import com.um.cloudfixum.cloudfixum.model.MinorJob;
import com.um.cloudfixum.cloudfixum.model.ProviderUser;
import com.um.cloudfixum.cloudfixum.repository.ProviderUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProviderUserService extends GenericServiceImpl<ProviderUser> {

    private final ProviderUserRepository providerUserRepository;

    public ProviderUserService(ProviderUserRepository providerUserRepository) {
        this.providerUserRepository = providerUserRepository;
    }

    public ResponseEntity<List<MinorJob>> getJobs(Long id) {
        if (!providerUserRepository.findById(id).isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        List<MinorJob> userJobs = providerUserRepository.findById(id).get().getServiceList();

        return new ResponseEntity<>(userJobs, HttpStatus.OK);

    }

    public ResponseEntity<ProviderUser> findUserByDni(String dni){
        if (!providerUserRepository.findByDni(dni).isPresent()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        ProviderUser providerUser = providerUserRepository.findByDni(dni).get();
        return new ResponseEntity<>(providerUser,HttpStatus.OK);
    }


    @Override
    public JpaRepository<ProviderUser, Long> getRepository() {
        return providerUserRepository;
    }
}