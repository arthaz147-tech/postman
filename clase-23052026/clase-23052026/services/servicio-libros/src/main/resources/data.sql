-- Datos iniciales del catálogo de libros (Sesiones 17-18 — PostgreSQL)
-- ON CONFLICT DO NOTHING: no falla si los registros ya existen al reiniciar.
INSERT INTO libro (id, titulo, autor, isbn, disponible) VALUES
  ('lib-001', 'Clean Architecture',     'Robert C. Martin', '978-0134494166', true),
  ('lib-002', 'Designing SOA',          'Thomas Erl',       '978-0137135790', true),
  ('lib-003', 'Building Microservices', 'Sam Newman',       '978-1492034025', true),
  ('lib-004', 'Enterprise Integration Patterns', 'Gregor Hohpe', '978-0321200686', true),
  ('lib-005', 'SOA Design Patterns',    'Thomas Erl',       '978-0136135166', true)
ON CONFLICT (id) DO NOTHING;
