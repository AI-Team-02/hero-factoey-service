-- Enable pgvector extension first
CREATE EXTENSION IF NOT EXISTS vector;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS prompt_logs CASCADE;
DROP TABLE IF EXISTS message_logs CASCADE;
DROP TABLE IF EXISTS payment_logs CASCADE;
DROP TABLE IF EXISTS payments CASCADE;
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

-- Create payments table
CREATE TABLE payments (
                          id BIGSERIAL PRIMARY KEY,
                          payment_id VARCHAR(36) UNIQUE NOT NULL,
                          order_id VARCHAR(255) NOT NULL,
                          tid VARCHAR(255) NOT NULL,
                          shop_item_id BIGINT NOT NULL,
                          member_id VARCHAR(255) NOT NULL,
                          amount BIGINT NOT NULL,
                          item_name VARCHAR(255) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          created_at TIMESTAMP NOT NULL,
                          updated_at TIMESTAMP,
                          approved_at TIMESTAMP,
                          canceled_at TIMESTAMP,
                          cancel_amount BIGINT,
                          cancel_reason VARCHAR(255),
                          payment_key VARCHAR(100),
                          error_message VARCHAR(500)
);

-- Create payment_logs table
CREATE TABLE payment_logs (
                              id BIGSERIAL PRIMARY KEY,
                              payment_id VARCHAR(36) NOT NULL,
                              log_type VARCHAR(50) NOT NULL,
                              content TEXT NOT NULL,
                              created_at TIMESTAMP NOT NULL,
                              FOREIGN KEY (payment_id) REFERENCES payments(payment_id) ON DELETE CASCADE
);

-- Create prompt_logs table
CREATE TABLE prompt_logs (
                             id BIGSERIAL PRIMARY KEY,
                             prompt_id VARCHAR(255),
                             log_type VARCHAR(50) NOT NULL,
                             content TEXT,
                             created_at TIMESTAMP NOT NULL,
                             FOREIGN KEY (prompt_id) REFERENCES prompts(prompt_id) ON DELETE CASCADE
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
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_prompts_prompt_id ON prompts(prompt_id);
CREATE INDEX idx_prompt_logs_prompt_id ON prompt_logs(prompt_id);
CREATE INDEX idx_message_logs_message_id ON message_logs(message_id);
CREATE INDEX idx_message_status ON message_logs(message_id, status);
CREATE INDEX idx_payments_payment_id ON payments(payment_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_member_id ON payments(member_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payment_logs_payment_id_created_at ON payment_logs(payment_id, created_at);

-- Create vector similarity search index
CREATE INDEX prompt_vector_idx ON prompts
    USING ivfflat (embedding_vector vector_l2_ops)
WITH (lists = 100);