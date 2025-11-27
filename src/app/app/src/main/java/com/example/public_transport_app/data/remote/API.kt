package com.example.public_transport_app.data.remote

import com.example.public_transport_app.data.remote.dto.request.AddRunPointRequest
import com.example.public_transport_app.data.remote.dto.request.CreateStopRequest
import com.example.public_transport_app.data.remote.dto.request.StartRunRequest
import com.example.public_transport_app.data.remote.dto.request.UpdateRouteStopsRequest
import com.example.public_transport_app.data.remote.dto.response.AddRunPointResponse
import com.example.public_transport_app.data.remote.dto.response.CreateRouteResponse
import com.example.public_transport_app.data.remote.dto.response.CreateStopResponse
import com.example.public_transport_app.data.remote.dto.response.FinishRunResponse
import com.example.public_transport_app.data.remote.dto.response.GetAgenciesResponse
import com.example.public_transport_app.data.remote.dto.response.GetBestRouteRunResponse
import com.example.public_transport_app.data.remote.dto.response.GetMatchedRunPointsResponse
import com.example.public_transport_app.data.remote.dto.response.GetNearbyRunsResponse
import com.example.public_transport_app.data.remote.dto.response.GetNearbyStopsByRouteResponse
import com.example.public_transport_app.data.remote.dto.response.GetRoutePublicDataResponse
import com.example.public_transport_app.data.remote.dto.response.GetRouteResponse
import com.example.public_transport_app.data.remote.dto.response.GetRouteStopsResponse
import com.example.public_transport_app.data.remote.dto.response.GetRoutesResponse
import com.example.public_transport_app.data.remote.dto.response.GetStopsResponse
import com.example.public_transport_app.data.remote.dto.response.StartRunResponse
import com.example.public_transport_app.data.remote.dto.response.UpdateRouteStopsResponse
import com.example.public_transport_app.data.remote.dto.response.VerifyIfUserIsAgencyAdminResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import okhttp3.RequestBody
import okhttp3.MultipartBody
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface API {

    @GET("/api/agency")
    suspend fun getAgencies(): Response<GetAgenciesResponse>

    @Multipart
    @POST("/api/route")
    suspend fun createRoute(
        @Part("agencyId") agencyId: RequestBody,
        @Part("startLocation") startLocation: RequestBody,
        @Part("endLocation") endLocation: RequestBody,
        @Part("units") units: RequestBody,
        @Part("name") name: RequestBody,
        @Part("schedules") schedules: RequestBody,
        @Part unitImages: List<MultipartBody.Part>
    ): Response<CreateRouteResponse>

    @GET("/api/route")
    suspend fun getRoutesList(
        @Query("agencyId") agencyId: String
    ): Response<GetRoutesResponse>

    @GET("/api/route/{routeId}")
    suspend fun getRoute(
        @Path("routeId") routeId: String,
    ): Response<GetRouteResponse>

    @POST("/api/route/run")
    suspend fun startRun(
        @Body request: StartRunRequest
    ): Response<StartRunResponse>

    @POST("/api/route/run/{runId}/point")
    suspend fun addRunPoint(
        @Path("runId") runId: String,
        @Body request: AddRunPointRequest
    ): Response<AddRunPointResponse>

    @POST("/api/route/run/{runId}/finish")
    suspend fun finishRun(
        @Path("runId") runId: String,
    ): Response<FinishRunResponse>

    @GET("/api/route/run/nearby")
    suspend fun getNearbyActiveRuns(
        @Query("lat") lat: Double,
        @Query("long") lon: Double
    ):Response<GetNearbyRunsResponse>

    @GET("/api/route/run/matched-points/{endToEndRunId}")
    suspend fun getMatchedPoints(
        @Path("endToEndRunId") endToEndRunId: String
    ): Response<GetMatchedRunPointsResponse>

    @GET("/api/route/{routeId}/public")
    suspend fun getRoutePublicData(
        @Path("routeId") routeId: String
    ): Response<GetRoutePublicDataResponse>

    @GET("/api/route/{routeId}/stop")
    suspend fun getRouteStops(
        @Path("routeId") routeId: String
    ): Response<GetRouteStopsResponse>

    @PUT("/api/route/{routeId}/stop")
    suspend fun updateRouteStops(
        @Path("routeId") routeId: String,
        @Body request: UpdateRouteStopsRequest
    ): Response<UpdateRouteStopsResponse>

    @GET("/api/stop")
    suspend fun getStops(
        @Query("agencyId") agencyId: String
    ): Response<GetStopsResponse>

    @POST("/api/stop")
    suspend fun createStop(
        @Body request: CreateStopRequest
    ): Response<CreateStopResponse>

    @GET("/api/agency/{agencyId}/is-admin")
    suspend fun verifyIfUserIsAgencyAdmin(
        @Path("agencyId") agencyId: String
    ): Response<VerifyIfUserIsAgencyAdminResponse>

    @GET("/api/stop/nearby/by-route")
    suspend fun getNearbyStopsByRoute(
        @Query("lat") lat: Double,
        @Query("long") long: Double,
    ): Response<GetNearbyStopsByRouteResponse>

    @GET("/api/route/{routeId}/run/best")
    suspend fun getBestRouteRun(
        @Path("routeId") routeId: String,
    ): Response<GetBestRouteRunResponse>

}