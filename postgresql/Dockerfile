FROM postgres:latest

# pgvector 설치에 필요한 빌드 도구 설치
RUN apt-get update \
    && apt-get install -y \
        build-essential \
        git \
        postgresql-server-dev-all \
    && rm -rf /var/lib/apt/lists/*

# pgvector 다운로드 및 설치
RUN git clone --branch v0.5.1 https://github.com/pgvector/pgvector.git \
    && cd pgvector \
    && make \
    && make install

# 설치 후 불필요한 패키지 제거
RUN apt-get remove -y \
        build-essential \
        git \
        postgresql-server-dev-all \
    && apt-get autoremove -y