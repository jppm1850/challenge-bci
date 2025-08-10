CREATE TABLE IF NOT EXISTS users (
                                     id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP,
    token VARCHAR(2000),
    is_active BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS phones (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      number VARCHAR(15) NOT NULL,
    citycode INTEGER NOT NULL,
    country_code VARCHAR(4) NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_phones_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_phones_user_id ON phones(user_id);