-- Populates role table
INSERT IGNORE INTO `role`
(id, name, updated_at, created_at, updated_by, created_by)
values
    (1, 'ADMIN', NOW(), NOW(), 'system', 'system'),
    (2, 'CLIENT', NOW(), NOW(), 'system', 'system'),
    (3, 'WORKER', NOW(), NOW(), 'system', 'system');