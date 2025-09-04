-- Populates global_configuration table
INSERT IGNORE INTO global_configuration (id, config_name, config_key, config_value, config_type, created_at, updated_at, created_by, updated_by) VALUES
    (1, 'Email Confirmation token expiry minutes', 'confirmation_token_expiry_minutes', 60, 'INT', NOW(), NOW(), 'system', 'system'),
    (2, 'Email', 'email', 'academy.project.do.not.reply@gmail.com', 'STRING', NOW(), NOW(), 'system', 'system'),
    (3, 'Password for email', 'password', 'hehixeicdqmkvjjo', 'PASSWORD', NOW(), NOW(), 'system', 'system'),
    (4, 'Password reset token expiry minutes', 'password_reset_token_expiry_minutes', 20, 'INT', NOW(), NOW(), 'system', 'system'),
    (5, 'Time to Confirm Appointment in minutes', 'confirm_appointment_expiry_minutes', 15, 'INT', NOW(), NOW(), 'system', 'system'),
    (6, 'Maximum of valid token-related requests at the same time', 'maximum_valid_tokens', 3, 'INT', NOW(), NOW(), 'system', 'system'),
    (7, 'Minimum days before cancellation in order to not pay', 'minimum_days_before_cancellation_to_not_pay', 3, 'INT', NOW(), NOW(), 'system', 'system'),
    (8, 'Account Deletion Expiry (days)', 'account_deletion_expiry_days', 30, 'INT', NOW(), NOW(), 'system', 'system'),
    (9, 'Number of Account Deletion tokens creatable per day', 'account_deletion_daily_tokens', 5, 'INT', NOW(), NOW(), 'system', 'system');