package com.atquil.springbootfaulttolerance.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

/**
 * @author atquil
 */

@Service
//@EnableRetry
//@Slf4j
public class RecoverService {

//    @Retryable(
//            retryFor = ArithmeticException.class
//    )
//    public String simpleRecover(String name, int number){
//        log.info("-------Name:{}, number:{} ---------", name, number);
//        throw new ArithmeticException("Arithmetic Exception from Simple Recover");
//    }
//
//    // Type 1 : All exception Handler
//    @Recover
//    public String recover(ArithmeticException ex){
//        return "Handle all ArithmeticException"+ex.getMessage();
//    }

}
