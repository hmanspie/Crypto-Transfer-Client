package com.rebalcomb.service;

import com.rebalcomb.model.entity.Log;
import com.rebalcomb.model.entity.enums.TypeLog;
import com.rebalcomb.repositories.LogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LogService {

    public final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public Log save(Log log){
        return logRepository.save(log);
    }

    public void create(TypeLog typeLog, String logInfo){
        Log log = new Log();
        log.setType(typeLog);
        log.setDate_time(Timestamp.valueOf(LocalDateTime.now()));
        log.setInfo(logInfo);
        logRepository.save(log);
    }

    public Optional<Log> findById(Long id) {
        return logRepository.findById(id);
    }

    public List<Log> findAll(){
        return logRepository.findAll();
    }

    public void deleteById(Long id) {
        logRepository.deleteById(id);
    }

}
