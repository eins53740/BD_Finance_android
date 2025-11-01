package com.example.bd_finance.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Create watchlist table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS watchlist (
                    ticker TEXT PRIMARY KEY NOT NULL,
                    companyName TEXT,
                    addedDate INTEGER NOT NULL,
                    lastPrice REAL,
                    lastPriceChange REAL,
                    lastRecommendation TEXT,
                    lastUpdated INTEGER
                )
            """.trimIndent())

            // Create portfolio table
            database.execSQL("""
                CREATE TABLE IF NOT EXISTS portfolio (
                    id TEXT PRIMARY KEY NOT NULL,
                    ticker TEXT NOT NULL,
                    companyName TEXT,
                    quantity REAL NOT NULL,
                    purchasePrice REAL NOT NULL,
                    purchaseDate INTEGER NOT NULL,
                    notes TEXT,
                    lastPrice REAL,
                    lastRecommendation TEXT,
                    lastUpdated INTEGER,
                    currency TEXT NOT NULL DEFAULT 'USD'
                )
            """.trimIndent())

            // Create index on portfolio ticker for faster queries
            database.execSQL("CREATE INDEX IF NOT EXISTS index_portfolio_ticker ON portfolio(ticker)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_3_4)
}
