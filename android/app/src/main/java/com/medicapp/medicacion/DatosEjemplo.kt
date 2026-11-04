package com.medicapp.medicacion

import com.medicapp.R
import java.time.LocalDate

val medicacionesDeEjemplo = listOf(
    Medicacion(1, "Paracetamol", "1 comprimido", 8, LocalDate.of(2026, 11, 15), R.drawable.ic_medicacion_defecto),
    Medicacion(2, "Omeprazol", "1 cápsula en ayunas", 24, null, R.drawable.ic_medicacion_defecto),
    Medicacion(3, "Enalapril", "1 comprimido", 12, null, R.drawable.ic_medicacion_defecto),
    Medicacion(4, "Vitamina D", "1 ampolla", 168, LocalDate.of(2027, 1, 31), R.drawable.ic_medicacion_defecto)
)
