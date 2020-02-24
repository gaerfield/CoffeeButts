package de.kramhal.coffeebutts.model

import java.io.Serializable

internal abstract class BaseEntity<T : Serializable>(
        val id: T,
        val version: Long? = null
) {
    override fun equals(other: Any?) = when {
        this === other -> true
        javaClass != other?.javaClass -> false
        else -> id == (other as BaseEntity<*>).id
    }

    override fun hashCode(): Int = id.hashCode()
}