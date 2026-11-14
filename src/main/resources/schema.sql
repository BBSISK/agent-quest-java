-- Agent Quest database schema (R2). The same tables run on Amazon RDS (MySQL 8.4) and, for tests,
-- on an in-memory H2 database in MySQL mode. On RDS they were created once with MySQL Workbench,
-- inside: CREATE DATABASE agent_quest CHARACTER SET utf8mb4;

-- one row per game
CREATE TABLE games (
    id          VARCHAR(36)  PRIMARY KEY,
    model_id    VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- the player's numbers: exactly one row per game; the CHECKs repeat the Java rules
CREATE TABLE player_state (
    game_id      VARCHAR(36) PRIMARY KEY,
    hp           INT NOT NULL CHECK (hp >= 0),
    max_hp       INT NOT NULL CHECK (max_hp > 0),
    strength     INT NOT NULL,
    spell_slots  INT NOT NULL CHECK (spell_slots >= 0),
    gold         INT NOT NULL CHECK (gold >= 0),
    CHECK (hp <= max_hp),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- the inventory: one row per item (first normal form)
CREATE TABLE inventory_items (
    id       INT PRIMARY KEY AUTO_INCREMENT,
    game_id  VARCHAR(36)  NOT NULL,
    item     VARCHAR(100) NOT NULL,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- the story so far
CREATE TABLE turns (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    game_id      VARCHAR(36) NOT NULL,
    turn_no      INT  NOT NULL,
    player_text  TEXT NOT NULL,
    gm_reply     TEXT NOT NULL,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (game_id, turn_no),
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
);

-- every tool the agent ran in a turn: the tool log, kept for good
CREATE TABLE tool_calls (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    turn_id    INT NOT NULL,
    seq        INT NOT NULL,
    tool_name  VARCHAR(50) NOT NULL,
    input      JSON,
    result     TEXT,
    UNIQUE (turn_id, seq),
    FOREIGN KEY (turn_id) REFERENCES turns(id) ON DELETE CASCADE
);
