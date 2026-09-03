package com.rozhak.imageoptimize.ui.optimization

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rozhak.imageoptimize.core.designsystem.component.WorkspaceTopAppBar

@Composable
fun OptimizationScreen(
    viewModel: OptimizationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.onImageSelected(context, it) } }
    )

    Scaffold(
        topBar = {
            WorkspaceTopAppBar(title = stringResource(com.rozhak.imageoptimize.R.string.title_optimization))
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    photoPickerLauncher.launch(
                        androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = "Pick Image") },
                text = { Text(stringResource(com.rozhak.imageoptimize.R.string.btn_pick_image)) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        if (uiState.selectedImageFile == null && !uiState.isAnalyzing && uiState.error == null) {
            OptimizationEmptyState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                uiState.error?.let { error ->
                    OptimizationErrorCard(error = error)
                }

                if (uiState.isAnalyzing) {
                    AnalyzingIndicatorCard()
                } else if (uiState.imageMetrics != null) {
                    ImageMetricsCard(metrics = uiState.imageMetrics!!)
                }

                if (uiState.selectedImageFile != null && !uiState.isAnalyzing) {
                    OptimizationParametersCard(
                        uiState = uiState,
                        onTargetKbChange = viewModel::updateTargetKb,
                        onResizeWidthChange = viewModel::updateResizeWidth,
                        onResizeHeightChange = viewModel::updateResizeHeight,
                        onFormatChange = viewModel::updateFormat,
                        onPresetChange = viewModel::updatePreset,
                        onSmartCropChange = viewModel::updateSmartCrop,
                        onPreserveMetadataChange = viewModel::updatePreserveMetadata,
                        onOptimizeClick = viewModel::optimizeImage
                    )
                }

                uiState.optimizedUrl?.let { url ->
                    ComparisonResultCard(
                        originalFile = uiState.selectedImageFile,
                        optimizedUrl = url,
                        onSaveClick = { viewModel.saveToGallery(context) }
                    )
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
