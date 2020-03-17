package ru.tinkoff.fintech

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

object ObjectMapperSupplier {

    val objectMapper: ObjectMapper = ObjectMapper().registerModules(KotlinModule(), JavaTimeModule())
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .setDateFormat(StdDateFormat())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
}