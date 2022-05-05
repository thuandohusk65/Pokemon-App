package com.example.pokemonapp.data.remote.api

import com.example.pokemonapp.data.remote.responses.Pokemon
import com.example.pokemonapp.data.remote.responses.PokemonList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonAPI {

    @GET("pokemon")
    suspend fun getPokemonList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): PokemonList

    @GET("pokemon/{name}")
    suspend fun getPokemonInfo(
        @Path("name") name: String
    ) : Pokemon

}