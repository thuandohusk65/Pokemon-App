package com.example.pokemonapp.pokemonlist

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.navigation.NavController
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.lifecycle.viewmodel.compose.viewModel
import coil.Coil
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.pokemonapp.R
import com.example.pokemonapp.data.models.PokemonListEntry
import com.example.pokemonapp.ui.theme.RobotoCondensed
import com.example.pokemonapp.ui.theme.TypeWater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import org.jetbrains.annotations.Async

@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.logo_pokemon),
                contentDescription = "logo",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .align(CenterHorizontally)
            )
            searchBar(
                hint = "Search your Pokemon ...",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) { query ->
                viewModel.searchPokemonList(query)
            }
            Spacer(modifier = Modifier.height(16.dp))
            ShowPokemonList(
                navController
            )
        }
    }
}

@Composable
private fun searchBar(
    modifier: Modifier = Modifier,
    hint: String = "",
    onSearch: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isHintDisplay by remember { mutableStateOf(hint != null) }

    Box(modifier = modifier) {
        BasicTextField(
            value = text,
            onValueChange = {
                text = it
                onSearch(it)
            },
            maxLines = 1,
            singleLine = true,
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(5.dp, CircleShape)
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .onFocusChanged {
                    isHintDisplay = it.isFocused != true && text.isNotEmpty()
                }
        )
        if (isHintDisplay) {
            Text(
                text = hint,
                color = Color.LightGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(5.dp, CircleShape)
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun PokemonEntry(
    entry: PokemonListEntry,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val defaultDominantColor = MaterialTheme.colors.surface
    var dominantColor by remember { mutableStateOf(defaultDominantColor) }

    Box(contentAlignment = Center,
        modifier = modifier
            .shadow(5.dp, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .aspectRatio(1f)
            .background(
                Brush.verticalGradient(
                    listOf(dominantColor, defaultDominantColor)
                )
            )
            .clickable {
                navController.navigate("pokemon_detail_screen/${dominantColor.toArgb()}/${entry.pokemonName}")
            }) {
        Column {

            val request = ImageRequest.Builder(LocalContext.current)
                .data(entry.imageUrl)
                .build()
            LaunchedEffect(key1 = true) {
                val drawable = Coil.execute(request).drawable
                if (drawable != null) {
                    viewModel.calcDominantColor(drawable) {
                        dominantColor = it
                    }
                }
            }

            val painter = rememberImagePainter(
                request = request, imageLoader = ImageLoader.invoke(LocalContext.current)
            )
            Image(
                painter = painter, contentDescription = null,
                modifier = Modifier
                    .size(100.dp)
                    .align(CenterHorizontally)
            )
            Text(
                text = entry.pokemonName,
                fontFamily = RobotoCondensed,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(CenterHorizontally)
            )
        }
    }
}

@Composable
fun ShowPokemonList(
    navController: NavController,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    var pokemonList by remember { viewModel.pokemonlist }
    val endReached by remember { viewModel.endReached }
    val loadError by remember { viewModel.loadError }
    var isLoading by remember { viewModel.isloading }
    val isSearching by remember { viewModel.isSearching }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        val itemCount =
            if (pokemonList.size % 2 == 0) pokemonList.size / 2 else pokemonList.size / 2 + 1
        items(itemCount) {
            if (it <= itemCount - 1 && !endReached && !isLoading && !isSearching) {
                viewModel.loadPokemonPaginated()
            }
            PokeRow(rowIndex = it, entries = pokemonList, navController = navController)
        }
    }
}

@Composable
fun PokeRow(
    rowIndex: Int,
    entries: List<PokemonListEntry>,
    navController: NavController
) {
    Column {
        Row {
            PokemonEntry(
                entry = entries[rowIndex * 2],
                navController = navController,
                modifier = Modifier.weight(1F)
            )
            Spacer(
                modifier = Modifier.width(16.dp)
            )
            if (entries.size >= rowIndex * 2 + 2) {
                PokemonEntry(
                    entry = entries[rowIndex * 2 + 1],
                    navController = navController,
                    modifier = Modifier.weight(1F)
                )
            } else {
                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }

}
