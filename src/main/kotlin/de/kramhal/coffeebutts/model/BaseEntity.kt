package de.kramhal.coffeebutts.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import java.io.Serializable

internal abstract class BaseEntity<T : Serializable> (
        @Id val id: T,
        @Version val version: Long? = null
) {
    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        else -> id == (other as BaseEntity<*>).id
    }

    override fun hashCode(): Int = id.hashCode()
}