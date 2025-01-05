package ph.edu.auf.apidiscussion

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
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
        @Query("units") units: String = "metric" // Fetch data in Celsius
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

// WeatherResponse Data Class
data class WeatherResponse(
    val name: String,
    val main: Main,
    val weather: List<Weather>
)

data class Main(val temp: Double)
data class Weather(val description: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APIDIscussionTheme {
                WeatherScreen()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WeatherScreen() {
    val weatherData = remember { mutableStateOf<WeatherResponse?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // Replace with your city and OpenWeatherMap API key
                val response = RetrofitInstance.api.getWeather("YourCity", "f61be9a0675aa5c6470d95b2b0ccf7b0")
                weatherData.value = response
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold {
        val weather = weatherData.value
        if (weather != null) {
            Text(
                text = "City: ${weather.name}\n" +
                        "Temperature: ${weather.main.temp}°C\n" +
                        "Description: ${weather.weather[0].description}",
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Text(text = "Fetching weather data...", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    APIDIscussionTheme {
        WeatherScreen()
    }
}
