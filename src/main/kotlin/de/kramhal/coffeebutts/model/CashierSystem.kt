package de.kramhal.coffeebutts.model

import de.kramhal.coffeebutts.infrastructure.EventBus
import de.kramhal.coffeebutts.repositories.InvoiceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Version
import org.springframework.stereotype.Component

@Component
internal class CashierSystem(
        private val invoiceRepository: InvoiceRepository
) {
    init {
        GlobalScope.launch {
            EventBus.on<FrontDesk.Ordered>().consumeEach { invoice(it) }
        }
    }

    class Invoiced(invoice: Invoice) : EventBus.Event

    suspend fun invoice(ordered: FrontDesk.Ordered) {
        val invoice = Invoice(ordered.order.id)
        // berechne Betrag
        // ziehe Mengenrabatt ab
        invoiceRepository.save(invoice)
        EventBus.send(Invoiced(invoice))
    }

    suspend fun payOrder(orderId: OrderId) {
        val invoice = invoiceRepository.findById(orderId).awaitSingle()
        EventBus.send(invoice.pay())
    }
}

@ExperimentalCoroutinesApi
internal class Invoice(
        @Id val orderId: OrderId,
        @Version val version: Long? = null
) {
    enum class State { OPEN, PAID }

    var state : State = State.OPEN
        private set

    class Paid(val orderId: OrderId) : EventBus.Event
    fun pay(): Paid {
        // validiere Betrag
        // berechne Wechselgeld
        this.state = State.PAID
        return Paid(orderId)
    }
}
