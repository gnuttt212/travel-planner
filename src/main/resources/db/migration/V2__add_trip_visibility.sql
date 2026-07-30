-- Thêm cột visibility vào bảng trips.
-- Default PRIVATE đảm bảo backward-compatible: các trip cũ vẫn giữ private.
ALTER TABLE trips ADD COLUMN visibility VARCHAR(20) DEFAULT 'PRIVATE' NOT NULL;
