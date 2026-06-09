-- Security patch: RBAC roles, permissions, test accounts

START TRANSACTION;

INSERT INTO `sys_role` (`id`, `name`, `sort`, `scope`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2001, 'VIEWER', 40, '1', 'enabled', 'read only device access', 'security_patch', NOW(), 'security_patch', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `name` = 'VIEWER');

INSERT INTO `sys_role` (`id`, `name`, `sort`, `scope`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`)
SELECT 2002, 'OPERATOR', 41, '1', 'enabled', 'device control access', 'security_patch', NOW(), 'security_patch', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `name` = 'OPERATOR');

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT r.id, m.id
FROM `sys_role` r
JOIN `sys_menu` m ON m.perms = 'iot:device:view'
WHERE r.name = 'VIEWER'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = r.id AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT r.id, m.id
FROM `sys_role` r
JOIN `sys_menu` m ON m.perms = 'iot:device:view'
WHERE r.name = 'OPERATOR'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = r.id AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT r.id, m.id
FROM `sys_role` r
JOIN `sys_menu` m ON m.perms = 'iot:device:ctrl'
WHERE r.name = 'OPERATOR'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = r.id AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.perms = 'core:admin:view'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = 2 AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.perms = 'core:admin:add'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = 2 AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.perms = 'core:admin:edit'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = 2 AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.perms = 'core:admin:del'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = 2 AND rm.mid = m.id
  );

INSERT INTO `sys_role_menu` (`rid`, `mid`)
SELECT 2, m.id
FROM `sys_menu` m
WHERE m.perms = 'core:admin:pwd'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm WHERE rm.rid = 2 AND rm.mid = m.id
  );

INSERT INTO `sys_admin` (
    `id`, `org_id`, `post_id`, `account`, `name`, `email`, `phone`, `sex`,
    `avatar`, `password`, `status`, `login_ip`, `login_date`, `remark`,
    `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    2001, 3, 1, 'viewer', 'viewer', 'viewer@example.local', '13000000001', 3,
    '', 'eae4a576fe9c14c010abf4e7f7e9110f', 'enabled', '', NULL, 'security viewer',
    'security_patch', NOW(), 'security_patch', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_admin` WHERE `account` = 'viewer');

INSERT INTO `sys_admin_role` (`aid`, `rid`)
SELECT a.id, r.id
FROM `sys_admin` a
JOIN `sys_role` r ON r.name = 'VIEWER'
WHERE a.account = 'viewer'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_admin_role` ar WHERE ar.aid = a.id AND ar.rid = r.id
  );

INSERT INTO `sys_admin` (
    `id`, `org_id`, `post_id`, `account`, `name`, `email`, `phone`, `sex`,
    `avatar`, `password`, `status`, `login_ip`, `login_date`, `remark`,
    `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
    2002, 3, 1, 'operator', 'operator', 'operator@example.local', '13000000002', 3,
    '', '56ea50547e1e49964e49d7022b4e980c', 'enabled', '', NULL, 'security operator',
    'security_patch', NOW(), 'security_patch', NOW()
WHERE NOT EXISTS (SELECT 1 FROM `sys_admin` WHERE `account` = 'operator');

INSERT INTO `sys_admin_role` (`aid`, `rid`)
SELECT a.id, r.id
FROM `sys_admin` a
JOIN `sys_role` r ON r.name = 'OPERATOR'
WHERE a.account = 'operator'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_admin_role` ar WHERE ar.aid = a.id AND ar.rid = r.id
  );

COMMIT;
