-- V3: Crear tabla 'lotes' y agregar columna lote_id a inventario_movimientos con FK
-- Esta versión evita 'IF NOT EXISTS' en lugares que algunos dialectos no aceptan.

CREATE TABLE IF NOT EXISTS lotes (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     codigo VARCHAR(255) UNIQUE,
                                     created_at TIMESTAMP NULL,
                                     created_by VARCHAR(255)
);

-- Agregamos columna lote_id (si la migración se ejecuta por primera vez, la columna no existirá).
ALTER TABLE inventario_movimientos
    ADD COLUMN lote_id BIGINT;

-- Añadimos la FK (sin IF NOT EXISTS). Si la constraint ya existe, este ALTER fallará;
-- esto está bien en una migración limpia. Si la constraint existe por alguna razón,
-- elimina la constraint previa o ajusta manualmente en la BD.
ALTER TABLE inventario_movimientos
    ADD CONSTRAINT fk_inventario_lote
        FOREIGN KEY (lote_id) REFERENCES lotes(id);