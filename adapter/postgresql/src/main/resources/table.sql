-- Category 테이블 생성
CREATE TABLE category (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(255)
);

-- Post 테이블 생성
CREATE TABLE post (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255),
                      content TEXT,
                      user_id BIGINT,
                      category_id BIGINT,
                      created_at TIMESTAMP,
                      updated_at TIMESTAMP,
                      deleted_at TIMESTAMP,
                      FOREIGN KEY (category_id) REFERENCES category(id)
);

