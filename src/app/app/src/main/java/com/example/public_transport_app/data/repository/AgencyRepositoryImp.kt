package com.example.public_transport_app.data.repository

import com.example.public_transport_app.data.entity.Agency
import com.example.public_transport_app.data.remote.API
import com.example.public_transport_app.utils.ErrorParser

class AgencyRepositoryImp(
    private val api:API
): AgencyRepository {
    override suspend fun getAgencies(): Resource<List<Agency>> {
        try {
            val result = api.getAgencies()
            val body = result.body()
            if(result.isSuccessful && body != null){
                return Resource.Success(body.agencies)
            }else{
                val errorBody = result.errorBody()
                val error = ErrorParser.parseErrorMessage(errorBody)
                return Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en getAgencies repository "+ex.message)
            return Resource.Error(null)
        }

    }

    /**
     * Verificar si el usuario es administrador de una agencia.
     * @param agencyId
     * @return Resource<Boolean>
     */
    override suspend fun verifyIfUserIsAgencyAdmin(agencyId: String): Resource<Boolean> {
        try {
            val result = api.verifyIfUserIsAgencyAdmin(agencyId)
            val body = result.body()
            if(result.isSuccessful && body != null){
                return Resource.Success(body.isAdmin)
            }else{
                val errorBody = result.errorBody()
                val error = ErrorParser.parseErrorMessage(errorBody)
                return Resource.Error(error)
            }
        } catch (ex: Exception) {
            println("Error en agency.verifyIfUserIsAgencyAdmin repository "+ex.message)
            return Resource.Error(null)
        }
    }

}