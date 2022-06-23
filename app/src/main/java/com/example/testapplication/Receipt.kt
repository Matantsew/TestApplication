package com.example.testapplication

class Receipt {
    var name: String? = null
    var age: String? = null

    var cashier: String? = null
    var docId: String? = null
    var docNumber: String? = null

    override fun toString(): String {
        return "Receipt: $name - $age"
    }
}