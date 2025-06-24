-- Populates member table with one of every role, address/postal-code/phone are all equal
-- Assumes that role table was initialized using populateRoles.sql script
INSERT IGNORE INTO `member`
(id, username, password, email, address, postal_code, phone_number, created_at, updated_at, role_id, enabled)
values
    (1, 'admin', 'Admin123@', 'admin@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 1, false),
    (2, 'client', 'Client123@', 'client@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 2, false),
    (3, 'worker', 'Worker123@', 'worker@example.com',
     'Avenue', '12345', '967843234',
     NOW(), NOW(), 3, false);