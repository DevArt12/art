package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.Artwork

@Dao
interface ArtworkDao {
    @Query("SELECT * FROM artworks ORDER BY id DESC")
    fun getAllArtworks(): LiveData<List<Artwork>>

    @Query("SELECT * FROM artworks WHERE isForSale = 1 ORDER BY id DESC")
    fun getArtworksForSale(): LiveData<List<Artwork>>

    @Query("SELECT * FROM artworks WHERE artistId = :artistId ORDER BY id DESC")
    fun getArtworksByArtist(artistId: Long): LiveData<List<Artwork>>
    
    @Query("SELECT * FROM artworks ORDER BY id DESC LIMIT :limit OFFSET :offset")
    suspend fun getArtworksByPage(limit: Int, offset: Int): List<Artwork>
    
    @Query("SELECT * FROM artworks WHERE title LIKE :query OR description LIKE :query ORDER BY id DESC")
    suspend fun searchArtworks(query: String): List<Artwork>
    
    @Query("SELECT * FROM artworks WHERE category = :category ORDER BY id DESC")
    suspend fun getArtworksByCategory(category: String): List<Artwork>
    
    @Query("SELECT COUNT(*) FROM artworks")
    suspend fun getArtworkCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtwork(artwork: Artwork): Long

    @Update
    suspend fun updateArtwork(artwork: Artwork)

    @Delete
    suspend fun deleteArtwork(artwork: Artwork)
}
