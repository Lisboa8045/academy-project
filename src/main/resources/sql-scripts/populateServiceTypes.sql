-- Insert Service Types
INSERT IGNORE INTO service_type (id, name, icon, created_at, updated_at, created_by, updated_by) VALUES
                                                                (1, 'Beauty', 'default.png', NOW(), NOW(), 'system', 'system'),
                                                                (2, 'Fitness', 'default.png', NOW(), NOW(), 'system', 'system'),
                                                                (3, 'Education', 'default.png', NOW(), NOW(), 'system', 'system'),
                                                                (4, 'Home Services', 'default.png', NOW(), NOW(), 'system', 'system'),
                                                                (5, 'Wellness', 'default.png', NOW(), NOW(), 'system', 'system');