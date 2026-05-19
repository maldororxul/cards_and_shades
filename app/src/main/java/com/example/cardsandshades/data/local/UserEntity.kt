package com.example.cardsandshades.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.cardsandshades.model.CardModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "user_profile")
data class UserEntity(
    @PrimaryKey val id: Int = 1, // У нас всегда одна строка профиля
    val gold: Int,
    val maxUnlockedLevel: Int,
    val collectionJson: String,
    val selectedDeckJson: String
)

class CardConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromCardList(value: List<CardModel>): String = gson.toJson(value)

    @TypeConverter
    fun toCardList(value: String): List<CardModel> {
        val listType = object : TypeToken<List<CardModel>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }
}
