CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS articles (
                          id UUID PRIMARY KEY,
                          external_article_id VARCHAR(255) NOT NULL,
                          title TEXT NOT NULL,
                          original_content TEXT NOT NULL,
                          metadata JSONB,
                          created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                          updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                          CONSTRAINT unique_external_article_id UNIQUE (external_article_id)
);

COMMENT ON TABLE articles IS 'Статьи';
COMMENT ON COLUMN articles.id IS 'Внутренний уникальный идентификатор статьи (UUID v7 - time-ordered)';
COMMENT ON COLUMN articles.external_article_id IS 'Внешний идентификатор статьи из главного сервиса';
COMMENT ON COLUMN articles.title IS 'Заголовок статьи';
COMMENT ON COLUMN articles.metadata IS 'Дополнительные метаданные статьи';
COMMENT ON COLUMN articles.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN articles.updated_at IS 'Дата и время последнего обновления записи';

CREATE INDEX idx_articles_external_id ON articles(external_article_id);
COMMENT ON INDEX idx_articles_external_id IS 'Индекс для быстрого поиска статей по внешнему идентификатору';

CREATE TABLE IF NOT EXISTS article_chunks (
                                id UUID PRIMARY KEY,
                                article_id UUID NOT NULL REFERENCES articles(id) ON DELETE CASCADE,
                                chunk_index INTEGER NOT NULL,
                                text_for_search TEXT NOT NULL,
                                embedding VECTOR(1024),
                                processing_status VARCHAR(20) DEFAULT 'PENDING',
                                processing_attempts INTEGER DEFAULT 0,
                                last_attempt_at TIMESTAMP WITH TIME ZONE,
                                processed_at TIMESTAMP WITH TIME ZONE,
                                chunk_metadata JSONB,
                                created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
                                updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),

                                CONSTRAINT unique_chunk_per_article UNIQUE (article_id, chunk_index)
);

COMMENT ON TABLE article_chunks IS 'Чанки статей для семантического поиска и RAG';
COMMENT ON COLUMN article_chunks.id IS 'Уникальный идентификатор чанка (UUID v7)';
COMMENT ON COLUMN article_chunks.article_id IS 'Ссылка на статью, к которой принадлежит чанк';
COMMENT ON COLUMN article_chunks.chunk_index IS 'Порядковый номер чанка в статье (0-based)';
COMMENT ON COLUMN article_chunks.text_for_search IS 'Очищенный текст для векторизации и семантического поиска';
COMMENT ON COLUMN article_chunks.embedding IS 'Векторное представление текста (1024 измерения для модели e5-large-v2)';
COMMENT ON COLUMN article_chunks.processing_status IS 'Статус обработки: PENDING, PROCESSING, COMPLETED, FAILED';
COMMENT ON COLUMN article_chunks.processing_attempts IS 'Количество попыток векторизации чанка';
COMMENT ON COLUMN article_chunks.last_attempt_at IS 'Время последней попытки векторизации (для exponential backoff)';
COMMENT ON COLUMN article_chunks.processed_at IS 'Время успешной обработки чанка (когда получен embedding)';
COMMENT ON COLUMN article_chunks.chunk_metadata IS 'Метаданные чанка: типы элементов, размер текста, статистика';
COMMENT ON COLUMN article_chunks.created_at IS 'Дата и время создания записи';
COMMENT ON COLUMN article_chunks.updated_at IS 'Дата и время последнего обновления записи';