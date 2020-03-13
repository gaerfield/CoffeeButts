package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import de.kramhal.coffeebutts.repositories.InvoiceRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.stereotype.Component
import java.io.Serializable
import java.util.*

@Component
internal class CashierSystem(
        private val eventBus: EventBus,
        private val invoiceRepository: InvoiceRepository
) {
    init {
        GlobalScope.launch {
            eventBus.on<FrontDesk.Ordered>().onEach { invoice(it) }
        }
    }

    data class Invoiced(val invoice: Invoice) : EventBus.Event

    suspend fun invoice(ordered: FrontDesk.Ordered) {
        val invoice = Invoice(ordered.order.id)
        // berechne Betrag
        // ziehe Mengenrabatt ab
        invoiceRepository.save(invoice).awaitSingle()
        eventBus.send(Invoiced(invoice))
    }

    suspend fun payOrder(orderId: OrderId) {
        val invoice = invoiceRepository.findByOrderId(orderId).awaitSingle()
        val paid = invoice.pay()
        invoiceRepository.save(invoice).awaitSingle()
        eventBus.send(paid)
    }
}

inline class InvoiceId(val id: String = UUID.randomUUID().toString()) : Serializable

@Document
internal class Invoice(
        val orderId: OrderId
) {
    enum class State { OPEN, PAID }

    var state : State = State.OPEN
        private set

    data class Paid(val orderId: OrderId) : EventBus.Event
    fun pay(): Paid {
        // validiere Betrag
        // berechne Wechselgeld
        this.state = State.PAID
        return Paid(orderId)
    }
}
