-- Inserir Tipos de Serviço com Ícones Font Awesome
INSERT IGNORE INTO service_type (id, name, icon, created_at, updated_at, created_by, updated_by) VALUES
    (1, 'Beauty', 'spa', NOW(), NOW(), 'system', 'system'),
    (2, 'Fitness', 'dumbbell', NOW(), NOW(), 'system', 'system'),
    (4, 'Home', 'house', NOW(), NOW(), 'system', 'system'),
    (6, 'Health', 'stethoscope', NOW(), NOW(), 'system', 'system'),
    (7, 'Finance', 'money-bill-wave', NOW(), NOW(), 'system', 'system'),
    (12, 'Cleaning', 'broom', NOW(), NOW(), 'system', 'system'),
    (13, 'Security', 'shield-alt', NOW(), NOW(), 'system', 'system'),
    (17, 'Pet Care', 'paw', NOW(), NOW(), 'system', 'system'),
    (21, 'Marketing', 'bullhorn', NOW(), NOW(), 'system', 'system'),
    (30, 'Repair', 'wrench', NOW(), NOW(), 'system', 'system'),
    (8, 'Legal', 'gavel', NOW(), NOW(), 'system', 'system'),
    (11, 'Education', 'book', NOW(), NOW(), 'system', 'system'),
    (14, 'Photography', 'camera', NOW(), NOW(), 'system', 'system'),
    (18, 'Construction', 'hard-hat', NOW(), NOW(), 'system', 'system'),
    (20, 'Entertainment', 'theater-masks', NOW(), NOW(), 'system', 'system'),
    (23, 'Travel', 'plane', NOW(), NOW(), 'system', 'system');
