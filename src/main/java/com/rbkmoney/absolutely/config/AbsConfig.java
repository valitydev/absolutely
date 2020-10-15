package com.rbkmoney.absolutely.config;

import com.rbkmoney.swag.adapter.abs.ApiClient;
import com.rbkmoney.swag.adapter.abs.api.AbsApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AbsConfig {
    @Bean
    public AbsApi absApi(@Value("${abs.url}") String absBasePath){
        return new AbsApi(new ApiClient().setBasePath(absBasePath));
    }
}
