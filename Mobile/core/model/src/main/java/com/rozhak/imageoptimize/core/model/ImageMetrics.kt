package com.rozhak.imageoptimize.core.model

data class ImageMetrics(
    val width: Int,
    val height: Int,
    val bands: Int,
    val hasAlpha: Boolean,
    val format: String
)
