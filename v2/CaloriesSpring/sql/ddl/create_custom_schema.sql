--------------------------------------------------------------
--             Runs on PostgreSQL 9.5                       --
--------------------------------------------------------------

-- NO NEED TO RUN: THE JPA PERSISTENCE.XML IS CONFIGURED TO GENERATE 
-- THE TABLES FROM THE ENTITIES WHENEVER NEEDED (CREATING SCHEMA OBJECTS 
-- ON THE FIRST TIME, THEN UPDATING WHEN THERE ARE CHANGES ON THE ENTITY).

CREATE TABLE App_User
    (
    	ID					SERIAL PRIMARY KEY,
    	login				VARCHAR(10) NOT NULL,
    	Password			VARCHAR(20) NOT NULL,
		Name	 			VARCHAR(60) NOT NULL,
		Gender				CHAR NOT NULL,
		Creation_Dt			Timestamptz NOT NULL,
		Daily_Calories		Integer NOT NULL
    ) 
;
ALTER TABLE App_User 
    ADD CONSTRAINT User_Login_UNIQ UNIQUE ( Login ) 
;

CREATE TABLE Role
    ( 
    	ID					SERIAL PRIMARY KEY,
		name				VARCHAR(10) NOT NULL
    ) 
;
ALTER TABLE Role 
    ADD CONSTRAINT Role_Name_UNIQ UNIQUE ( name ) 
;


CREATE TABLE User_Role
    ( 
		User_ID				INTEGER NOT NULL,
    	Role_ID	 			INTEGER NOT NULL
    ) 
;
ALTER TABLE User_Role 
    ADD CONSTRAINT User_Role_PK PRIMARY KEY ( User_ID, Role_ID ) 
;
ALTER TABLE User_Role 
    ADD CONSTRAINT User_Role_User_FK FOREIGN KEY ( User_ID ) REFERENCES App_User( ID ) 
;
ALTER TABLE User_Role 
    ADD CONSTRAINT User_Role_Role_FK FOREIGN KEY ( Role_ID ) REFERENCES Role ( ID ) 
;

CREATE TABLE Meal 
    ( 
    	ID					SERIAL PRIMARY KEY,
    	User_ID				INTEGER NOT NULL,
		Meal_Time			Timestamptz NOT NULL,
    	Description			VARCHAR(60) NOT NULL,
		Calories			INTEGER NOT NULL
    ) 
;
ALTER TABLE Meal
    ADD CONSTRAINT Meal_Uniq UNIQUE ( User_ID, Meal_Time ) 
;
ALTER TABLE Meal 
    ADD CONSTRAINT Meal_User_FK FOREIGN KEY ( User_ID ) REFERENCES App_User( ID ) 
;
CREATE INDEX Meal_IDX ON Meal (User_ID, Meal_Time);
