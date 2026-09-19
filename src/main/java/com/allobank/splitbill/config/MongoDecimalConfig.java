package com.allobank.splitbill.config;

import java.math.BigDecimal;
import java.util.List;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoDecimalConfig {

  @Bean
  public MongoCustomConversions konversiDesimalMongo() {
    return new MongoCustomConversions(
        List.of(new BigDecimalKeDecimal128(), new Decimal128KeBigDecimal()));
  }

  @WritingConverter
  static class BigDecimalKeDecimal128 implements Converter<BigDecimal, Decimal128> {
    @Override
    public Decimal128 convert(BigDecimal sumber) {
      return new Decimal128(sumber);
    }
  }

  @ReadingConverter
  static class Decimal128KeBigDecimal implements Converter<Decimal128, BigDecimal> {
    @Override
    public BigDecimal convert(Decimal128 sumber) {
      return sumber.bigDecimalValue();
    }
  }
}
