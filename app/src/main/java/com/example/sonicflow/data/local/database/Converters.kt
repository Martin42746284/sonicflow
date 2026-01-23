package com.example.sonicflow.data.local.database

import androidx.room.TypeConverter

/**
 * Convertisseurs de types pour Room (version simple)
 */
class Converters {

    /**
     * Convertit une List<String> en String
     */
    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.joinToString(separator = ",")
    }

    /**
     * Convertit un String en List<String>
     */
    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.split(",")?.filter { it.isNotBlank() }
    }

    /**
     * Convertit une List<Long> en String
     */
    @TypeConverter
    fun fromLongList(value: List<Long>?): String? {
        return value?.joinToString(separator = ",")
    }

    /**
     * Convertit un String en List<Long>
     */
    @TypeConverter
    fun toLongList(value: String?): List<Long>? {
        return value?.split(",")
            ?.filter { it.isNotBlank() }
            ?.mapNotNull { it.toLongOrNull() }
    }
}