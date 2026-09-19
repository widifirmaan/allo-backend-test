package com.allobank.splitbill.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI dokumentasiSplitBill() {
    return new OpenAPI()
        .info(
            new Info()
                .title("Split Bill API")
                .description("API patungan: buat grup, catat expense, lihat settlement.")
                .version("0.0.1"));
  }
}
