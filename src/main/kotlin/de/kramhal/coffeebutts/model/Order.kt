package de.kramhal.coffeebutts.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.io.Serializable
import java.util.*

inline class OrderId(val id: String = UUID.randomUUID().toString()) : Serializable

internal data class Order(
        val coffee: Coffee,
        @Id val id: String = UUID.randomUUID().toString(),
        @Version val version: Long? = null
)