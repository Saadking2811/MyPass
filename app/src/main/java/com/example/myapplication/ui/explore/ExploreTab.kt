package com.example.myapplication.ui.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.ui.components.GoldAccent
import com.example.myapplication.ui.theme.LightGreen
import com.example.myapplication.ui.theme.PureWhite
import com.example.myapplication.ui.util.Destination
import com.example.myapplication.ui.util.Img
import com.example.myapplication.ui.util.popularDestinations

/** Bottom-nav "Explore" tab — inspirational hero + 2-column grid of destinations. */
@Composable
fun ExploreTab() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoldAccent(modifier = Modifier.height(28.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "Explore",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black, letterSpacing = (-0.6).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "Discover your next destination",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(220.dp),
                shape    = RoundedCornerShape(20.dp),
                elevation= CardDefaults.cardElevation(2.dp)
            ) {
                Box {
                    AsyncImage(
                        model = Img.HERO_AIRPORT, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.65f)), startY = 60f)
                    ))
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                        Text("Trending now", style = MaterialTheme.typography.labelMedium,
                            color = LightGreen, fontWeight = FontWeight.Bold)
                        Text("Summer escapes",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                            color = PureWhite)
                        Text("Hand-picked destinations to inspire your next trip",
                            style = MaterialTheme.typography.bodySmall, color = PureWhite.copy(0.85f))
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
        item {
            Text(
                "All destinations",
                modifier = Modifier.padding(horizontal = 20.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        item { Spacer(modifier = Modifier.height(12.dp)) }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().heightIn(min = 600.dp).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false
            ) {
                items(popularDestinations) { dest -> DestinationGridCard(dest) }
            }
        }
    }
}

@Composable
private fun DestinationGridCard(dest: Destination) {
    Card(
        modifier = Modifier.fillMaxWidth().height(180.dp),
        shape    = RoundedCornerShape(14.dp),
        elevation= CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = dest.imageUrl, contentDescription = dest.city,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(0.7f)), startY = 60f)
            ))
            Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
                Text(dest.city, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = PureWhite)
                Text(dest.country, style = MaterialTheme.typography.labelSmall,
                    color = PureWhite.copy(0.85f), fontWeight = FontWeight.Medium)
            }
        }
    }
}
