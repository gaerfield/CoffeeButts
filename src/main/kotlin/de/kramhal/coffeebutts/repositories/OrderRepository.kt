package de.kramhal.coffeebutts.repositories

import de.kramhal.coffeebutts.model.Invoice
import de.kramhal.coffeebutts.model.InvoiceId
import de.kramhal.coffeebutts.model.Order
import de.kramhal.coffeebutts.model.OrderId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@ExperimentalCoroutinesApi
@Repository
internal interface OrderRepository : ReactiveCrudRepository<Order, OrderId>

@ExperimentalCoroutinesApi
@Repository
internal interface InvoiceRepository : ReactiveCrudRepository<Invoice, InvoiceId> {
    @ExperimentalCoroutinesApi
    fun findByOrderId(orderId: OrderId) : Mono<Invoice>
}