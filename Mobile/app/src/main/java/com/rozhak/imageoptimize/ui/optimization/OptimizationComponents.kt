package com.rozhak.imageoptimize.ui.optimization

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import coil.compose.AsyncImage
import com.rozhak.imageoptimize.core.designsystem.component.WorkspaceCard
import com.rozhak.imageoptimize.core.model.ImageMetrics
import java.io.File

@Composable
fun OptimizationEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.Info,
                contentDescription = "Empty State",
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(com.rozhak.imageoptimize.R.string.opt_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(com.rozhak.imageoptimize.R.string.opt_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun OptimizationErrorCard(error: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Text(text = error, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
    }
}

@Composable
fun AnalyzingIndicatorCard() {
    WorkspaceCard {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(com.rozhak.imageoptimize.R.string.opt_analyzing), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ImageMetricsCard(metrics: ImageMetrics) {
    WorkspaceCard {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(com.rozhak.imageoptimize.R.string.opt_metrics_title), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("${stringResource(com.rozhak.imageoptimize.R.string.opt_metrics_resolution)}: ${metrics.width} x ${metrics.height} px", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${stringResource(com.rozhak.imageoptimize.R.string.opt_metrics_color_characteristics)}: Alpha=${metrics.hasAlpha} | Bands=${metrics.bands}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${stringResource(com.rozhak.imageoptimize.R.string.opt_metrics_original_format)}: ${metrics.format}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizationParametersCard(
    uiState: OptimizationState,
    onTargetKbChange: (String) -> Unit,
    onResizeWidthChange: (String) -> Unit,
    onResizeHeightChange: (String) -> Unit,
    onFormatChange: (String) -> Unit,
    onPresetChange: (String) -> Unit,
    onSmartCropChange: (Boolean) -> Unit,
    onPreserveMetadataChange: (Boolean) -> Unit,
    onOptimizeClick: () -> Unit
) {
    var formatExpanded by remember { mutableStateOf(false) }
    val formatOptions = listOf("auto", "jpeg", "webp", "avif", "png")

    var presetExpanded by remember { mutableStateOf(false) }
    val presetOptions = listOf("custom", "balanced", "high_quality", "maximum_compression", "web", "thumbnail")

    WorkspaceCard {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(com.rozhak.imageoptimize.R.string.opt_params_title), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.targetKb,
                onValueChange = onTargetKbChange,
                label = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_target_size)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = uiState.resizeWidth,
                    onValueChange = onResizeWidthChange,
                    label = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_width)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.resizeHeight,
                    onValueChange = onResizeHeightChange,
                    label = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_height)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = formatExpanded,
                onExpandedChange = { formatExpanded = !formatExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.outputFormat,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_output_format)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = formatExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = formatExpanded,
                    onDismissRequest = { formatExpanded = false }
                ) {
                    formatOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onFormatChange(option)
                                formatExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            ExposedDropdownMenuBox(
                expanded = presetExpanded,
                onExpandedChange = { presetExpanded = !presetExpanded }
            ) {
                OutlinedTextField(
                    value = uiState.preset,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_preset)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = presetExpanded,
                    onDismissRequest = { presetExpanded = false }
                ) {
                    presetOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onPresetChange(option)
                                presetExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            ListItem(
                headlineContent = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_smart_crop_title)) },
                supportingContent = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_smart_crop_subtitle)) },
                trailingContent = {
                    Switch(checked = uiState.smartCrop, onCheckedChange = onSmartCropChange)
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
            
            ListItem(
                headlineContent = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_preserve_exif_title)) },
                supportingContent = { Text(stringResource(com.rozhak.imageoptimize.R.string.opt_preserve_exif_subtitle)) },
                trailingContent = {
                    Switch(checked = uiState.preserveMetadata, onCheckedChange = onPreserveMetadataChange)
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onOptimizeClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !uiState.isOptimizing
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(com.rozhak.imageoptimize.R.string.opt_btn_optimize))
            }

            if (uiState.isOptimizing) {
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(stringResource(com.rozhak.imageoptimize.R.string.opt_processing), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
fun ComparisonResultCard(
    originalFile: File?,
    optimizedUrl: String,
    onSaveClick: () -> Unit
) {
    WorkspaceCard {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Text(stringResource(com.rozhak.imageoptimize.R.string.opt_result_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(com.rozhak.imageoptimize.R.string.opt_result_original), style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = originalFile,
                        contentDescription = "Original Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(com.rozhak.imageoptimize.R.string.opt_result_optimized), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = optimizedUrl,
                        contentDescription = "Optimized Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = onSaveClick,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(stringResource(com.rozhak.imageoptimize.R.string.opt_result_save_gallery))
            }
        }
    }
}
