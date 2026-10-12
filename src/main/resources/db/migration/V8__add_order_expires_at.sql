-- Flyway Migration V8__add_order_expires_at.sql
-- Thêm cột expires_at vào bảng orders để quản lý cơ chế giữ vé tạm thời (ticket reservation hold)
-- Tạo index tối ưu cho scheduler quét đơn hết hạn cần hoàn lại vé

ALTER TABLE orders ADD COLUMN IF NOT EXISTS expires_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_orders_expires_at ON orders(expires_at, order_status, payment_status);
