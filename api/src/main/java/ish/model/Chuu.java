package ish.model;


import dao.ChuuDatasource;
import dao.ChuuService;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

@Factory
public class Chuu {

    @Bean
    @Singleton
    public ChuuService getService() {
        return new ChuuService(new ChuuDatasource());
    }
}
