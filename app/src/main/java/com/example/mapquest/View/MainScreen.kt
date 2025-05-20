package com.example.mapquest

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import com.example.mapquest.Model.LocationResult
import com.example.mapquest.Viewmodel.SearchViewModel
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: SearchViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current


    LaunchedEffect(query) {
        viewModel.searchAddress(query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
       // tìm kiếm
        TextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Enter keyword") },
            leadingIcon = {
                Box {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Icon",
                            tint = Color.Black
                        )
                    }
                }
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = { query = "" }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear text",
                            tint = Color.Black
                        )
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color(0xFFF5F5F5),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.searchAddress(query) })
        )
// lỗi
        state.errorMessage?.let { errorCode ->
            val friendlyMessage = when (errorCode) {
                "HTTP 404" -> "Không tìm thấy địa điểm bạn yêu cầu"
                "500" -> "Lỗi hệ thống, vui lòng thử lại sau"
                "network_error" -> "Mất kết nối mạng, vui lòng kiểm tra lại"
                else -> "Không tìm thấy địa điểm bạn yêu cầu"
            }

            Text(
                text = friendlyMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn {
            items(state.results) { result ->
                LocationItem(
                    result = result,
                    query = query,
                    onClick = { viewModel.openInGoogleMaps(context, result) } // Truyền Context
                )
            }
        }
    }
}

@Composable
fun LocationItem(result: LocationResult, query: String, onClick: () -> Unit) {
    val highlightedText = remember(result.displayName, query) {
        if (query.isNotBlank()) {
            val startIndex = result.displayName.lowercase().indexOf(query.lowercase())
            if (startIndex >= 0) {
                AnnotatedString(
                    text = result.displayName,
                    spanStyles = listOf(
                        AnnotatedString.Range(
                            SpanStyle(color = Color.Blue, fontWeight = FontWeight.Bold),
                            start = startIndex,
                            end = startIndex + query.length
                        )
                    )
                )
            } else {
                AnnotatedString(result.displayName)
            }
        } else {
            AnnotatedString(result.displayName)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.location),
                contentDescription = "Location Icon",
                modifier = Modifier
                    .size(24.dp)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = highlightedText,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.Black
                )
                Text(
                    text = result.address,
                    style = TextStyle(fontSize = 14.sp),
                    color = Color.Gray
                )
            }
            Image(
                painter = painterResource(id = R.drawable.direction),
                contentDescription = "Direction Icon",
                modifier = Modifier
                    .size(24.dp)
            )
        }

    }
}