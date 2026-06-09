SET NAMES utf8mb4;
INSERT INTO sys_menu (id, name, pid, sort, url, msn, type, status, perms, icon, remark, target, create_time, update_time) VALUES
(1480, 'Mqtt子设备', 1411, 68, '/iot/mqttDevice', 'iot', 'V', 'enabled', NULL, 'iz-icon-jindu', '', '_self', '2025-05-20 06:31:24', NULL),
(1481, '温湿度LED实验', 1411, 78, '/iot/experiment/env-led', 'iot', 'V', 'enabled', NULL, 'iz-icon-data', '温湿度实时展示与LED开关实验页', '_self', '2026-06-03 00:00:00', NULL);
INSERT INTO sys_role_menu (rid, mid) VALUES (2,1480),(2,1481),(7,1481);
