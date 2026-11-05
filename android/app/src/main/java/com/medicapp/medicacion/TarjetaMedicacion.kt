package com.medicapp.medicacion

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medicapp.ui.theme.MedicAppTheme
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formatoFecha =
    DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es-ES"))

@Composable
fun TarjetaMedicacion(medicacion: Medicacion, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(medicacion.imagen),
                contentDescription = "Imagen de ${medicacion.nombre}",
                modifier = Modifier.size(96.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = medicacion.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(text = medicacion.dosis, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = textoFrecuencia(medicacion.frecuenciaHoras),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(text = textoFechaFin(medicacion), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

fun textoFrecuencia(horas: Int): String = when (horas) {
    24 -> "Una vez al día"
    168 -> "Una vez a la semana"
    else -> "Cada $horas horas"
}

fun textoFechaFin(medicacion: Medicacion): String =
    medicacion.fechaFin?.let { "Hasta el ${it.format(formatoFecha)}" } ?: "Sin fecha de fin"

@Preview(showBackground = true)
@Preview(showBackground = true, fontScale = 1.5f, name = "Letra grande")
@Composable
private fun TarjetaMedicacionPreview() {
    MedicAppTheme {
        TarjetaMedicacion(medicacionesDeEjemplo.first())
    }
}
