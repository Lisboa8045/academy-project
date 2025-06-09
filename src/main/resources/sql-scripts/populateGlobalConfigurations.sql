-- Populates role table
INSERT IGNORE INTO `global_configuration`
(id, config_key, config_value, config_type, created_at, updated_at)
values
    (1, 'salada', 'cesar', 'STRING', NOW(), NOW()),
    (2, 'isParvo', 'true', 'BOOLEAN', NOW(), NOW()),
    (3, 'diasDeVida', '45', 'INT', NOW(), NOW());