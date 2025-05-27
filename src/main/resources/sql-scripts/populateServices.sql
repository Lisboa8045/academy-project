-- noinspection SqlNoDataSourceInspectionForFile

-- Insert Service Types
INSERT IGNORE INTO service_type (id, name, created_at, updated_at) VALUES
                                                                (1, 'Beauty', NOW(), NOW()),
                                                                (2, 'Fitness', NOW(), NOW()),
                                                                (3, 'Education', NOW(), NOW()),
                                                                (4, 'Home Services', NOW(), NOW()),
                                                                (5, 'Wellness', NOW(), NOW());

-- Insert Tags
INSERT IGNORE INTO tag (id, name, is_custom, created_at, updated_at) VALUES
                                                                  (1, 'salon', 0, NOW(), NOW()),
                                                                  (2, 'pedicure', 0, NOW(), NOW()),
                                                                  (3, 'manicure', 0, NOW(), NOW()),
                                                                  (4, 'skincare', 0, NOW(), NOW()),
                                                                  (5, 'yoga', 0, NOW(), NOW()),
                                                                  (6, 'massage', 0, NOW(), NOW()),
                                                                  (7, 'cleaning', 0, NOW(), NOW()),
                                                                  (8, 'gardening', 0, NOW(), NOW()),
                                                                  (9, 'tutoring', 0, NOW(), NOW()),
                                                                  (10, 'math', 0, NOW(), NOW()),
                                                                  (11, 'english', 0, NOW(), NOW()),
                                                                  (12, 'therapy', 0, NOW(), NOW()),
                                                                  (13, 'nutrition', 0, NOW(), NOW()),
                                                                  (14, 'spa', 0, NOW(), NOW()),
                                                                  (15, 'personal training', 0, NOW(), NOW());

-- Insert Services
INSERT IGNORE INTO service (id, name, description, price, discount, negotiable, duration, service_type_id, created_at, updated_at) VALUES
    (1, 'Math & Tutoring Assistance', 'Supportive math and tutoring sessions designed to help students succeed.', 354.12, 15, 0, 120, 3, NOW(), NOW()),
    (2, 'Academic Coaching with Extras', 'Personalized tutoring and wellness support with spa-like relaxation.', 342.43, 11, 0, 120, 3, NOW(), NOW()),
    (3, 'Salon & Spa Experience', 'Rejuvenating salon treatments paired with a luxurious spa ambiance.', 222.91, 10, 0, 120, 1, NOW(), NOW()),
    (4, 'Gardening & Wellness Support', 'Revitalize your garden and your spirit with eco-friendly skincare and nutrition services.', 384.98, 2, 0, 90, 4, NOW(), NOW()),
    (5, 'Nail & Spa Combo', 'Complete manicure, pedicure, and spa treatments to refresh your look.', 121.15, 5, 1, 60, 1, NOW(), NOW()),
    (6, 'Language & Therapy Support', 'Comprehensive English tutoring with therapeutic relaxation sessions.', 399.17, 20, 0, 120, 3, NOW(), NOW()),
    (7, 'Holistic Wellness Coaching', 'Integrated nutrition, therapy, and spa experiences tailored for well-being.', 299.77, 25, 0, 90, 5, NOW(), NOW()),
    (8, 'Yoga & Massage Studio', 'Relaxing yoga classes paired with rejuvenating massage sessions.', 286.21, 14, 1, 90, 2, NOW(), NOW()),
    (9, 'Therapeutic Manicure Package', 'Restore your hands and health with soothing therapy and manicure care.', 408.69, 25, 1, 45, 1, NOW(), NOW()),
    (10, 'Home Cleaning & Math Coaching', 'Academic tutoring with the bonus of a tidier, more focused environment.', 192.44, 15, 0, 30, 4, NOW(), NOW()),
    (11, 'Yoga & Relaxation Hub', 'Stretch and unwind with yoga, massage, and calming therapy.', 152.41, 30, 1, 30, 5, NOW(), NOW()),
    (12, 'Massage & Home Wellness', 'Enjoy therapeutic massages and personalized home-based wellness services.', 357.93, 15, 1, 30, 5, NOW(), NOW()),
    (13, 'Spa & Skincare Package', 'Luxurious skincare and spa service for full-body rejuvenation.', 219.79, 3, 1, 30, 1, NOW(), NOW()),
    (14, 'Home Therapy & English Support', 'Engaging English lessons with therapeutic care for the whole family.', 415.1, 24, 1, 30, 3, NOW(), NOW()),
    (15, 'Tutoring & Nail Care Service', 'Academic excellence meets beauty with tutoring and manicure sessions.', 295.88, 29, 1, 120, 3, NOW(), NOW()),
    (16, 'Academic Wellness Retreat', 'Support students with tutoring, mental clarity, and skincare.', 373.42, 12, 1, 60, 5, NOW(), NOW()),
    (17, 'Study & Relax Center', 'Focused learning with integrated wellness practices.', 498.61, 12, 0, 30, 3, NOW(), NOW()),
    (18, 'Holistic Nutrition & Wellness', 'Balanced approach to therapy and nutrition in a peaceful setting.', 186.91, 12, 0, 120, 5, NOW(), NOW()),
    (19, 'Salon & Therapy Spa', 'Classic salon services combined with mental wellness support.', 189.77, 8, 0, 120, 1, NOW(), NOW()),
    (20, 'Academic Boost Program', 'Effective tutoring and support in a calm, salon-inspired atmosphere.', 442.23, 29, 1, 120, 3, NOW(), NOW());



-- Insert Service-Tag Relations
INSERT IGNORE INTO service_tag (service_id, tag_id) VALUES
                                                 (1, 2), (1, 10), (1, 9),
                                                 (2, 9), (2, 10), (2, 8), (2, 14),
                                                 (3, 14), (3, 1), (3, 7),
                                                 (4, 8), (4, 13), (4, 4),
                                                 (5, 14), (5, 10), (5, 2),
                                                 (6, 11), (6, 12), (6, 4),
                                                 (7, 11), (7, 13), (7, 14),
                                                 (8, 6), (8, 5),
                                                 (9, 3), (9, 13),
                                                 (10, 7), (10, 10), (10, 3),
                                                 (11, 5), (11, 6), (11, 13), (11, 8),
                                                 (12, 6), (12, 7), (12, 5),
                                                 (13, 10), (13, 6), (13, 1),
                                                 (14, 7), (14, 12), (14, 11),
                                                 (15, 9), (15, 3), (15, 8),
                                                 (16, 10), (16, 6), (16, 11),
                                                 (17, 10), (17, 9), (17, 6),
                                                 (18, 12), (18, 10), (18, 13), (18, 14),
                                                 (19, 1), (19, 12), (19, 4), (19, 11),
                                                 (20, 9), (20, 1), (20, 4);