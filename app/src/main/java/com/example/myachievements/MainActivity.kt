import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.myachievements.R

// Data classes for API response
data class PlayerAchievementsResponse(val playerstats: PlayerStats)
data class PlayerStats(val achievements: List<Achievement>)
data class Achievement(val apiname: String, val achieved: Int)

// Retrofit API service interface
interface SteamApiService {
    @GET("ISteamUserStats/GetPlayerAchievements/v0001/")
    fun getPlayerAchievements(
        @Query("appid") appId: String,
        @Query("key") apiKey: String,
        @Query("steamid") steamId: String
    ): Call<PlayerAchievementsResponse>
}

// Retrofit client singleton
object RetrofitClient {
    private const val BASE_URL = "https://api.steampowered.com/"

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var apiService: SteamApiService
    private val apiKey = "1D5828604BC24180E01C8E32B4F297E0" // Substitua pela sua API Key da Steam
    private val appId = "440" // Exemplo de ID do jogo (Team Fortress 2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiService = RetrofitClient.retrofitInstance.create(SteamApiService::class.java)

        val editTextSteamId = findViewById<EditText>(R.id.editTextSteamId)
        val buttonSearch = findViewById<Button>(R.id.buttonSearch)
        val textViewResults = findViewById<TextView>(R.id.textViewResults)

        buttonSearch.setOnClickListener {
            val steamId = editTextSteamId.text.toString()
            if (steamId.isNotEmpty()) {
                getAchievements(steamId, textViewResults)
            } else {
                textViewResults.text = "Por favor, insira um Steam ID."
            }
        }
    }

    private fun getAchievements(steamId: String, textViewResults: TextView) {
        apiService.getPlayerAchievements(appId, apiKey, steamId).enqueue(object : Callback<PlayerAchievementsResponse> {
            override fun onResponse(call: Call<PlayerAchievementsResponse>, response: Response<PlayerAchievementsResponse>) {
                if (response.isSuccessful) {
                    val achievements = response.body()?.playerstats?.achievements
                    if (!achievements.isNullOrEmpty()) {
                        val resultText = achievements.joinToString("\n") { "${it.apiname}: ${if (it.achieved == 1) "Achieved" else "Not Achieved"}" }
                        textViewResults.text = resultText
                    } else {
                        textViewResults.text = "No achievements found or error retrieving data."
                    }
                } else {
                    textViewResults.text = "Erro ao buscar as conquistas. Tente novamente."
                }
            }

            override fun onFailure(call: Call<PlayerAchievementsResponse>, t: Throwable) {
                textViewResults.text = "Falha na solicitação: ${t.message}"
            }
        })
    }
}

class R {

}
