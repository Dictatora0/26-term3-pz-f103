-- Security patch: OAuth2 client, PKCE flags, local user mapping

START TRANSACTION;

SET @has_public_client := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'oauth2_app'
      AND COLUMN_NAME = 'public_client'
);

SET @has_require_pkce := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'oauth2_app'
      AND COLUMN_NAME = 'require_pkce'
);

SET @has_status := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'oauth2_app'
      AND COLUMN_NAME = 'status'
);

SET @has_abbreviate := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'oauth2_app'
      AND COLUMN_NAME = 'abbreviate'
);

SET @has_remark := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'oauth2_app'
      AND COLUMN_NAME = 'remark'
);

SET @sql := IF(@has_public_client = 0,
    'ALTER TABLE `oauth2_app` ADD COLUMN `public_client` tinyint NOT NULL DEFAULT 0 COMMENT ''public client flag'' AFTER `client_name`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_require_pkce = 0,
    'ALTER TABLE `oauth2_app` ADD COLUMN `require_pkce` tinyint NOT NULL DEFAULT 1 COMMENT ''pkce required flag'' AFTER `public_client`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_status = 0,
    'ALTER TABLE `oauth2_app` ADD COLUMN `status` tinyint NOT NULL DEFAULT 1 COMMENT ''status'' AFTER `client_secret`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_abbreviate = 0,
    'ALTER TABLE `oauth2_app` ADD COLUMN `abbreviate` varchar(64) DEFAULT NULL COMMENT ''abbreviation'' AFTER `client_name`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql := IF(@has_remark = 0,
    'ALTER TABLE `oauth2_app` ADD COLUMN `remark` varchar(255) DEFAULT NULL COMMENT ''remark'' AFTER `abbreviate`',
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO `oauth2_app` (
    `id`, `client_id`, `client_secret`, `status`, `contract_scope`, `allow_url`,
    `client_name`, `public_client`, `require_pkce`, `abbreviate`, `remark`,
    `access_token_timeout`, `refresh_token_timeout`, `client_token_timeout`,
    `past_token_timeout`, `create_time`
)
SELECT
    2001,
    'iboot-local-web',
    NULL,
    1,
    'device.read,device.control,user.manage',
    'http://localhost:5173/oauth/callback.html,http://127.0.0.1:5173/oauth/callback.html',
    'iBOOT Local Web',
    1,
    1,
    'local-web',
    'local vite public client',
    1800,
    604800,
    1800,
    1800,
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM `oauth2_app` WHERE `client_id` = 'iboot-local-web');

UPDATE `oauth2_app`
SET
    `client_secret` = NULL,
    `status` = 1,
    `contract_scope` = 'device.read,device.control,user.manage',
    `allow_url` = 'http://localhost:5173/oauth/callback.html,http://127.0.0.1:5173/oauth/callback.html',
    `client_name` = 'iBOOT Local Web',
    `public_client` = 1,
    `require_pkce` = 1,
    `abbreviate` = 'local-web',
    `remark` = 'local vite public client',
    `access_token_timeout` = 1800,
    `refresh_token_timeout` = 604800,
    `client_token_timeout` = 1800,
    `past_token_timeout` = 1800
WHERE `client_id` = 'iboot-local-web';

INSERT INTO `oauth2_user` (`id`, `login_id`, `client_id`, `openid`, `create_time`)
SELECT 2001, '2001', 'iboot-local-web', 'viewer-iboot-local-web', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `oauth2_user`
    WHERE `login_id` = '2001' AND `client_id` = 'iboot-local-web'
);

INSERT INTO `oauth2_user` (`id`, `login_id`, `client_id`, `openid`, `create_time`)
SELECT 2002, '2002', 'iboot-local-web', 'operator-iboot-local-web', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `oauth2_user`
    WHERE `login_id` = '2002' AND `client_id` = 'iboot-local-web'
);

INSERT INTO `oauth2_user` (`id`, `login_id`, `client_id`, `openid`, `create_time`)
SELECT 1, '1', 'iboot-local-web', 'admin-iboot-local-web', NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `oauth2_user`
    WHERE `login_id` = '1' AND `client_id` = 'iboot-local-web'
);

COMMIT;
