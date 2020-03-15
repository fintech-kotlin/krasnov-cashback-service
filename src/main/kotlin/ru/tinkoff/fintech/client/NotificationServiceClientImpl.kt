package ru.tinkoff.fintech.client

import io.ktor.client.request.post
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class NotificationServiceClientImpl @Autowired constructor(
    private val httpClient: ApacheHttpClientService
) : NotificationServiceClient {
    @Value("\${service.url.notification}")
    private val url: String? = null

    override fun sendNotification(clientId: String, message: String) {
        return runBlocking {
            httpClient.getHttpClient().use {
                it.post<String> {
                    url("${this@NotificationServiceClientImpl.url}$clientId/message")
                    body = message
                    contentType(ContentType.Application.Json)
                }
                return@runBlocking
            }
        }
    }
}