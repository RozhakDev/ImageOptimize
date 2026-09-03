package com.rozhak.imageoptimize.core.network.service

import com.rozhak.imageoptimize.core.network.model.AnalysisDataDto
import com.rozhak.imageoptimize.core.network.model.HealthDataDto
import com.rozhak.imageoptimize.core.network.model.JSendResponse
import com.rozhak.imageoptimize.core.network.model.OptimizationDataDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit interface representing the Remote API contract for the Python FastAPI compression engine.
 * Ensures all network transactions strictly follow the JSend specification.
 */
interface ImageOptimizeService {
    /**
     * Pings the backend gateway to verify service availability.
     *
     * @return [JSendResponse] containing the service status.
     */
    @GET("health")
    suspend fun getHealth(): JSendResponse<HealthDataDto>

    /**
     * Uploads an image payload using multipart form data to extract raw image specifications.
     *
     * @param file The binary image part with MIME type 'image/any' or specific image format.
     * @return [JSendResponse] containing [AnalysisDataDto] with resolution and format metadata.
     */
    @Multipart
    @POST("api/v1/analyze/")
    suspend fun analyzeImage(
        @Part file: MultipartBody.Part
    ): JSendResponse<AnalysisDataDto>

    /**
     * Triggers the comprehensive optimization pipeline on the backend cluster.
     * Passes the binary image along with a series of control parameters defined by the user.
     *
     * @param file The physical image data.
     * @param targetSizeKb Target size constraint.
     * @param resizeWidth Target width scaling.
     * @param resizeHeight Target height scaling.
     * @param outputFormat The requested compression format.
     * @param preset Balancing profile (e.g., 'balanced').
     * @param smartCrop Enables bounding-box object detection cropping.
     * @param preserveMetadata Keeps original EXIF structure intact.
     * @return [JSendResponse] containing [OptimizationDataDto] with relative URL to the output resource.
     */
    @Multipart
    @POST("api/v1/images/")
    suspend fun optimizeImage(
        @Part file: MultipartBody.Part,
        @Part("target_size_kb") targetSizeKb: RequestBody?,
        @Part("resize_width") resizeWidth: RequestBody?,
        @Part("resize_height") resizeHeight: RequestBody?,
        @Part("output_format") outputFormat: RequestBody,
        @Part("preset") preset: RequestBody,
        @Part("smart_crop") smartCrop: RequestBody,
        @Part("preserve_metadata") preserveMetadata: RequestBody
    ): JSendResponse<OptimizationDataDto>
}
