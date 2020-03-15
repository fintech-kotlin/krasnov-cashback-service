package ru.tinkoff.fintech.client

import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.tinkoff.fintech.model.Client

@Service
class ClientServiceImpl @Autowired constructor(
    private val httpClient: ApacheHttpClientService
) : ClientService {

    @Value("\${service.url.client}")
    private val url: String? = null

    override fun getClient(id: String): Client {
        return runBlocking {
            return@runBlocking httpClient.getHttpClient().use {
                it.get {
                    url(this@ClientServiceImpl.url + id)
                    contentType(ContentType.Application.Json)
                } as Client
            }
        }
    }
}