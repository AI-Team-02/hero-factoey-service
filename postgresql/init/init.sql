CREATE TABLE IF NOT EXISTS prompts (
    id SERIAL PRIMARY KEY,
    prompt_id VARCHAR(255) UNIQUE NOT NULL,
    member_id VARCHAR(255),
    original_prompt TEXT,
    improved_prompt TEXT,
    keywords TEXT[],
    category_keywords JSONB,
    embedding_vector DOUBLE PRECISION[],
    status VARCHAR(50),
    error_message TEXT,
    created_at TIMESTAMP,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message_logs (
    id SERIAL PRIMARY KEY,
    message_id VARCHAR(255),
    prompt_id VARCHAR(255),
    payment_id VARCHAR(255),
    status VARCHAR(50),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_prompts_prompt_id ON prompts(prompt_id);
CREATE INDEX IF NOT EXISTS idx_message_logs_message_id ON message_logs(message_id);
