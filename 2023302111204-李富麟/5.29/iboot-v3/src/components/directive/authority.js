import store from "@/store";
function auth(el, content, vue) {
    let auth = store.getters['sys/authMenuMap'] || {};

    // 获取所有的权限菜单
    let isArray = content.value instanceof Array;

    let arg = content.arg ? content.arg : 'and';
    let value = isArray ? content.value : [content.value];
    let args = arg.split(':');

    // e.g v-auth:and="['a:b:c', 'd:e:f']"
    if(args[0] == 'and') {
        // 有一个不包含说明没有权限 直接移除
        for(let item of value) {
            if(!auth[item]) { // 不包含
                if(args[1] == 'remove') {
                    el.remove();
                } else {
                    if(vue && vue.props.func) {
                        vue.attrs.title = "没有权限"
                        vue.props.disabled = true; // 禁用按钮
                    } else {
                        el.remove();
                    }
                }
            }
        }
    } else if(args[0] == 'or') { // e.g v-auth:or="['a:b:c', 'd:e:f']"
        let contain = false;
        // 只要包含其中一个就是有权限
        for(let item of value) {
            if(auth[item]) { // 包含
                contain = true; break;
            }
        }

        // 一个也不包含直接移除
        if(!contain) {
            if(args[1] == 'remove') {
                el.remove();
            } else {
                if(vue && vue.props.func) {
                    vue.attrs.title = "没有权限"
                    vue.props.disabled = true; // 禁用按钮
                } else {
                    el.remove();
                }
            }
        }
    } else if(args[0] == 'remove') {
        el.remove();
    } else {
        console.warn(`v-auth指令只支持[or | and | remove] 默认and`)
    }
}
/**
 * 权限指令 v-auth
 */
export default {
    created() { },
    beforeMount(el, content, a) {
        auth(el, content, a.ctx.parent);
    },
    mounted(el, content, a) {

    },
    updated(el, content, a) {
        auth(el, content, a.ctx.parent);
    },
    unmounted() { }
}
