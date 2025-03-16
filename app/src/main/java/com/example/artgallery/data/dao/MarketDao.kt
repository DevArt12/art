
package com.example.artgallery.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.artgallery.data.entity.MarketItem

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_items WHERE isAvailable = 1")
    fun getAvailableItems(): LiveData<List<MarketItem>>

    @Query("SELECT * FROM market_items WHERE artworkId = :artworkId")
    suspend fun getMarketItemByArtworkId(artworkId: Long): MarketItem?

    @Insert
    suspend fun insertMarketItem(item: MarketItem): Long

    @Update
    suspend fun updateMarketItem(item: MarketItem)

    @Delete
    suspend fun deleteMarketItem(item: MarketItem)
}
