use iboot3;
delete from iot_collect_data where id > 0;
delete from iot_collect_detail where id > 0;
delete from iot_collect_task where id > 0;
delete from iot_device where id > 0;
delete from iot_device_child where id > 0;
delete from iot_device_group where id > 0;
delete from iot_device_model where id > 0;
delete from iot_device_type where id > 0;
delete from iot_event_source where id > 0;
delete from iot_event_source_detail where id > 0;

delete from iot_group_point where id > 0;
delete from iot_model_api where id > 0;
delete from iot_model_api_config where id > 0;
delete from iot_model_attr where id > 0;
delete from iot_model_attr_dict where id > 0;
delete from iot_point_group where id > 0;
delete from iot_group_point where id > 0;
delete from iot_product where id > 0;
delete from iot_product_type where id > 0;

delete from iot_serial where id > 0;
delete from iot_signal where id > 0;

delete from sys_access_log where id > 0;
delete from sys_captcha where id > 0;
delete from sys_online_user where id > 0;