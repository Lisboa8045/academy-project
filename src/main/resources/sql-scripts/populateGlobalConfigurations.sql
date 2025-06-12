-- Populates role table
INSERT IGNORE INTO `global_configuration`
(id, config_key, config_value, config_type, created_at, updated_at)
values
    (1, 'confirmation_token_expiry_minutes', 60)
    (2, 'email', 'academy.project.do.not.reply@gmail.com')
    (3, 'password', 'hehixeicdqmkvjjo')
