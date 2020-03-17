package ru.tinkoff.fintech.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import org.springframework.stereotype.Service
import ru.tinkoff.fintech.ObjectMapperSupplier

@Service
class ApacheHttpClientService {

    fun getHttpClient(): HttpClient {
        return HttpClient(Apache) {
            install(JsonFeature) {
                serializer = JacksonSerializer(ObjectMapperSupplier.objectMapper) {}
            }
        }
    }
}