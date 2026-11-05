package com.medicapp.medicacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.medicapp.ui.theme.MedicAppTheme

@Composable
fun PantallaMedicaciones(medicaciones: List<Medicacion>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Mis medicinas",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        items(medicaciones, key = { it.id }) { medicacion ->
            TarjetaMedicacion(medicacion)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PantallaMedicacionesPreview() {
    MedicAppTheme {
        PantallaMedicaciones(medicacionesDeEjemplo)
    }
}
