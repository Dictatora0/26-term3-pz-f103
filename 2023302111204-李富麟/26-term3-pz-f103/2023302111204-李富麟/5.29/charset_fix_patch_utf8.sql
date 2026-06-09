SET NAMES utf8mb4;
UPDATE sys_menu
SET name='Mqtt子设备', remark=''
WHERE id=1480;
SELECT COUNT(*) AS bad_menu_after_final FROM sys_menu WHERE name LIKE '%?%' OR IFNULL(remark,'') LIKE '%?%';
SELECT id,name,remark,url FROM sys_menu WHERE id IN (1453,1480,1481);
