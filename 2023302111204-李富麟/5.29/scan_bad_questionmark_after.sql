SET NAMES utf8mb4;
SELECT 'iot_collect_data' AS table_name, 'field' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_data` WHERE `field` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_data' AS table_name, 'address' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_data` WHERE `address` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_data' AS table_name, 'value' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_data` WHERE `value` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_data' AS table_name, 'reason' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_data` WHERE `reason` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_detail' AS table_name, 'store_action' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_detail` WHERE `store_action` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_task' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_task` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_task' AS table_name, 'cron' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_task` WHERE `cron` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_task' AS table_name, 'status' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_task` WHERE `status` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_task' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_task` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_collect_task' AS table_name, 'reason' AS column_name, COUNT(*) AS bad_count FROM `iot_collect_task` WHERE `reason` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'lon' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `lon` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'lat' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `lat` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'ip' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `ip` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'address' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `address` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'extend' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `extend` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'account' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `account` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'password' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `password` LIKE '%?%'
UNION ALL
SELECT 'iot_device' AS table_name, 'device_sn' AS column_name, COUNT(*) AS bad_count FROM `iot_device` WHERE `device_sn` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'lat' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `lat` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'lon' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `lon` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'path' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `path` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_device_group' AS table_name, 'address' AS column_name, COUNT(*) AS bad_count FROM `iot_device_group` WHERE `address` LIKE '%?%'
UNION ALL
SELECT 'iot_event_source' AS table_name, 'cron' AS column_name, COUNT(*) AS bad_count FROM `iot_event_source` WHERE `cron` LIKE '%?%'
UNION ALL
SELECT 'iot_event_source' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_event_source` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_event_source' AS table_name, 'reason' AS column_name, COUNT(*) AS bad_count FROM `iot_event_source` WHERE `reason` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'host' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `host` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'username' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `username` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'password' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `password` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'protocol_type' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `protocol_type` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_gateway' AS table_name, 'reason' AS column_name, COUNT(*) AS bad_count FROM `iot_gateway` WHERE `reason` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api' AS table_name, 'direct' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api` WHERE `direct` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api' AS table_name, 'func_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api` WHERE `func_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'api_code' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `api_code` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'attr_field' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `attr_field` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'data_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `data_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'attr_name' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `attr_name` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'field_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `field_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'value' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `value` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'protocol_data_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `protocol_data_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'protocol_attr_field' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `protocol_attr_field` LIKE '%?%'
UNION ALL
SELECT 'iot_model_api_config' AS table_name, 'protocol_attr_name' AS column_name, COUNT(*) AS bad_count FROM `iot_model_api_config` WHERE `protocol_attr_name` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'field' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `field` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'data_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `data_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'unit' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `unit` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'attr_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `attr_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'real_type' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `real_type` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'default_value' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `default_value` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'resolver' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `resolver` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr' AS table_name, 'script' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr` WHERE `script` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr_dict' AS table_name, 'path' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr_dict` WHERE `path` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr_dict' AS table_name, 'dict_name' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr_dict` WHERE `dict_name` LIKE '%?%'
UNION ALL
SELECT 'iot_model_attr_dict' AS table_name, 'dict_value' AS column_name, COUNT(*) AS bad_count FROM `iot_model_attr_dict` WHERE `dict_value` LIKE '%?%'
UNION ALL
SELECT 'iot_point_group' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_point_group` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_product' AS table_name, 'logo' AS column_name, COUNT(*) AS bad_count FROM `iot_product` WHERE `logo` LIKE '%?%'
UNION ALL
SELECT 'iot_product' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `iot_product` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'iot_product' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_product` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_product' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_product` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_product' AS table_name, 'protocol_code' AS column_name, COUNT(*) AS bad_count FROM `iot_product` WHERE `protocol_code` LIKE '%?%'
UNION ALL
SELECT 'iot_product_type' AS table_name, 'path' AS column_name, COUNT(*) AS bad_count FROM `iot_product_type` WHERE `path` LIKE '%?%'
UNION ALL
SELECT 'iot_product_type' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_product_type` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_product_type' AS table_name, 'alias' AS column_name, COUNT(*) AS bad_count FROM `iot_product_type` WHERE `alias` LIKE '%?%'
UNION ALL
SELECT 'iot_product_type' AS table_name, 'image' AS column_name, COUNT(*) AS bad_count FROM `iot_product_type` WHERE `image` LIKE '%?%'
UNION ALL
SELECT 'iot_product_type' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_product_type` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'impl_class' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `impl_class` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'impl_mode' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `impl_mode` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'jar_path' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `jar_path` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'version' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `version` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'ctrl_mode' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `ctrl_mode` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'check_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `check_type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol' AS table_name, 'decoder_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol` WHERE `decoder_type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api' AS table_name, 'protocol_code' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api` WHERE `protocol_code` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api' AS table_name, 'func_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api` WHERE `func_type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'field_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `field_type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'protocol_api_code' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `protocol_api_code` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'protocol_attr_field' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `protocol_attr_field` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'position' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `position` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_api_config' AS table_name, 'direction' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_api_config` WHERE `direction` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'protocol_code' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `protocol_code` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'field' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `field` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'data_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `data_type` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'iot_protocol_attr' AS table_name, 'attr_type' AS column_name, COUNT(*) AS bad_count FROM `iot_protocol_attr` WHERE `attr_type` LIKE '%?%'
UNION ALL
SELECT 'iot_serial' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_serial` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_serial' AS table_name, 'com' AS column_name, COUNT(*) AS bad_count FROM `iot_serial` WHERE `com` LIKE '%?%'
UNION ALL
SELECT 'iot_serial' AS table_name, 'status' AS column_name, COUNT(*) AS bad_count FROM `iot_serial` WHERE `status` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'address' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `address` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'message' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `message` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'encode' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `encode` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'field_name' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `field_name` LIKE '%?%'
UNION ALL
SELECT 'iot_signal' AS table_name, 'direct' AS column_name, COUNT(*) AS bad_count FROM `iot_signal` WHERE `direct` LIKE '%?%'
UNION ALL
SELECT 'oauth2_app' AS table_name, 'client_id' AS column_name, COUNT(*) AS bad_count FROM `oauth2_app` WHERE `client_id` LIKE '%?%'
UNION ALL
SELECT 'oauth2_app' AS table_name, 'client_secret' AS column_name, COUNT(*) AS bad_count FROM `oauth2_app` WHERE `client_secret` LIKE '%?%'
UNION ALL
SELECT 'oauth2_app' AS table_name, 'contract_scope' AS column_name, COUNT(*) AS bad_count FROM `oauth2_app` WHERE `contract_scope` LIKE '%?%'
UNION ALL
SELECT 'oauth2_app' AS table_name, 'allow_url' AS column_name, COUNT(*) AS bad_count FROM `oauth2_app` WHERE `allow_url` LIKE '%?%'
UNION ALL
SELECT 'oauth2_app' AS table_name, 'client_name' AS column_name, COUNT(*) AS bad_count FROM `oauth2_app` WHERE `client_name` LIKE '%?%'
UNION ALL
SELECT 'oauth2_client_user' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `oauth2_client_user` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'oauth2_client_user' AS table_name, 'user_type' AS column_name, COUNT(*) AS bad_count FROM `oauth2_client_user` WHERE `user_type` LIKE '%?%'
UNION ALL
SELECT 'oauth2_client_user' AS table_name, 'openid' AS column_name, COUNT(*) AS bad_count FROM `oauth2_client_user` WHERE `openid` LIKE '%?%'
UNION ALL
SELECT 'oauth2_client_user' AS table_name, 'nickname' AS column_name, COUNT(*) AS bad_count FROM `oauth2_client_user` WHERE `nickname` LIKE '%?%'
UNION ALL
SELECT 'oauth2_client_user' AS table_name, 'avatar_url' AS column_name, COUNT(*) AS bad_count FROM `oauth2_client_user` WHERE `avatar_url` LIKE '%?%'
UNION ALL
SELECT 'oauth2_user' AS table_name, 'login_id' AS column_name, COUNT(*) AS bad_count FROM `oauth2_user` WHERE `login_id` LIKE '%?%'
UNION ALL
SELECT 'oauth2_user' AS table_name, 'client_id' AS column_name, COUNT(*) AS bad_count FROM `oauth2_user` WHERE `client_id` LIKE '%?%'
UNION ALL
SELECT 'oauth2_user' AS table_name, 'openid' AS column_name, COUNT(*) AS bad_count FROM `oauth2_user` WHERE `openid` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'cron' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `cron` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'status' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `status` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'params' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `params` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'method' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `method` LIKE '%?%'
UNION ALL
SELECT 'qrtz_job_task' AS table_name, 'job_name' AS column_name, COUNT(*) AS bad_count FROM `qrtz_job_task` WHERE `job_name` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'url' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `url` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'ip' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `ip` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'msn' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `msn` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'title' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `title` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'params' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `params` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'method' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `method` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'user_name' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `user_name` LIKE '%?%'
UNION ALL
SELECT 'sys_access_log' AS table_name, 'err_msg' AS column_name, COUNT(*) AS bad_count FROM `sys_access_log` WHERE `err_msg` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'account' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `account` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'email' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `email` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'phone' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `phone` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'avatar' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `avatar` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'password' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `password` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'login_ip' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `login_ip` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'create_by' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `create_by` LIKE '%?%'
UNION ALL
SELECT 'sys_admin' AS table_name, 'update_by' AS column_name, COUNT(*) AS bad_count FROM `sys_admin` WHERE `update_by` LIKE '%?%'
UNION ALL
SELECT 'sys_captcha' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `sys_captcha` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'sys_captcha' AS table_name, 'captcha' AS column_name, COUNT(*) AS bad_count FROM `sys_captcha` WHERE `captcha` LIKE '%?%'
UNION ALL
SELECT 'sys_config' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_config` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_config' AS table_name, 'label' AS column_name, COUNT(*) AS bad_count FROM `sys_config` WHERE `label` LIKE '%?%'
UNION ALL
SELECT 'sys_config' AS table_name, 'value' AS column_name, COUNT(*) AS bad_count FROM `sys_config` WHERE `value` LIKE '%?%'
UNION ALL
SELECT 'sys_config' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_config` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'label' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `label` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'value' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `value` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'create_by' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `create_by` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_data' AS table_name, 'update_by' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_data` WHERE `update_by` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_type' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_type` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_type' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_type` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_type' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_type` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_type' AS table_name, 'create_by' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_type` WHERE `create_by` LIKE '%?%'
UNION ALL
SELECT 'sys_dict_type' AS table_name, 'update_by' AS column_name, COUNT(*) AS bad_count FROM `sys_dict_type` WHERE `update_by` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'url' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `url` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'msn' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `msn` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'perms' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `perms` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'icon' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `icon` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_menu' AS table_name, 'target' AS column_name, COUNT(*) AS bad_count FROM `sys_menu` WHERE `target` LIKE '%?%'
UNION ALL
SELECT 'sys_message_source' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_message_source` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_message_source' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_message_source` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_message_source' AS table_name, 'channel' AS column_name, COUNT(*) AS bad_count FROM `sys_message_source` WHERE `channel` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'template_id' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `template_id` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'template_name' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `template_name` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'template_title' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `template_title` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'content' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `content` LIKE '%?%'
UNION ALL
SELECT 'sys_message_template' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_message_template` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_notify' AS table_name, 'title' AS column_name, COUNT(*) AS bad_count FROM `sys_notify` WHERE `title` LIKE '%?%'
UNION ALL
SELECT 'sys_notify' AS table_name, 'content' AS column_name, COUNT(*) AS bad_count FROM `sys_notify` WHERE `content` LIKE '%?%'
UNION ALL
SELECT 'sys_notify' AS table_name, 'type' AS column_name, COUNT(*) AS bad_count FROM `sys_notify` WHERE `type` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'session_id' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `session_id` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'account' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `account` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'user_nick' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `user_nick` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'access_ip' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `access_ip` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'browse' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `browse` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'os' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `os` LIKE '%?%'
UNION ALL
SELECT 'sys_online_user' AS table_name, 'location' AS column_name, COUNT(*) AS bad_count FROM `sys_online_user` WHERE `location` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'leader' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `leader` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'phone' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `phone` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'path' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `path` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'create_by' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `create_by` LIKE '%?%'
UNION ALL
SELECT 'sys_org' AS table_name, 'update_by' AS column_name, COUNT(*) AS bad_count FROM `sys_org` WHERE `update_by` LIKE '%?%'
UNION ALL
SELECT 'sys_post' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_post` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_post' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_post` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_region' AS table_name, 'code' AS column_name, COUNT(*) AS bad_count FROM `sys_region` WHERE `code` LIKE '%?%'
UNION ALL
SELECT 'sys_region' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_region` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_region' AS table_name, 'first_letter' AS column_name, COUNT(*) AS bad_count FROM `sys_region` WHERE `first_letter` LIKE '%?%'
UNION ALL
SELECT 'sys_role' AS table_name, 'name' AS column_name, COUNT(*) AS bad_count FROM `sys_role` WHERE `name` LIKE '%?%'
UNION ALL
SELECT 'sys_role' AS table_name, 'scope' AS column_name, COUNT(*) AS bad_count FROM `sys_role` WHERE `scope` LIKE '%?%'
UNION ALL
SELECT 'sys_role' AS table_name, 'remark' AS column_name, COUNT(*) AS bad_count FROM `sys_role` WHERE `remark` LIKE '%?%'
UNION ALL
SELECT 'sys_role' AS table_name, 'create_by' AS column_name, COUNT(*) AS bad_count FROM `sys_role` WHERE `create_by` LIKE '%?%'
UNION ALL
SELECT 'sys_role' AS table_name, 'update_by' AS column_name, COUNT(*) AS bad_count FROM `sys_role` WHERE `update_by` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'email' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `email` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'phone' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `phone` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'identity' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `identity` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'account' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `account` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'password' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `password` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'nick_name' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `nick_name` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'real_name' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `real_name` LIKE '%?%'
UNION ALL
SELECT 'vip_user' AS table_name, 'wechat_id' AS column_name, COUNT(*) AS bad_count FROM `vip_user` WHERE `wechat_id` LIKE '%?%';
