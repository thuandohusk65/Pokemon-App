package com.example.pokemonapp.di

import com.example.pokemonapp.data.remote.api.PokemonAPI
import com.example.pokemonapp.repository.PokemonRepository
import com.example.pokemonapp.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun providePokemonRepository(
        api: PokemonAPI
    ) = PokemonRepository(api)

    @Singleton
    @Provides
    fun ProvidePokemonApi(): PokemonAPI {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(Constants.BASE_URL)
            .build()
            .create(PokemonAPI::class.java)
    }
}