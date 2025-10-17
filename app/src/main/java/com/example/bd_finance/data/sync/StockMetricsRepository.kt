package com.example.bd_finance.data.sync

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "stock_metrics")
data class StockMetricsEntity(
    @PrimaryKey val ticker: String,
    val price: Double?,
    val forwardPe: Double?,
    val trailingPe: Double?,
    val pegRatio: Double?,
    val priceToBook: Double?,
    val dividendRate: Double?,
    val epsForward: Double?,
    val bookValue: Double?,
    val beta: Double?,
    val returnOnEquity: Double?,
    val returnOnAssets: Double?,
    val operatingMargin: Double?,
    val netMargin: Double?,
    val debtToEquity: Double?,
    val sector: String?,
    val sectorSnapshotCompressed: String?,
    val historicalFundamentalsCompressed: String?,
    val metadataTimestamp: Long?,
    val metadataRetries: Int,
    val metadataFallback: Boolean
)

@Dao
interface StockMetricsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StockMetricsEntity)

    @Query("SELECT * FROM stock_metrics WHERE ticker = :ticker LIMIT 1")
    fun observe(ticker: String): Flow<StockMetricsEntity?>

    @Query("SELECT * FROM stock_metrics WHERE ticker = :ticker LIMIT 1")
    suspend fun find(ticker: String): StockMetricsEntity?

    @Query("SELECT * FROM stock_metrics")
    fun observeAll(): Flow<List<StockMetricsEntity>>
}

@Database(entities = [StockMetricsEntity::class], version = 3, exportSchema = false)
abstract class StockMetricsDatabase : RoomDatabase() {
    abstract fun stockMetricsDao(): StockMetricsDao
}

interface StockMetricsRepository {
    suspend fun save(metrics: NormalizedStockMetrics)
    suspend fun get(ticker: String): NormalizedStockMetrics?
    fun observe(ticker: String): Flow<NormalizedStockMetrics?>
    fun snapshot(): Flow<Map<String, NormalizedStockMetrics>>
}

class RoomStockMetricsRepository(
    private val dao: StockMetricsDao
) : StockMetricsRepository {
    override suspend fun save(metrics: NormalizedStockMetrics) {
        dao.upsert(metrics.toEntity())
    }

    override suspend fun get(ticker: String): NormalizedStockMetrics? =
        dao.find(ticker)?.toDomain()

    override fun observe(ticker: String): Flow<NormalizedStockMetrics?> =
        dao.observe(ticker).map { it?.toDomain() }

    override fun snapshot(): Flow<Map<String, NormalizedStockMetrics>> =
        dao.observeAll().map { list -> list.associate { it.ticker to it.toDomain() } }
}

private fun NormalizedStockMetrics.toEntity(): StockMetricsEntity = StockMetricsEntity(
    ticker = ticker,
    price = price,
    forwardPe = forwardPe,
    trailingPe = trailingPe,
    pegRatio = pegRatio,
    priceToBook = priceToBook,
    dividendRate = dividendRate,
    epsForward = epsForward,
    bookValue = bookValue,
    beta = beta,
    returnOnEquity = returnOnEquity,
    returnOnAssets = returnOnAssets,
    operatingMargin = operatingMargin,
    netMargin = netMargin,
    debtToEquity = debtToEquity,
    sector = sector,
    sectorSnapshotCompressed = sectorSnapshot.toCompressedJson(),
    historicalFundamentalsCompressed = historicalFundamentals.toCompressedJson(),
    metadataTimestamp = refreshMetadata.lastUpdatedMillis,
    metadataRetries = refreshMetadata.retries,
    metadataFallback = refreshMetadata.usedFallback
)

private fun StockMetricsEntity.toDomain(): NormalizedStockMetrics = NormalizedStockMetrics(
    ticker = ticker,
    price = price,
    forwardPe = forwardPe,
    trailingPe = trailingPe,
    pegRatio = pegRatio,
    priceToBook = priceToBook,
    dividendRate = dividendRate,
    epsForward = epsForward,
    bookValue = bookValue,
    beta = beta,
    returnOnEquity = returnOnEquity,
    returnOnAssets = returnOnAssets,
    operatingMargin = operatingMargin,
    netMargin = netMargin,
    debtToEquity = debtToEquity,
    sector = sector,
    sectorSnapshot = sectorSnapshotCompressed.toSectorMedianSnapshot(),
    historicalFundamentals = historicalFundamentalsCompressed.toHistoricalFundamentalSnapshot(),
    refreshMetadata = RefreshMetadata(
        lastUpdatedMillis = metadataTimestamp ?: System.currentTimeMillis(),
        retries = metadataRetries,
        usedFallback = metadataFallback
    )
)

