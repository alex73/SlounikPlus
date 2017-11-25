/**
 * Database installation.
 * 
 * 1. Install postgresql, postgresql-client
 * 
 * 2. sudo -u postgres psql - login to postgresql
 * 
 * 3. \password - change password for postgres user
 * 
 * 4. CREATE DATABASE <dbname> ENCODING 'UTF8' TEMPLATE template0 LC_COLLATE 'be_BY.UTF-8'; - create database
 * 
 * 5. psql -h localhost -U postgres -W <dbname> -f DB_STRUCTURE.sql
 *
 * TODO : індэксы
 */
DROP TABLE IF EXISTS Issues;
DROP TABLE IF EXISTS Comments;
DROP TABLE IF EXISTS ArticlesHistory;
DROP TABLE IF EXISTS ArticleNotes;
DROP TABLE IF EXISTS Articles;

CREATE TABLE Articles (
	articleId SERIAL PRIMARY KEY,
	articleType VARCHAR(30) NOT NULL,
	header VARCHAR(250) NOT NULL,
	xml BYTEA,
	assignedUsers VARCHAR(50)[] NULL,
	state VARCHAR(50) NOT NULL,
	deleted BOOLEAN NOT NULL DEFAULT FALSE,
	markers VARCHAR(50)[] NOT NULL,
	watchers VARCHAR(50)[] NOT NULL,
	linkedTo VARCHAR(250)[] NOT NULL,
	textForSearch TEXT,
	lettersCount INTEGER NOT NULL,
	lastUpdated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	validationError VARCHAR(500) NULL
);

CREATE TABLE ArticlesHistory (
	historyId SERIAL PRIMARY KEY,
	articleId INTEGER NOT NULL REFERENCES Articles(articleId),
	changed TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	changer VARCHAR(50) NOT NULL,
	oldState VARCHAR(50) NULL,
	newState VARCHAR(50) NULL,
	oldHeader VARCHAR(250) NULL,
	newHeader VARCHAR(250) NULL,
	oldAssignedUsers VARCHAR(50)[] NULL,
	newAssignedUsers VARCHAR(50)[] NULL,
	oldXml BYTEA,
	newXml BYTEA
);

CREATE TABLE Comments (
	commentId SERIAL PRIMARY KEY,
	articleId INTEGER NOT NULL REFERENCES Articles(articleId),
	created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	author VARCHAR(50) NOT NULL,
	comment TEXT NOT NULL
);

CREATE TABLE Issues (
	issueId SERIAL PRIMARY KEY,
	articleId INTEGER NOT NULL REFERENCES Articles(articleId),
	created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
	author VARCHAR(50) NOT NULL,
	comment TEXT,
	oldXml BYTEA,
	newXml BYTEA,
	fixed TIMESTAMP WITHOUT TIME ZONE NULL,
	fixer VARCHAR(50) NULL,
	accepted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE ArticleNotes (
    articleId INTEGER NOT NULL REFERENCES Articles(articleId),
    creator VARCHAR(50) NOT NULL,
    note TEXT NOT NULL,
    PRIMARY KEY (articleId, creator)
);
