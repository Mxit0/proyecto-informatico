-- Migration: agregar columna id_componente_maestro a tabla producto
-- Ejecutar en la base de datos (p. ej. psql o desde Supabase SQL editor)

ALTER TABLE producto
ADD COLUMN IF NOT EXISTS id_componente_maestro uuid;

-- Si existe la tabla componente_maestro con columna id (uuid), agregamos la FK
ALTER TABLE producto
ADD CONSTRAINT IF NOT EXISTS fk_producto_componente
FOREIGN KEY (id_componente_maestro) REFERENCES componente_maestro(id);

-- Nota: la columna se crea nullable para mantener compatibilidad con registros existentes.
