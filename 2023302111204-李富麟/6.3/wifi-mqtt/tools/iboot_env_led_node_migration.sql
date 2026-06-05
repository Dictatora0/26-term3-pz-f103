START TRANSACTION;

UPDATE iot_product
SET code = 'mqtt_gateway_env_led',
    name = 'MQTT_GATEWAY_BOARD',
    status = 'enabled',
    remark = 'only for env_led_node board001'
WHERE id = 57;

UPDATE iot_product
SET code = 'env_led_node',
    name = 'ENV_LED_BOARD',
    status = 'enabled',
    remark = 'stm32 esp8266 mqtt board',
    parent_id = 57,
    protocol_code = 'MQTT_DEFAULT_IMPL'
WHERE id = 58;

UPDATE iot_device
SET name = 'MQTT_GATEWAY_CLIENT',
    pid = NULL,
    product_id = 57,
    status = 'offline',
    device_sn = 'mqtt_gateway_env_led',
    config = JSON_OBJECT(
        'qos', 0,
        'host', '127.0.0.1',
        'port', 1883,
        'topics', 'env_led_node/#',
        'clientId', 'iboot_env_led_gateway_001',
        'keepalive', 3600
    )
WHERE id = 57;

UPDATE iot_device
SET name = 'board001',
    pid = 57,
    product_id = 58,
    status = 'offline',
    device_sn = 'board001',
    config = NULL
WHERE id = 58;

DELETE FROM iot_device WHERE id NOT IN (57, 58);

UPDATE iot_model_attr
SET field = 'temperature',
    name = 'temperature',
    data_type = 'float',
    unit = 'C',
    accuracy = 1,
    remark = 'board temperature',
    attr_type = 'R',
    real_type = 'float',
    ctrl_status = 0
WHERE id = 119;

UPDATE iot_model_attr
SET field = 'humidity',
    name = 'humidity',
    data_type = 'float',
    unit = '%',
    accuracy = 1,
    remark = 'mock humidity',
    attr_type = 'R',
    real_type = 'float',
    ctrl_status = 0
WHERE id = 120;

UPDATE iot_model_attr
SET field = 'payload',
    name = 'payload',
    data_type = 'json',
    unit = NULL,
    accuracy = NULL,
    remark = 'raw json payload',
    attr_type = 'R',
    real_type = 'json',
    ctrl_status = 0
WHERE id = 121;

UPDATE iot_model_attr
SET field = 'led',
    name = 'led',
    data_type = 'int',
    unit = NULL,
    accuracy = 0,
    remark = '0 off 1 on',
    attr_type = 'RW',
    real_type = 'int',
    ctrl_status = 1
WHERE id = 122;

DELETE FROM iot_model_attr_dict WHERE model_attr_id = 122;
INSERT INTO iot_model_attr_dict(path, dict_name, dict_value, model_attr_id) VALUES
('$.params.led', 'off', '0', 122),
('$.params.led', 'on', '1', 122);

UPDATE iot_model_api
SET code = 'report_board_status',
    name = 'report_board_status',
    direct = 'subscribe',
    status = 'enabled',
    remark = 'json payload with temperature humidity led'
WHERE id = 165;

DELETE FROM iot_model_api_config WHERE api_code = 'report_temp_shidu_1';
DELETE FROM iot_model_api WHERE id = 166;

UPDATE iot_model_api
SET code = 'set_led',
    name = 'set_led',
    direct = 'publish',
    trigger_mode = 'passive',
    func_type = 'W',
    as_status = 1,
    status = 'enabled',
    remark = 'publish led control json'
WHERE id = 167;

UPDATE iot_model_api_config
SET api_code = 'report_board_status',
    value = 'env_led_node/{deviceSn}/up',
    remark = 'uplink topic'
WHERE id = 516;

UPDATE iot_model_api_config
SET api_code = 'report_board_status'
WHERE id = 517;

UPDATE iot_model_api_config
SET api_code = 'report_board_status',
    attr_field = 'payload',
    attr_name = 'payload',
    value = '@payload',
    model_attr_id = 121,
    protocol_data_type = 'json',
    protocol_attr_field = 'payload',
    protocol_attr_name = 'payload'
WHERE id = 518;

UPDATE iot_model_api_config
SET api_code = 'set_led',
    value = 'env_led_node/{deviceSn}/down',
    sort = 1,
    attr_field = NULL,
    attr_name = '主题',
    data_type = 'any',
    model_attr_id = NULL,
    protocol_data_type = 'any',
    protocol_attr_field = 'topic',
    protocol_attr_name = '主题',
    direction = 'DOWN',
    remark = 'downlink topic'
WHERE id = 522;

UPDATE iot_model_api_config
SET api_code = 'set_led',
    sort = 2,
    value = NULL,
    attr_field = 'led',
    attr_name = 'led',
    data_type = 'int',
    model_attr_id = 122,
    protocol_data_type = 'int',
    protocol_attr_field = 'led',
    protocol_attr_name = 'led',
    direction = 'DOWN',
    remark = 'led control value'
WHERE id = 523;

UPDATE iot_event_source
SET name = 'board001_data_source',
    product_ids = JSON_ARRAY(58),
    device_group_ids = JSON_ARRAY()
WHERE id = 8;

DELETE FROM iot_collect_data;
DELETE FROM iot_collect_detail;

COMMIT;
