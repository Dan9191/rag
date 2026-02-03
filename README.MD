# RAG service

## Описание
RAG сервис.

## Требования:

### 1. LLM
Для работы сервиса требуется Ollama

Инструкция по установке для Windows
1. Скачать с https://ollama.com/download

2. Проверить в Windows Terminal:
```ollama --version```
3. Установить модель
```ollama pull qwen2.5:3b```
4. Запустить и проверить
```ollama run qwen2.5:3b```

### 2. Vectorization
Для векторизации данных и запроса используется сервис https://github.com/rag-wtf/open-text-embeddings