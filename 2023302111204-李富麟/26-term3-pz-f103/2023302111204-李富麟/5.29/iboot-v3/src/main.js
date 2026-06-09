import {createApp} from 'vue'
import App from './App.vue'
import store from "@/store";
import Router from './router'
import {http, env} from "@/utils/request"
// import '@/doc' // 文档教程 不需要请注释掉
import 'ant-design-vue/dist/reset.css'
import Antd, {message, notification} from 'ant-design-vue'
import IvzComponents from '@/components'
import Authority from '@/components/directive/authority'

let app = createApp(App).use(Router).use(Antd).use(store)
    .use(IvzComponents).directive('auth', Authority)


app.config.globalProperties.$env=env
app.config.globalProperties.$http = http
app.config.globalProperties.$msg = message
app.config.globalProperties.$notify = notification
app.config.globalProperties.$apiUrl=env.http.httpUrl

app.mount('#app')
