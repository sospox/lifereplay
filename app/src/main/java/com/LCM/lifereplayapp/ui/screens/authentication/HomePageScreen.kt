package com.LCM.lifereplayapp.ui.screens.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.LCM.lifereplayapp.ui.navigation.ROUTES
// Add this to help with the background modifier in the Pager Indicators
import androidx.compose.foundation.background

data class PagerItem(
    val title: String,
    val description: String,
    val imageUrl: String
)

@Composable
fun HomePageScreen(navController: NavHostController, modifier: Modifier) {
    val pagerItems = listOf(
        PagerItem(
            title = "Capture Your Life",
            description = "Life Replay allows you to capture and organize your most precious moments effortlessly.",
            // IMAGE LINK FOR PAGE 1
            imageUrl = "https://images.pexels.com/photos/36851763/pexels-photo-36851763.jpeg"
        ),
        PagerItem(
            title = "Relive Memories",
            description = "Browse through your timeline and relive your best experiences with high-quality playback.",
            // IMAGE LINK FOR PAGE 2
            imageUrl = "https://images.pexels.com/photos/36510987/pexels-photo-36510987.jpeg"
        ),
        PagerItem(
            title = "Secure & Private",
            description = "Your data is encrypted and stored securely, ensuring that your memories stay private.",
            // IMAGE LINK FOR PAGE 3
            imageUrl = "https://images.pexels.com/photos/31415466/pexels-photo-31415466.jpeg"
        )
    )

    val pagerState = rememberPagerState(pageCount = { pagerItems.size })

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = { navController.navigate(ROUTES.Login.name) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Get Started", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { navController.navigate(ROUTES.Signup.name) }) {
                    Text("Don't have an account? Sign Up")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to Life Replay",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp),
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 32.dp),
                pageSpacing = 16.dp
            ) { page ->
                PagerCard(pagerItems[page])
            }

            // Pager Indicators
            Row(
                Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerItems.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    }
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .clip(RoundedCornerShape(50))
                            .size(10.dp)
                            .background(color)
                    )
                }
            }
        }
    }
}

@Composable
fun PagerCard(item: PagerItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.8f),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.5f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                contentScale = ContentScale.Crop
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


