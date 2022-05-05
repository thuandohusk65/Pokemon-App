package com.example.pokemonapp.pokemonlist

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.capitalize
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.example.pokemonapp.data.models.PokemonListEntry
import com.example.pokemonapp.repository.PokemonRepository
import com.example.pokemonapp.util.Constants
import com.example.pokemonapp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository
) : ViewModel() {

    private var curPage = 0
    var pokemonlist = mutableStateOf<List<PokemonListEntry>>(listOf())
    var loadError = mutableStateOf("")
    var isloading = mutableStateOf(false)
    var endReached = mutableStateOf(false)

    fun loadPokemonPaginated() {
        isloading.value = true
        viewModelScope.launch {
            val result =
                repository.getPokemonList(Constants.PAGE_SIZE, curPage * Constants.PAGE_SIZE)
            when (result) {
                is Resource.Success -> {
                    endReached.value == curPage * Constants.PAGE_SIZE >= result.data!!.count
                    val pokeDexEntries = result.data.results.mapIndexed { index, entry ->
                        val index = if (entry.url.endsWith("/")) {
                            entry.url.dropLast(1).takeLastWhile { it.isDigit() }
                        } else {
                            entry.url.takeLastWhile { it.isDigit() }
                        }
                        val url =
                            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${index}.png"
                        PokemonListEntry(entry.name.capitalize(Locale.ROOT), url, index.toInt())
                    }
                    loadError.value = ""
                    isloading.value = false
                    pokemonlist.value += pokeDexEntries
                }
                is Resource.Error -> {
                    loadError.value = result.message!!
                    isloading.value = false
                }
            }

        }
    }

    fun calcDominantColor(drawable: Drawable, onFinished: (Color) -> Unit) {
        val bmp = (drawable as BitmapDrawable).bitmap.copy(Bitmap.Config.ARGB_8888, true)

        Palette.from(bmp).generate { palette ->
            palette?.dominantSwatch?.rgb?.let { colorValue ->
                onFinished(Color(colorValue))
            }
        }
    }

    init {
        loadPokemonPaginated()
    }
}