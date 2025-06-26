INSERT IGNORE INTO `member`
(id, username, password, email, address, postal_code, phone_number, created_at, updated_at, role_id, enabled, created_by, updated_by)
values
    (1, 'admin', 'Admin123@', 'admin@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 1, false, 'system', 'system'),
    (2, 'client', 'Client123@', 'client@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 2, false, 'system', 'system'),
    (3, 'worker', 'Worker123@', 'worker@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 3, false, 'system', 'system');
