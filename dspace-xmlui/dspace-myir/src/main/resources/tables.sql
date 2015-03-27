-------------------------------------------------------
-- Favorite table
-------------------------------------------------------

CREATE SEQUENCE favorite_seq;

CREATE TABLE Favorite (
  favorite_id INTEGER PRIMARY KEY,
  item_id INTEGER NOT NULL REFERENCES Item(item_id),
  eperson_id INTEGER NOT NULL REFERENCES EPerson(eperson_id),
  add_date TIMESTAMP,
  status INTEGER
);

CREATE INDEX favorite_item_fk_idx ON Favorite(item_id);
CREATE INDEX favorite_eperson_fk_idx ON Favorite(eperson_id);

-------------------------------------------------------
-- Authorship table
-------------------------------------------------------

CREATE SEQUENCE authorship_seq;

CREATE TABLE Authorship (
  authorship_id INTEGER PRIMARY KEY,
  item_id INTEGER NOT NULL REFERENCES Item(item_id),
  eperson_id INTEGER NOT NULL REFERENCES EPerson(eperson_id),
  attr VARCHAR(150)
);

CREATE INDEX authorship_item_fk_idx ON Authorship(item_id);
CREATE INDEX authorship_eperson_fk_idx ON Authorship(eperson_id);

-------------------------------------------------------
-- Academic Interest table
-------------------------------------------------------

CREATE SEQUENCE academicinterest_seq;

CREATE TABLE AcademicInterest (
  academicinterest_id INTEGER PRIMARY KEY,
  eperson_id INTEGER NOT NULL REFERENCES EPerson(eperson_id),
  term VARCHAR(350)
);

CREATE INDEX academicinterest_eperson_fk_idx ON AcademicInterest(eperson_id);

-------------------------------------------------------
-- EPersonProfile table
-------------------------------------------------------

CREATE SEQUENCE epersonprofile_seq;

CREATE TABLE EPersonProfile (
  profile_id INTEGER PRIMARY KEY,
  eperson_id INTEGER NOT NULL REFERENCES EPerson(eperson_id) UNIQUE,
  profession VARCHAR(255),
  affiliation VARCHAR(255),
  website_url VARCHAR(255),
  is_public BOOLEAN NOT NULL
);

CREATE INDEX epersonprofile_eperson_fk_idx ON EPersonProfile(eperson_id);