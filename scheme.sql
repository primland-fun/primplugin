CREATE TABLE IF NOT EXISTS players (
    name VARCHAR(16) NOT NULL,
    daily_strike INT,
    `chat.sound` TEXT,
    `chat.lastReceived` VARCHAR(16),
    `chat.flags` INT,
    `admin.spy` INT,
    cards JSON,
    `balance.reputation` INT,
    `balance.donate` INT,
    `reputation.value` INT,
    `reputation.lastAction` BIGINT,
    PRIMARY KEY (name)
);

CREATE TABLE IF NOT EXISTS messages (
    sender VARCHAR(16) NOT NULL,
    receiver VARCHAR(16) NOT NULL,
    content TEXT,
    time BIGINT,
    PRIMARY KEY (receiver)
);

CREATE TABLE IF NOT EXISTS gifts (
    id VARCHAR(32) NOT NULL,
    name VARCHAR(64),
    type TINYINT NOT NULL,
    content JSON NOT NULL,
    receiver VARCHAR(16),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS logs (
    action VARCHAR(64) NOT NULL,
    time BIGINT NOT NULL,
    message TEXT NOT NULL,
    data JSON NOT NULL
);