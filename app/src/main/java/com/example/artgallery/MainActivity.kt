package com.example.artgallery

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.artgallery.data.AppDatabase
import com.example.artgallery.data.entity.ArtNews
import com.example.artgallery.data.entity.Artist
import com.example.artgallery.data.entity.Artwork
import com.example.artgallery.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Correct way to get NavController from NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // Setup bottom navigation with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Add sample data if needed
        addSampleData()
    }

    private fun addSampleData() {
        val db = AppDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if we already have data to avoid duplicates
                val artworkCount = db.artworkDao().getArtworkCount()
                if (artworkCount > 0) {
                    Log.d(TAG, "Sample data already exists, skipping insertion")
                    return@launch
                }
                
                // Add sample artists
                val artists = listOf(
                    Artist(name = "Vincent van Gogh", bio = "Dutch post-impressionist painter", profilePicturePath = "https://example.com/vangogh.jpg"),
                    Artist(name = "Leonardo da Vinci", bio = "Italian Renaissance polymath", profilePicturePath = "https://example.com/davinci.jpg"),
                    Artist(name = "Claude Monet", bio = "French impressionist painter", profilePicturePath = "https://example.com/monet.jpg")
                )
                val artistIds = artists.map { db.artistDao().insertArtist(it) }

                // Add sample artworks with proper error handling for resources
                val artworkTitles = listOf(
                    "Sunset Painting",
                    "Nature Photography",
                    "Modern Sculpture",
                    "Digital Abstract"
                )
                
                val artworkDescriptions = listOf(
                    "A beautiful sunset painting",
                    "Stunning nature photograph",
                    "Contemporary sculpture piece",
                    "Digital art creation"
                )
                
                val artworkCategories = listOf(
                    Artwork.CATEGORY_PAINTING,
                    Artwork.CATEGORY_PHOTOGRAPHY,
                    Artwork.CATEGORY_SCULPTURE,
                    Artwork.CATEGORY_DIGITAL
                )
                
                val artworkPrices = listOf(299.99, 199.99, 599.99, 149.99)
                
                val artworkContacts = listOf(
                    "john@example.com",
                    "jane@example.com",
                    "mike@example.com",
                    "sarah@example.com"
                )
                
                for (i in artworkTitles.indices) {
                    try {
                        val artwork = Artwork(
                            title = artworkTitles[i],
                            description = artworkDescriptions[i],
                            imagePath = "android.resource://${packageName}/${R.drawable.placeholder_image}",
                            artistId = artistIds[0],
                            category = artworkCategories[i],
                            price = artworkPrices[i],
                            isForSale = true,
                            contactDetails = artworkContacts[i]
                        )
                        db.artworkDao().insertArtwork(artwork)
                        Log.d(TAG, "Inserted artwork: ${artworkTitles[i]}")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error inserting artwork ${artworkTitles[i]}: ${e.message}")
                    }
                }

                // Add sample art news
                val news = listOf(
                    ArtNews(
                        title = "New Van Gogh Exhibition",
                        content = "A major exhibition of Van Gogh's work opens next month",
                        imagePath = "https://example.com/exhibition.jpg",
                        date = System.currentTimeMillis()
                    ),
                    ArtNews(
                        title = "Art Market Update",
                        content = "Contemporary art sales reach new heights",
                        imagePath = "https://example.com/market.jpg",
                        date = System.currentTimeMillis() - 86400000 // 1 day ago
                    )
                )
                news.forEach { db.artNewsDao().insertNews(it) }
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Sample data added successfully", Toast.LENGTH_SHORT).show()
                }
                
                Log.d(TAG, "Sample data added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding sample data: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error adding sample data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
