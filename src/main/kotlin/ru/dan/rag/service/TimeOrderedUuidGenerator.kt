package ru.dan.rag.service

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.NoArgGenerator
import java.time.Instant
import java.util.*
import org.springframework.stereotype.Component

@Component
class TimeOrderedUuidGenerator {

    private val generator: NoArgGenerator = Generators.timeBasedGenerator()

    /**
     * Генерация UUIDv7
      */
    fun generateUUID(): UUID {
        return generator.generate()
    }

    /**
     * Извлекает timestamp из UUID v7
     * @return Instant или null если не UUID v7
     */
    fun extractTimestamp(uuid: UUID): Instant? {
        return try {
            val mostSigBits = uuid.mostSignificantBits
            val timestamp = (mostSigBits shr 16) and 0xFFFFFFFFFFFFL
            Instant.ofEpochMilli(timestamp)
        } catch (e: Exception) {
            null
        }
    }
}