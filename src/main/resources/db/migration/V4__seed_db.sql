INSERT INTO franchise (id, name) VALUES
    (1, 'Franquicia Norte'),
    (2, 'Franquicia Sur')
ON CONFLICT (id) DO NOTHING;

INSERT INTO branch (id, name, address, franchise_id) VALUES
    (1, 'Sucursal Centro', 'Av. Principal 123, Ciudad Central', 1),
    (2, 'Sucursal Norte', 'Calle 45 #10, Ciudad Norte', 1),
    (3, 'Sucursal Sur', 'Av. Costera 55, Ciudad Sur', 2)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, name, description, stock, sku, branch_id) VALUES
    (1, 'Combo Básico', 'Paquete estándar para clientes nuevos', 50, 1001, 1),
    (2, 'Combo Premium', 'Versión ampliada del combo básico', 30, 1002, 1),
    (3, 'Bebida Especial', 'Bebida exclusiva de temporada', 75, 1003, 1),
    (4, 'Snack Saludable', 'Opción baja en calorías', 60, 1004, 1),
    (5, 'Postre Clásico', 'Receta tradicional de la franquicia', 40, 1005, 1),
    (6, 'Combo Familiar', 'Porción ideal para familias', 45, 2001, 2),
    (7, 'Promo Local', 'Producto destacado de la sucursal norte', 35, 2002, 2),
    (8, 'Bebida Citrus', 'Refresco cítrico artesanal', 70, 2003, 2),
    (9, 'Snack Energético', 'Bocadillo con granos y frutos secos', 55, 2004, 2),
    (10, 'Postre Frutal', 'Postre frío con frutas tropicales', 25, 2005, 2),
    (11, 'Combo Playero', 'Menú fresco para clima cálido', 65, 3001, 3),
    (12, 'Promo Costera', 'Especialidad de mariscos', 28, 3002, 3),
    (13, 'Bebida Tropical', 'Mezcla de frutas exóticas', 80, 3003, 3),
    (14, 'Snack Veggie', 'Opción vegana con vegetales orgánicos', 50, 3004, 3),
    (15, 'Postre Helado', 'Helado artesanal con toppings', 35, 3005, 3)
ON CONFLICT (id) DO NOTHING;

SELECT setval('franchise_id_seq', (SELECT COALESCE(MAX(id), 1) FROM franchise));
SELECT setval('branch_id_seq', (SELECT COALESCE(MAX(id), 1) FROM branch));
SELECT setval('product_id_seq', (SELECT COALESCE(MAX(id), 1) FROM product));

