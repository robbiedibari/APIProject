package com.example.apiproject

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.codepath.asynchttpclient.AsyncHttpClient
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.button)
        val imageView = findViewById<ImageView>(R.id.pokeImage)

        button.setOnClickListener {
            fetchRandomPokemonImage(imageView)
        }
    }

    private fun fetchRandomPokemonImage(imageView: ImageView) {
        val client = AsyncHttpClient()

        client["https://pokeapi.co/api/v2/pokemon?limit=100000&offset=0", object : JsonHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON?) {
                // Choose a random Pokémon from the list
                val results = json?.jsonObject?.getJSONArray("results")
                val randomIndex = Random.nextInt(results?.length() ?: 0)
                val pokemonUrl = results?.getJSONObject(randomIndex)?.getString("url")

                // Fetch the details of the chosen Pokémon
                client[pokemonUrl, object : JsonHttpResponseHandler() {
                    override fun onSuccess(statusCode: Int, headers: Headers, json: JSON?) {
                        val imageUrl = json?.jsonObject?.getJSONObject("sprites")?.getString("front_default")
                        val pokemonName = json?.jsonObject?.getString("name")

                        // Get types
                        val typesArray = json?.jsonObject?.getJSONArray("types")
                        val types = mutableListOf<String>()
                        for (i in 0 until (typesArray?.length() ?: 0)) {
                            types.add(typesArray?.getJSONObject(i)?.getJSONObject("type")?.getString("name").orEmpty())
                        }

                        // Get weight
                        val weight = json?.jsonObject?.getInt("weight")

                        // Load image using Glide
                        Glide.with(this@MainActivity)
                            .load(imageUrl)
                            .fitCenter()
                            .into(imageView)

                        // Display name, types, and weight
                        findViewById<TextView>(R.id.tvPokemonName).text = pokemonName?.capitalize()
                        findViewById<TextView>(R.id.tvPokemonTypes).text = "Types: ${types.joinToString(", ")}"
                        findViewById<TextView>(R.id.tvPokemonWeight).text = "Weight: $weight kg"  // Assuming the weight is in kilograms
                    }

                    override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String?, throwable: Throwable?) {
                        Log.e("APIResponse", "Failure fetching Pokémon details: $errorResponse")
                    }
                }]
            }

            override fun onFailure(statusCode: Int, headers: Headers?, errorResponse: String?, throwable: Throwable?) {
                Log.e("APIResponse", "Failure fetching Pokémon list: $errorResponse")
            }
        }]
    }
}
