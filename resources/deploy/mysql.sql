/*
 * database schema and user creation
 * run this inside `mysql -u root', by issuing there the command: source resources/deploy/mysql.sql
 */

/* schema and user */

SET @schemaName="boteval";
SET @text = CONCAT('DROP SCHEMA ', @schemaName); PREPARE stmt FROM @text; EXECUTE stmt; /* previously DROP SCHEMA `boteval` */
SET @text = CONCAT('CREATE SCHEMA ', @schemaName, ' DEFAULT CHARACTER SET utf8'); PREPARE stmt FROM @text; EXECUTE stmt; /* previously CREATE SCHEMA `boteval` DEFAULT CHARACTER SET utf8 */

DROP USER 'boteval'@'localhost';
CREATE USER 'boteval'@'localhost' IDENTIFIED BY 'boteval234%^&';
GRANT ALL ON boteval . * TO 'boteval'@'localhost';
FLUSH PRIVILEGES;

USE boteval;

/**
  scenario entities
**/

/* tables */
CREATE TABLE `boteval`.`exchanges` (
  `text` VARCHAR(2048) NULL,
  `is_user` TINYINT NOT NULL COMMENT 'is it a user initiated exchange? false would mean a bot initiated exchange',
  `exchange_time` DATETIME(6) NOT NULL,
  `session_id` INT NOT NULL COMMENT 'session id assigned by the bot driver',
  `scenario_execution_id` BIGINT NOT NULL);

/* scenario executions */
CREATE TABLE `boteval`.`scenario_executions` (
  `scenario_id` BIGINT NOT NULL COMMENT 'connects to a scenario id',
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT 'execution id',
  `parent_id` BIGINT NULL COMMENT 'execution id of parent executor (null would mean no parent, i.e. a top level execution)',
  `started` DATETIME(6) NOT NULL,
  `ended` DATETIME(6) NULL COMMENT 'can be null while the execution has not yet ended',
  `parameters` JSON COMMENT 'parameters given to the scenario',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` DESC));

/* scenarios */
CREATE TABLE `boteval`.`scenarios` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` CHAR(128) NOT NULL,
  `project_id` INT NOT NULL COMMENT 'the project this scenario belongs to',
  UNIQUE KEY `unique_key` (`name`, `project_id`),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` DESC));

/* projects */
CREATE TABLE `boteval`.`projects` (
  `name` CHAR(128) NOT NULL COMMENT 'top level identification of a project',
  `owner` CHAR(64) NOT NULL COMMENT 'further identifying a project',
  `version_name` CHAR(64) NULL COMMENT 'optionally further identifying a project',
  /* `git_hash` CHAR(64) NOT NULL, */
  `id` INT NOT NULL AUTO_INCREMENT,
  UNIQUE KEY `unique_key` (`name`, `owner`, `version_name`),
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` DESC));

/* foreign keys connecting between the tables */

ALTER TABLE `boteval`.`exchanges`
ADD CONSTRAINT `fk_exchanges_1`
  FOREIGN KEY (`scenario_execution_id`)
  REFERENCES `boteval`.`scenario_executions` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `boteval`.`scenario_executions`
ADD CONSTRAINT `fk_scenario_executions_1`
  FOREIGN KEY (`scenario_id`)
  REFERENCES `boteval`.`scenarios` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `boteval`.`scenarios`
ADD CONSTRAINT `fk_scenarios_1`
  FOREIGN KEY (`project_id`)
  REFERENCES `boteval`.`projects` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


