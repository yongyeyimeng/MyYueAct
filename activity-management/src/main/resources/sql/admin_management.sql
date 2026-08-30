-- Admin management schema support.
ALTER TABLE user_blacklist
  ADD COLUMN reason VARCHAR(500) NULL DEFAULT NULL COMMENT 'ban reason' AFTER blacklisted_user_id,
  ADD COLUMN banned_until DATETIME NULL DEFAULT NULL COMMENT 'ban expiry, NULL means permanent' AFTER reason,
  ADD COLUMN updated_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time' AFTER created_at;
