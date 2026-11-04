package com.medicapp.medicacion

import androidx.annotation.DrawableRes
import java.time.LocalDate

data class Medicacion(
    val id: Long,
    val nombre: String,
    val dosis: String,
    val frecuenciaHoras: Int,
    val fechaFin: LocalDate?,
    @DrawableRes val imagen: Int
)
