-- Enable pgvector extension first
CREATE EXTENSION IF NOT EXISTS vector;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS message_logs CASCADE;
DROP TABLE IF EXISTS prompts CASCADE;

-- Create prompts table with vector column
CREATE TABLE prompts (
                         id UUID PRIMARY KEY,
                         prompt_id VARCHAR(255) UNIQUE NOT NULL,
                         member_id VARCHAR(255) NOT NULL,
                         original_prompt TEXT NOT NULL,
                         improved_prompt TEXT,
                         keywords JSONB DEFAULT '[]'::jsonb,
                         category_keywords JSONB DEFAULT '[]'::jsonb,
                         embedding_vector vector(1536),
                         status VARCHAR(50) NOT NULL,
                         error_message TEXT,
                         created_at TIMESTAMP NOT NULL,
                         completed_at TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL
);

-- Create message logs table
CREATE TABLE message_logs (
                              id BIGSERIAL PRIMARY KEY,
                              message_id VARCHAR(255) NOT NULL,
                              prompt_id VARCHAR(255),
                              payment_id VARCHAR(255),
                              status VARCHAR(50) NOT NULL,
                              error_message TEXT,
                              retry_count INTEGER DEFAULT 0,
                              created_at TIMESTAMP NOT NULL,
                              updated_at TIMESTAMP NOT NULL
);

-- Create basic indexes
CREATE INDEX idx_prompts_prompt_id ON prompts(prompt_id);
CREATE INDEX idx_message_logs_message_id ON message_logs(message_id);
CREATE INDEX idx_message_status ON message_logs(message_id, status);

-- Create vector similarity search index
CREATE INDEX prompt_vector_idx ON prompts
    USING ivfflat (embedding_vector vector_l2_ops)
WITH (lists = 100);