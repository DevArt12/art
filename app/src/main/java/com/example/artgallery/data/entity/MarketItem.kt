
package com.example.artgallery.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "market_items",
    foreignKeys = [
        ForeignKey(
            entity = Artwork::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MarketItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val artworkId: Long,
    val price: Double,
    val isAvailable: Boolean = true,
    val description: String,
    val contactInfo: String
)
