-- noinspection SqlNoDataSourceInspectionForFile

-- Insert Service Types
INSERT INTO service_type (id, name, created_at, updated_at) VALUES (1, 'Beauty', NOW(), NOW());
INSERT INTO service_type (id, name, created_at, updated_at) VALUES (2, 'Fitness', NOW(), NOW());
INSERT INTO service_type (id, name, created_at, updated_at) VALUES (3, 'Education', NOW(), NOW());
INSERT INTO service_type (id, name, created_at, updated_at) VALUES (4, 'Home Services', NOW(), NOW());
INSERT INTO service_type (id, name, created_at, updated_at) VALUES (5, 'Wellness', NOW(), NOW());

-- Insert Tags
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (1, 'salon', 1, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (2, 'pedicure', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (3, 'manicure', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (4, 'skincare', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (5, 'yoga', 1, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (6, 'massage', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (7, 'cleaning', 1, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (8, 'gardening', 1, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (9, 'tutoring', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (10, 'math', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (11, 'english', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (12, 'therapy', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (13, 'nutrition', 0, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (14, 'spa', 1, NOW(), NOW());
INSERT INTO tag (id, name, is_custom, created_at, updated_at) VALUES (15, 'personal training', 1, NOW(), NOW());

-- Insert Services
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (1, 'Gentry-Anderson Service', 'Though couple early reason too player performance generation pressure event wonder century.', 354.12, 15, 0, 120, 5, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (2, 'Williams-Green Service', 'Management conference key eat couple partner.', 342.43, 11, 0, 120, 2, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (3, 'Graham, Alexander and Wilkins Service', 'Act cell candidate subject minute stock tough agent field discuss.', 222.91, 10, 0, 120, 1, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (4, 'Robertson, Welch and Flores Service', 'Network authority drug put character hour current get heavy allow I his natural.', 384.98, 2, 0, 90, 1, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (5, 'Ingram, Valdez and Taylor Service', 'Without morning if chance walk thousand answer reason parent administration now.', 121.15, 5, 1, 60, 5, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (6, 'Ballard PLC Service', 'Decide make some manage strategy ok science.', 399.17, 20, 0, 120, 2, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (7, 'Wilson-Reyes Service', 'Already soldier maintain easy source real production western move area probably specific.', 299.77, 25, 0, 90, 5, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (8, 'Brown, Santos and Flowers Service', 'Cost be especially see push small agreement officer cause also.', 286.21, 14, 1, 90, 1, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (9, 'Reid Group Service', 'Professional car red audience group stand together Congress government more.', 408.69, 25, 1, 45, 5, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (10, 'Mathews-Hines Service', 'Rock everything by key wall certainly.', 192.44, 15, 0, 30, 3, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (11, 'Jensen-Marquez Service', 'Writer wrong blood just very the stage language always quickly talk husband in.', 152.41, 30, 1, 30, 3, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (12, 'Gardner-Herrera Service', 'Trip respond laugh product little arm here send hour crime born wear.', 357.93, 15, 1, 30, 3, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (13, 'Roth, Raymond and Weaver Service', 'Respond next current money together enjoy.', 219.79, 3, 1, 30, 2, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (14, 'Howard, Thompson and Lara Service', 'Mouth entire one know eight quite fact drop behind range everything child ago.', 415.1, 24, 1, 30, 3, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (15, 'Barnett, Daniel and Mckee Service', 'Someone positive like late drive culture under mention.', 295.88, 29, 1, 120, 4, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (16, 'Ryan PLC Service', 'Fine wish save management thousand represent main win many attack professor each.', 373.42, 12, 1, 60, 4, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (17, 'Rodriguez, Hampton and Williams Service', 'Take education would everything close election whether upon ten thus investment collection.', 498.61, 12, 0, 30, 2, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (18, 'Murphy PLC Service', 'Our whatever cup sure happy forward tell machine.', 186.91, 12, 0, 120, 2, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (19, 'Jackson-Harding Service', 'President mission candidate similar see step.', 189.77, 8, 0, 120, 3, NOW(), NOW());
INSERT INTO service (id, name, description, price, discount, is_negotiable, duration, service_type_id, created_at, updated_at)
VALUES (20, 'Jackson and Sons Service', 'Six guess education apply evidence training.', 442.23, 29, 1, 120, 4, NOW(), NOW());

-- Insert Service-Tag Relations
INSERT INTO service_tag (service_id, tag_id) VALUES (1, 2);
INSERT INTO service_tag (service_id, tag_id) VALUES (1, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (1, 9);
INSERT INTO service_tag (service_id, tag_id) VALUES (2, 9);
INSERT INTO service_tag (service_id, tag_id) VALUES (2, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (2, 8);
INSERT INTO service_tag (service_id, tag_id) VALUES (2, 14);
INSERT INTO service_tag (service_id, tag_id) VALUES (3, 14);
INSERT INTO service_tag (service_id, tag_id) VALUES (3, 1);
INSERT INTO service_tag (service_id, tag_id) VALUES (3, 7);
INSERT INTO service_tag (service_id, tag_id) VALUES (4, 8);
INSERT INTO service_tag (service_id, tag_id) VALUES (4, 13);
INSERT INTO service_tag (service_id, tag_id) VALUES (4, 4);
INSERT INTO service_tag (service_id, tag_id) VALUES (5, 14);
INSERT INTO service_tag (service_id, tag_id) VALUES (5, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (5, 2);
INSERT INTO service_tag (service_id, tag_id) VALUES (6, 11);
INSERT INTO service_tag (service_id, tag_id) VALUES (6, 12);
INSERT INTO service_tag (service_id, tag_id) VALUES (6, 4);
INSERT INTO service_tag (service_id, tag_id) VALUES (7, 11);
INSERT INTO service_tag (service_id, tag_id) VALUES (7, 13);
INSERT INTO service_tag (service_id, tag_id) VALUES (7, 14);
INSERT INTO service_tag (service_id, tag_id) VALUES (8, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (8, 5);
INSERT INTO service_tag (service_id, tag_id) VALUES (9, 3);
INSERT INTO service_tag (service_id, tag_id) VALUES (9, 13);
INSERT INTO service_tag (service_id, tag_id) VALUES (10, 7);
INSERT INTO service_tag (service_id, tag_id) VALUES (10, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (10, 3);
INSERT INTO service_tag (service_id, tag_id) VALUES (11, 5);
INSERT INTO service_tag (service_id, tag_id) VALUES (11, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (11, 13);
INSERT INTO service_tag (service_id, tag_id) VALUES (11, 8);
INSERT INTO service_tag (service_id, tag_id) VALUES (12, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (12, 7);
INSERT INTO service_tag (service_id, tag_id) VALUES (12, 5);
INSERT INTO service_tag (service_id, tag_id) VALUES (13, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (13, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (13, 1);
INSERT INTO service_tag (service_id, tag_id) VALUES (14, 7);
INSERT INTO service_tag (service_id, tag_id) VALUES (14, 12);
INSERT INTO service_tag (service_id, tag_id) VALUES (14, 11);
INSERT INTO service_tag (service_id, tag_id) VALUES (15, 9);
INSERT INTO service_tag (service_id, tag_id) VALUES (15, 3);
INSERT INTO service_tag (service_id, tag_id) VALUES (15, 8);
INSERT INTO service_tag (service_id, tag_id) VALUES (16, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (16, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (16, 11);
INSERT INTO service_tag (service_id, tag_id) VALUES (17, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (17, 9);
INSERT INTO service_tag (service_id, tag_id) VALUES (17, 6);
INSERT INTO service_tag (service_id, tag_id) VALUES (18, 12);
INSERT INTO service_tag (service_id, tag_id) VALUES (18, 10);
INSERT INTO service_tag (service_id, tag_id) VALUES (18, 13);
INSERT INTO service_tag (service_id, tag_id) VALUES (18, 14);
INSERT INTO service_tag (service_id, tag_id) VALUES (19, 1);
INSERT INTO service_tag (service_id, tag_id) VALUES (19, 12);
INSERT INTO service_tag (service_id, tag_id) VALUES (19, 4);
INSERT INTO service_tag (service_id, tag_id) VALUES (19, 11);
INSERT INTO service_tag (service_id, tag_id) VALUES (20, 9);
INSERT INTO service_tag (service_id, tag_id) VALUES (20, 1);
INSERT INTO service_tag (service_id, tag_id) VALUES (20, 4);