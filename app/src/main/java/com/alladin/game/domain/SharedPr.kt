package com.alladin.game.domain

import android.content.Context

class SharedPr(private val context: Context) {
    private val sp = context.getSharedPreferences("SP", Context.MODE_PRIVATE)

    fun getCoins(): Int = sp.getInt("COINS", 0)
    fun setCoins(coins: Int) = sp.edit().putInt("COINS", getCoins() + coins).apply()

    fun getRecord(): Int = sp.getInt("RECORD", 0)
    fun setRecord(record: Int) = sp.edit().putInt("RECORD", record).apply()

    fun getCurrentCharacter(): Int = sp.getInt("CHARACTER", 1)
    fun setCurrentCharacter(character: Int) = sp.edit().putInt("CHARACTER", character).apply()

    fun getCurrentLamp(): Int = sp.getInt("LAMP", 1)
    fun setCurrentLamp(lamp: Int) = sp.edit().putInt("LAMP", lamp).apply()

    fun isLampBought(lamp: Int): Boolean = sp.getBoolean("LAMP$lamp", false)
    fun buyLamp(lamp: Int) = sp.edit().putBoolean("LAMP$lamp", true).apply()
}