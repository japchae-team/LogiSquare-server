ALTER TABLE users
    MODIFY role ENUM('ADMIN','USER') NOT NULL;

ALTER TABLE items
    MODIFY rotation_grade ENUM('A','B','C');

ALTER TABLE storage_locations
    MODIFY area_code ENUM('A','B','C'),
    MODIFY location_grade ENUM('A','B','C');
