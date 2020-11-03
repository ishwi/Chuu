package ish.model;


import dao.ChuuService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;

import javax.inject.Singleton;

@Factory
public class Chuu {

    @Bean
    @Singleton
    public ChuuService getService() {
        return new ChuuService();
    }
}
