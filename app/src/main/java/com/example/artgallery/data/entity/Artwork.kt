package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.Locale

@Parcelize
@Entity(
    tableName = "artworks",
    foreignKeys = [
        ForeignKey(
            entity = Artist::class,
            parentColumns = ["id"],
            childColumns = ["artistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("artistId")]
)
data class Artwork(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String,
    val imagePath: String,
    val artistId: Long,
    val category: String,
    val price: Double? = null,
    val isForSale: Boolean = false,
    val contactDetails: String? = null
) : Parcelable {
    fun getFormattedPrice(): String {
        if (price == null) return "Not for sale"
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(price)
    }

    companion object {
        const val CATEGORY_PAINTING = "painting"
        const val CATEGORY_SCULPTURE = "sculpture"
        const val CATEGORY_PHOTOGRAPHY = "photography"
        const val CATEGORY_DIGITAL = "digital"
    }
}
