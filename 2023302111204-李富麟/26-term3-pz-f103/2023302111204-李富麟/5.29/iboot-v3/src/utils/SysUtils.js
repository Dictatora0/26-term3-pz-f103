import qs from "qs";

export default {
    getCookie(cname) {
        let name = cname + "=";
        let ca = document.cookie.split(';');
        for(let i=0; i<ca.length; i++) {
            let c = ca[i].trim();
            if (c.indexOf(name)==0) return c.substring(name.length,c.length);
        }

        return "";
    },
    /**
     * 是否是生产环境
     * @return {boolean}
     */
    isProd() {
        return import.meta.env.PROD
    },
    /**
     * 克隆对象
     * @param obj
     * @return {Object} obj
     */
    clone(obj) {
        let stringify = JSON.stringify(obj);
        return JSON.parse(stringify);
    },

    /**
     * 解析url的参数
     * @param url /test?a=1&b=2
     * @return {Object} {a: "1", b: "2"}
     */
    resolverQueryOfUrl(url) {
        let queryStr = url.split('?')[1];
        return qs.parse(queryStr);
    },

    /**
     * 截取url部分
     * @param url {String}
     */
    cutUrlToUri(url) {
        let number = url.indexOf("?");
        return number > -1 ? url.substring(0, number) : url;
    },

    downloadFile(bin, fileType, fileName) {
        let blob = new Blob([bin], {type: fileType });

        let downloadElement = document.createElement("a");
        let href = window.URL.createObjectURL(blob); //创建下载的链接
        downloadElement.href = href;
        downloadElement.download = fileName; //下载后文件名

        document.body.appendChild(downloadElement);
        downloadElement.click(); //点击下载
        document.body.removeChild(downloadElement); //下载完成移除元素
        window.URL.revokeObjectURL(href); //释放掉blob对象
    },
    /**
     * 解析带有占位符[{}]的url
     * @param url /iot/test?deviceId={deviceId}
     * @param editModel 包含deviceId字段值的对象
     * @return String
     */
    resolverPlaceholderUrl(url, editModel) {
        let fields = url.match(/\{(.*)\}/g);
        fields.forEach(field => {
            let fieldStr = field.substr(1, field.length - 2);
            let fieldValue = editModel[fieldStr] == null ? '' : editModel[fieldStr];
            url = url.replace(field, fieldValue);
        })

        return url;
    },
    readBlobToJson(data, call) {
        let blob = new Blob([data], {type: 'application/json' });
        let fileReader = new FileReader();
        fileReader.readAsText(blob, "utf-8");
        fileReader.onload = () => {
            if(call instanceof Function) {
                call(JSON.parse(fileReader.result));
            }
        }
    },
    resolverOptions(data, labelField, valueField, targetLabelField, targetValueField, valueLabelMap, valueModelMap) {
        data.forEach(item => {
            item[targetLabelField] = item[labelField];
            item[targetValueField] = item[valueField];
            valueModelMap[item[targetValueField]] = item;
            valueLabelMap[item[targetValueField]] = item[targetLabelField];

            if(item.children instanceof Array) {
                this.resolverOptions(item.children, labelField, valueField
                    , targetLabelField, targetValueField, valueLabelMap, valueModelMap);
            }
        })
    }
}
