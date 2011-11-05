CREATE SEQUENCE DB_VERSIONS_SEQUENCE INCREMENT BY 1 START WITH 1 MINVALUE 0;

CREATE TABLE DBVERSIONS(DBVER_ID INT NOT NULL, VER VARCHAR(255), PRIMARY KEY(DBVER_ID));

create view MAX_VERSION_VIEW as select max(DBVER_ID) as MAX_ID from DBVERSIONS;
