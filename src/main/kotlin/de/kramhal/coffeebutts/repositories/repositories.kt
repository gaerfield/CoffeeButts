package de.kramhal.coffeebutts.repositories

import com.fasterxml.jackson.databind.ObjectMapper
import de.kramhal.coffeebutts.model.Invoice
import de.kramhal.coffeebutts.model.InvoiceId
import de.kramhal.coffeebutts.model.Order
import de.kramhal.coffeebutts.model.OrderId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

suspend inline fun <T> ReactiveMongoTemplate.saveAndAwait(entity: T) : T = save(entity).awaitSingle()
suspend inline fun <reified T> ReactiveMongoTemplate.findById(id: Any) : T = findById(id, T::class.java).awaitSingle()

@Repository
internal class OrderRepository(private val template: ReactiveMongoTemplate,
                      private val objectMapper: ObjectMapper) {
    suspend fun save(order: Order) : Order = template.saveAndAwait(order)
    suspend fun findById(orderId: OrderId): Order = template.findById(orderId.id)

}

@Repository
internal interface InvoiceRepository : ReactiveCrudRepository<Invoice, InvoiceId> {
    fun findByOrderId(orderId: OrderId) : Mono<Invoice>
}