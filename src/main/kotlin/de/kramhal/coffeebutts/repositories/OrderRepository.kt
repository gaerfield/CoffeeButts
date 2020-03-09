package de.kramhal.coffeebutts.repositories

import de.kramhal.coffeebutts.model.Invoice
import de.kramhal.coffeebutts.model.Order
import de.kramhal.coffeebutts.model.OrderId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface OrderRepository : ReactiveCrudRepository<Order, OrderId>

@Repository
internal interface InvoiceRepository : ReactiveCrudRepository<Invoice, OrderId>