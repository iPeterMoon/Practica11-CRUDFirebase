package com.example.practica11


data class Luchador(
    var id: Int = 0,
    var nombre : String = ""
) {
    override fun toString(): String {
        return nombre ?: "Sin nombre"
    }
}
