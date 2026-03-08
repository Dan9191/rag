package ru.dan.rag.service

import com.fasterxml.uuid.Generators
import com.fasterxml.uuid.NoArgGenerator
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
}