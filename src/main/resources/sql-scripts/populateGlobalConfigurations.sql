-- Populates global_configuration table
INSERT IGNORE INTO global_configuration (id, config_key, config_value, config_type, created_at, updated_at, created_by, updated_by) VALUES
    (1, 'confirmation_token_expiry_minutes', 60, 'INT', NOW(), NOW(), 'system', 'system'),
    (2, 'email', 'academy.project.do.not.reply@gmail.com', 'STRING', NOW(), NOW(), 'system', 'system'),
    (3, 'password', 'hehixeicdqmkvjjo', 'STRING', NOW(), NOW(), 'system', 'system'),
    (4, 'password_reset_token_expiry_minutes', 20, 'INT', NOW(), NOW(), 'system', 'system'),
    (5, 'confirm_appointment_expiry_minutes', 15, 'INT', NOW(), NOW(), 'system', 'system');

