package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.Agency

interface AgencyRepository {

    suspend fun getAgencies():Resource<List<Agency>>

    /**
     * Verificar si el usuario es administrador de una agencia.
     * @param agencyId
     * @return Resource<Boolean>
     */
    suspend fun verifyIfUserIsAgencyAdmin(agencyId: String): Resource<Boolean>
}