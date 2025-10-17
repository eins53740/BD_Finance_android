package com.example.bd_finance.data.sync

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object StockMetricsMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN sector TEXT")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN sectorSnapshotCompressed TEXT")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN historicalFundamentalsCompressed TEXT")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN metadataTimestamp INTEGER")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN metadataRetries INTEGER NOT NULL DEFAULT 0")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN metadataFallback INTEGER NOT NULL DEFAULT 0")
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN returnOnEquity REAL")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN returnOnAssets REAL")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN operatingMargin REAL")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN netMargin REAL")
            database.execSQL("ALTER TABLE stock_metrics ADD COLUMN debtToEquity REAL")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2, MIGRATION_2_3)
}
