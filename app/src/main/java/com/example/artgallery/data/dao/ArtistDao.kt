package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.Artist

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists")
    fun getAllArtists(): LiveData<List<Artist>>

    @Query("SELECT * FROM artists WHERE id = :id")
    suspend fun getArtistById(id: Long): Artist?

    @Insert
    suspend fun insertArtist(artist: Artist): Long

    @Update
    suspend fun updateArtist(artist: Artist)

    @Delete
    suspend fun deleteArtist(artist: Artist)
}
package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.Artist

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists")
    fun getAllArtists(): LiveData<List<Artist>>

    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: Long): Artist?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: Artist): Long

    @Update
    suspend fun updateArtist(artist: Artist)

    @Delete
    suspend fun deleteArtist(artist: Artist)

    @Query("SELECT * FROM artists WHERE name LIKE :searchQuery")
    suspend fun searchArtists(searchQuery: String): List<Artist>
}
