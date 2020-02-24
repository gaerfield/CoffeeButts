package de.kramhal.coffeebutts.model

class Coffee(
        val type : Type
) {
    enum class Type { LatteMachiatto, Cappucino, Espresso, DoubleEspresso }
}