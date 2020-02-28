package de.kramhal.coffeebutts.repositories

import de.kramhal.coffeebutts.model.Order
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository

@Repository
internal interface OrderRepository : ReactiveCrudRepository<Order, String>