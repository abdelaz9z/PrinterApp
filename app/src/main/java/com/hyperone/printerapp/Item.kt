package com.hyperone.printerapp

data class Item(
    val name: String,
    val price: Double = 0.0,
    val quantity: Double = 0.0,
    val sku: String,
    val unitOfMeasurement: String,
    var imageUrl: String? = null
) {
    constructor() : this(
        name = "",
        price = 0.0,
        quantity = 0.0,
        sku = "",
        unitOfMeasurement = "",
        imageUrl = null
    )
}
