import {DELETE, GET, PATCH, POST, PUT} from "@/utils/request";

export const FuncNameMeta = {ADD: 'ADD', DEL: 'DEL', EDIT: 'EDIT', QUERY: 'QUERY'
    , IMPORT: 'IMPORT', DOWNLOAD: 'DOWNLOAD', EXPORT: 'EXPORT', CANCEL: 'CANCEL'
    , RESET: 'RESET', EXPAND: 'EXPAND', SUBMIT: 'SUBMIT', DETAIL: 'DETAIL'}

const TypeMethodMaps = {
    GET: GET,
    PUT: PUT,
    POST: POST,
    PATCH: PATCH,
    DELETE: DELETE
}

function getMetaValue(keyPath, model) {
    if(keyPath.length == 0 || !model) {
        return null;
    }

    let temp = model;
    for(let i=0; i<= keyPath.length - 1; i++) {
        temp = temp[keyPath[i]]
        if(temp == undefined) return temp;
    }

    return temp;
}

function setMetaValue(keyPath, model, value) {
    let temp = model;
    for(let i=0; i < keyPath.length - 1; i++) {
        if(!temp[keyPath[i]]) {
            temp = temp[keyPath[i]] = {}
        } else {
            temp = temp[keyPath[i]];
        }
    }

    temp[keyPath[keyPath.length - 1]] = value;
}

function removeMeta(keyPath, model) {

}

function initMetaValue(keyPath, model, value) {
    let temp = model;
    for(let i=0; i < keyPath.length - 1; i++) {
        if(!temp[keyPath[i]]) {
            temp = temp[keyPath[i]] = {}
        } else {
            temp = temp[keyPath[i]];
        }
    }

    if(temp[keyPath[keyPath.length - 1]] == null) {
        temp[keyPath[keyPath.length - 1]] = value;
    }
}

const MetaConst = {
    EditFormType: 'edit',
    SearchFormType: 'search',

    DefaultLabelField: 'label',
    DefaultValueField: 'value',

    SuccessCode: 200, // http请求成功码
}

export {getMetaValue, TypeMethodMaps, initMetaValue, setMetaValue, MetaConst, removeMeta}
