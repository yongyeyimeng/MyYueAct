CREATE TABLE IF NOT EXISTS draws (
  id INT NOT NULL AUTO_INCREMENT,
  content VARCHAR(500) NOT NULL COMMENT 'draw content',
  people_count INT NOT NULL COMMENT 'participant capacity',
  visibility VARCHAR(20) NOT NULL DEFAULT 'public' COMMENT 'public/private',
  invite_code CHAR(4) NOT NULL COMMENT 'unique 4-digit code',
  creator_id INT NOT NULL COMMENT 'creator user id',
  status VARCHAR(20) NOT NULL DEFAULT 'open' COMMENT 'open/drawn/expired',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME NOT NULL COMMENT 'code valid until',
  drawn_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_draw_invite_code (invite_code),
  KEY idx_draw_creator (creator_id),
  CONSTRAINT fk_draw_creator FOREIGN KEY (creator_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draws table';

CREATE TABLE IF NOT EXISTS draw_items (
  id INT NOT NULL AUTO_INCREMENT,
  draw_id INT NOT NULL,
  name VARCHAR(255) NOT NULL COMMENT 'draw item name',
  count INT NOT NULL COMMENT 'people count for this item',
  PRIMARY KEY (id),
  KEY idx_draw_item_draw (draw_id),
  CONSTRAINT fk_draw_items_draw FOREIGN KEY (draw_id) REFERENCES draws (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draw content items';

CREATE TABLE IF NOT EXISTS draw_participants (
  id INT NOT NULL AUTO_INCREMENT,
  draw_id INT NOT NULL,
  user_id INT NOT NULL,
  result INT NULL COMMENT 'random draw number',
  draw_item_id INT NULL COMMENT 'assigned draw item',
  joined_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_draw_user (draw_id, user_id),
  KEY idx_draw_participant_user (user_id),
  CONSTRAINT fk_draw_participants_draw FOREIGN KEY (draw_id) REFERENCES draws (id) ON DELETE CASCADE,
  CONSTRAINT fk_draw_participants_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_draw_participants_item FOREIGN KEY (draw_item_id) REFERENCES draw_items (id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='draw participants';
