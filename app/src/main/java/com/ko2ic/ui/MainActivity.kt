package com.ko2ic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.ko2ic.Injection
import com.ko2ic.R
import com.ko2ic.ui.theme.Spikepaging3Theme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(
            this, Injection.provideViewModelFactory(
                context = this,
                owner = this
            )
        )[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Spikepaging3Theme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SearchScreen()
                }
            }
        }
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun SearchScreen() {

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val keyboardController = LocalSoftwareKeyboardController.current
        val inputValue = rememberSaveable { mutableStateOf(uiState.query) }

        Column {
            OutlinedTextField(
                value = inputValue.value,
                onValueChange = { inputValue.value = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    viewModel.accept(UiAction.Search(inputValue.value))
                    keyboardController?.hide() // Hide the keyboard after search
                }),
                label = { Text(text = stringResource(id = R.string.search_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            RepositoriesColumn()

        }
    }

    @Composable
    fun RepositoriesColumn() {
        val pagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
        LazyColumn {
            items(count = pagingItems.itemCount) { index ->
                pagingItems[index]?.let { uiModel ->

                    when (uiModel) {
                        is UiModel.RepoItem -> {
                            RepositoryItem(
                                repoName = uiModel.repo.name,
                                repoDescription = uiModel.repo.description ?: "",
                                language = uiModel.repo.language ?: "",
                                stars = uiModel.repo.stars,
                                forks = uiModel.repo.forks
                            )
                        }

                        is UiModel.SeparatorItem -> {
                            SeparatorLayout(uiModel.description)
                        }
                    }
                }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewSearchScreen() {
        SearchScreen()
    }

    @Composable
    fun RepositoryItem(
        repoName: String,
        repoDescription: String,
        language: String,
        stars: Int,
        forks: Int
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp)
        ) {
            Text(
                text = repoName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = repoDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = language,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stars.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_git_branch),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = forks.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewRepositoryItem() {
        RepositoryItem(
            repoName = "android-architecture",
            repoDescription = "A collection of samples to discuss and showcase different architectural tools and patterns for Android apps.",
            language = "Kotlin",
            stars = 30,
            forks = 30
        )
    }

    @Composable
    fun SeparatorLayout(description: String) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .background(color = Color.Gray)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            Text(
                text = description,
                fontSize = 16.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    @Preview
    @Composable
    fun PreviewSeparatorLayout() {
        SeparatorLayout("10000+ stars")
    }

}

