package ru.tinkoff.fintech.client

import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tinkoff.fintech.model.Card

@Service
class CardServiceClientImpl (
    @Value("\${service.url.card}") private val url: String,
    private val httpClient: ApacheHttpClientService
) : CardServiceClient {

    override fun getCard(id: String): Card {
        return runBlocking {
            return@runBlocking httpClient.getHttpClient().use {
                it.get {
                    url(this@CardServiceClientImpl.url + id)
                    contentType(ContentType.Application.Json)
                } as Card
            }
        }
    }
}