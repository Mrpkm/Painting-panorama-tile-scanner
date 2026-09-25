package com.example.photomosaicscanner.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.photomosaicscanner.grid.GridCalculator
import com.example.photomosaicscanner.model.PaintingUnits
import com.example.photomosaicscanner.ui.components.GridDiagram
import com.example.photomosaicscanner.ui.components.TileVisualState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(onConfirm: (Double, Double, PaintingUnits, Int, Int, Int) -> Unit) {
    var widthText by remember { mutableStateOf("80") }
    var heightText by remember { mutableStateOf("120") }
    var units by remember { mutableStateOf(PaintingUnits.CM) }
    var unitsExpanded by remember { mutableStateOf(false) }
    var overlap by remember { mutableStateOf(40f) }
    var columnsText by remember { mutableStateOf("5") }
    var rowsText by remember { mutableStateOf("8") }
    var suggestionNote by remember { mutableStateOf<String?>(null) }

    val width = widthText.toDoubleOrNull() ?: 0.0
    val height = heightText.toDoubleOrNull() ?: 0.0
    val columns = (columnsText.toIntOrNull() ?: 1).coerceAtLeast(1)
    val rows = (rowsText.toIntOrNull() ?: 1).coerceAtLeast(1)

    val overlapWarning = when {
        overlap < 20f -> "Low overlap may make stitching in Hugin unreliable."
        overlap > 60f -> "High overlap means many more photographs than necessary."
        else -> null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("New Painting Scan", style = MaterialTheme.typography.headlineSmall)

        Text("Painting dimensions", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = widthText,
                onValueChange = { widthText = it },
                label = { Text("Width") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            OutlinedTextField(
                value = heightText,
                onValueChange = { heightText = it },
                label = { Text("Height") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        ExposedDropdownMenuBox(expanded = unitsExpanded, onExpandedChange = { unitsExpanded = it }) {
            OutlinedTextField(
                value = units.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Units") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitsExpanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            androidx.compose.material3.ExposedDropdownMenu(
                expanded = unitsExpanded,
                onDismissRequest = { unitsExpanded = false }
            ) {
                PaintingUnits.values().forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            units = option
                            unitsExpanded = false
                        }
                    )
                }
            }
        }

        Text("Overlap: ${overlap.toInt()}%", style = MaterialTheme.typography.titleMedium)
        Slider(value = overlap, onValueChange = { overlap = it }, valueRange = 10f..70f)
        overlapWarning?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Text("Grid", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = columnsText,
                onValueChange = { columnsText = it },
                label = { Text("Columns") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = rowsText,
                onValueChange = { rowsText = it },
                label = { Text("Rows") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        OutlinedButton(onClick = {
            val suggestion = GridCalculator.suggestGrid(
                paintingWidthCm = width * units.toCmFactor,
                paintingHeightCm = height * units.toCmFactor,
                overlapPercent = overlap.toInt()
            )
            columnsText = suggestion.columns.toString()
            rowsText = suggestion.rows.toString()
            suggestionNote = suggestion.note
        }) { Text("Suggest a starting grid (estimate)") }

        suggestionNote?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
        }

        Text(
            "Preview: $rows × $columns = ${rows * columns} photographs",
            style = MaterialTheme.typography.titleMedium
        )
        GridDiagram(
            rows = rows,
            columns = columns,
            aspectRatio = (if (width > 0) width.toFloat() else 1f) / (if (height > 0) height.toFloat() else 1f),
            tileState = { TileVisualState.PENDING },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Button(
            onClick = { onConfirm(width, height, units, overlap.toInt(), rows, columns) },
            modifier = Modifier.fillMaxWidth(),
            enabled = width > 0 && height > 0 && rows > 0 && columns > 0
        ) { Text("Confirm Grid & Start Scan") }
    }
}
