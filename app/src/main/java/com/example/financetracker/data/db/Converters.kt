package com.example.financetracker.data.db

import androidx.room.TypeConverter
import java.time.LocalDate

/** LocalDate se v databázi ukládá jako počet dní od 1. 1. 1970 (dá se tak řadit a filtrovat). */
class Converters {
    @TypeConverter
    fun fromEpochDay(value: Long?): LocalDate? = value?.let(LocalDate::ofEpochDay)

    @TypeConverter
    fun toEpochDay(date: LocalDate?): Long? = date?.toEpochDay()
}
