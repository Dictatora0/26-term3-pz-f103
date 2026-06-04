SET NAMES utf8mb4;
DROP TABLE IF EXISTS sys_access_log_bak_charsetfix_manual_20260603;
CREATE TABLE sys_access_log_bak_charsetfix_manual_20260603 AS
SELECT * FROM sys_access_log WHERE user_name LIKE '%?%';
DROP TABLE IF EXISTS sys_online_user_bak_charsetfix_manual_20260603;
CREATE TABLE sys_online_user_bak_charsetfix_manual_20260603 AS
SELECT * FROM sys_online_user WHERE user_nick LIKE '%?%';
UPDATE sys_access_log SET user_name='超级管理员' WHERE user_id=1 AND user_name LIKE '%?%';
UPDATE sys_online_user SET user_nick='超级管理员' WHERE account='admin' AND user_nick LIKE '%?%';
SELECT COUNT(*) AS bad_access_log FROM sys_access_log WHERE user_name LIKE '%?%';
SELECT COUNT(*) AS bad_online_user FROM sys_online_user WHERE user_nick LIKE '%?%';
