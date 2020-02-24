package de.kramhal.coffeebutts.model

import java.io.Serializable
import java.util.*

inline class OrderId(val uuid: String = UUID.randomUUID().toString()) : Serializable

internal class Order(coffee: Coffee) : BaseEntity<OrderId>(OrderId())