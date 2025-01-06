@file:OptIn(ExperimentalMaterial3Api::class)

package ph.edu.auf.apidiscussion

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ph.edu.auf.apidiscussion.ui.theme.APIDIscussionTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Weather API Service
interface WeatherApiService {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

// Retrofit Instance
object RetrofitInstance {
    private const val BASE_URL = "https://api.openweathermap.org/"
    val api: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}

// Data Classes
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind
)

data class Main(val temp: Double, val pressure: Int, val humidity: Int)
data class Weather(val description: String)
data class Wind(val speed: Double, val deg: Int)

// Main Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIDIscussionTheme {
                WeatherApp()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherApp() {
    val cityInput = remember { mutableStateOf(TextFieldValue("")) }
    val weatherData = remember { mutableStateOf<WeatherResponse?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather App") }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Underline above search bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Divider(
                    color = MaterialTheme.colorScheme.primary,
                    thickness = 2.dp
                )
            }

            // Search Bar
            OutlinedTextField(
                value = cityInput.value,
                onValueChange = { cityInput.value = it },
                label = { Text("Enter City Name") },
                placeholder = { Text("Type city name here...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                trailingIcon = {
                    if (cityInput.value.text.isNotEmpty()) {
                        IconButton(onClick = { cityInput.value = TextFieldValue("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        keyboardController?.hide()
                        errorMessage = ""
                        scope.launch(Dispatchers.IO) {
                            if (cityInput.value.text.isBlank()) {
                                errorMessage = "Please enter a city name."
                                return@launch
                            }
                            isLoading.value = true
                            try {
                                val response = RetrofitInstance.api.getWeather(
                                    city = cityInput.value.text,
                                    apiKey = "f61be9a0675aa5c6470d95b2b0ccf7b0"
                                )
                                weatherData.value = response
                            } catch (e: Exception) {
                                errorMessage = "Error fetching data. Please try again."
                                e.printStackTrace()
                            } finally {
                                isLoading.value = false
                            }
                        }
                    }
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = {
                    errorMessage = ""
                    scope.launch(Dispatchers.IO) {
                        if (cityInput.value.text.isBlank()) {
                            errorMessage = "Please enter a city name."
                            return@launch
                        }
                        isLoading.value = true
                        try {
                            val response = RetrofitInstance.api.getWeather(
                                city = cityInput.value.text,
                                apiKey = "f61be9a0675aa5c6470d95b2b0ccf7b0"
                            )
                            weatherData.value = response
                        } catch (e: Exception) {
                            errorMessage = "Error fetching data. Please try again."
                            e.printStackTrace()
                        } finally {
                            isLoading.value = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Error Message
            if (errorMessage.isNotBlank()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            // Weather Info Display
            if (isLoading.value) {
                CircularProgressIndicator()
            } else {
                weatherData.value?.let { weather ->
                    Text("City: ${weather.name}")
                    Text("Temperature: ${weather.main.temp}°C")
                    Text("Description: ${weather.weather[0].description}")
                    Text("Humidity: ${weather.main.humidity}%")
                    Text("Wind Speed: ${weather.wind.speed} m/s")
                    Text("Wind Direction: ${weather.wind.deg}°")
                    Text("Air Pressure: ${weather.main.pressure} hPa")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WeatherAppPreview() {
    APIDIscussionTheme {
        WeatherApp()
    }
}
