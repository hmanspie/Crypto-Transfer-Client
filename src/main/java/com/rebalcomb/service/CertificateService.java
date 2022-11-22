package com.rebalcomb.service;

import com.rebalcomb.model.entity.Certificate;
import com.rebalcomb.repositories.CerificateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CertificateService {

    private final CerificateRepository cerificateRepository;

    @Autowired
    public CertificateService(CerificateRepository cerificateRepository) {
        this.cerificateRepository = cerificateRepository;
    }

    public Certificate save(Certificate certificate){
        return cerificateRepository.save(certificate);
    }
}
