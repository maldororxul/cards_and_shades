package com.example.cardsandshades.model

import android.content.Context
import com.example.cardsandshades.data.local.AppDatabase
import com.example.cardsandshades.data.local.UserEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

object UserProfile {
    val gold = MutableStateFlow(500)
    val collection = MutableListFlow(mutableListOf<CardModel>())
    val selectedDeck = mutableListOf<CardModel>()
    var maxUnlockedLevel = 1

    private var database: AppDatabase? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson = Gson()

    // Инициализация базы данных при старте приложения
    fun initDatabase(context: Context) {
        database = AppDatabase.getDatabase(context)

        // Асинхронно загружаем профиль из Room
        scope.launch {
            val savedUser = database?.userDao()?.getUserProfile()
            if (savedUser != null) {
                gold.value = savedUser.gold
                maxUnlockedLevel = savedUser.maxUnlockedLevel

                val listType = object : com.google.gson.reflect.TypeToken<List<CardModel>>() {}.type
                val loadedCollection: List<CardModel> = gson.fromJson(savedUser.collectionJson, listType) ?: emptyList()
                val loadedDeck: List<CardModel> = gson.fromJson(savedUser.selectedDeckJson, listType) ?: emptyList()

                collection.clear()
                collection.addAll(loadedCollection)
                collection.notifyChanges()

                selectedDeck.clear()
                selectedDeck.addAll(loadedDeck)
            } else {
                // Если база чистая (первый запуск), наполняем коллекцию базовыми картами и сохраняем ее
                repeat(15) {
                    com.example.cardsandshades.catalog.CardCatalog.generateTestDeck().firstOrNull()?.let { collection.add(it) }
                }
                collection.notifyChanges()
                save(context)
            }
        }
    }

    // Метод принудительного сохранения состояния в Room DB
    fun save(context: Context) {
        val db = database ?: AppDatabase.getDatabase(context)
        scope.launch {
            val entity = UserEntity(
                gold = gold.value,
                maxUnlockedLevel = maxUnlockedLevel,
                collectionJson = gson.toJson(collection.toList()),
                selectedDeckJson = gson.toJson(selectedDeck.toList())
            )
            db.userDao().saveUserProfile(entity)
        }
    }
}


// Вспомогательный класс для реактивного обновления списков в Compose
class MutableListFlow<T>(private val delegate: MutableList<T>) : MutableList<T> by delegate {
    private val stateFlow = MutableStateFlow(delegate.toList())
    val status = stateFlow
    fun notifyChanges() {
        stateFlow.value = delegate.toList()
    }
}
