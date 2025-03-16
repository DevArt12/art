package com.example.artgallery.data.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "artists")
data class Artist(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bio: String,
    val profilePicturePath: String
) : Parcelable
