package com.example.bd_finance.data

import androidx.room.TypeConverter
import com.example.bd_finance.data.model.StockVerdict
import java.time.Instant

class RoomConverters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun fromStockVerdict(value: StockVerdict?): String? {
        return value?.name
    }

    @TypeConverter
    fun toStockVerdict(value: String?): StockVerdict? {
        return value?.let { StockVerdict.valueOf(it) }
    }
}
