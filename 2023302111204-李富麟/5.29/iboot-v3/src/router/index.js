import store from "@/store";
import { computed, watch } from "vue";
import Main from "@msn/main/index.vue";
import Login from "@msn/login/login.vue";
import NotFound from "@msn/error/404.vue";
import Refresh from "@msn/main/Refresh.vue";
import Index from "@msn/core/index/index.vue";
import OAuthCallback from "@msn/oauth/callback.vue";
import { createRouter, createWebHashHistory } from "vue-router";

const MainName = "Main";

const router = createRouter({
  routes: [
    {
      path: "/",
      component: Main,
      name: MainName,
      children: [
        { path: "", component: Index, name: "工作台", meta: { closable: false, keepAlive: "Index" } },
        { path: "refresh", name: "refresh", component: Refresh },
        { path: "profile", component: () => import("@msn/core/admin/UserProfile.vue"), name: "个人资料", meta: { closable: true } },
        {
          path: "search/result/:type",
          component: () => import("@msn/main/search/index.vue"),
          name: "搜索结果",
          meta: { closable: true }
        },
        {
          path: "core/dict/data",
          component: () => import("@msn/core/dict/data/index.vue"),
          name: "字典数据",
          meta: { keepAlive: "DictData", closable: true }
        },
        {
          path: "/:chapters+",
          name: "404",
          component: NotFound,
          beforeEnter(to, from, next) {
            let path = to.path;
            let init = store.getters["sys/init"];
            if (!init) {
              store.commit("sys/removeTask", path);
              init = computed(() => store.getters["sys/init"]);
              watch(init, () => {
                let urlMenuMaps = store.getters["sys/urlMenuMaps"];
                let menu = urlMenuMaps[path];
                if (menu) {
                  router.push(path).catch(reason => console.error(`${reason}`));
                }
              });
              next({ path: "/" });
            } else {
              next();
            }
          }
        }
      ]
    },
    { path: "/login", component: Login, name: "登录" },
    { path: "/oauth/callback", component: OAuthCallback, name: "oauth-callback" }
  ],
  history: createWebHashHistory()
});

router.beforeResolve((to, form, next) => {
  if (to.name !== "refresh" && to.path !== "/login" && to.path !== "/oauth/callback") {
    if (form.meta && form.meta.taskBar === false) {
      store.commit("sys/switchActiveMenuTo", to.path);
    }

    if (!(to.meta && to.meta.taskBar === false)) {
      store.commit("sys/openOrSwitchTask", to);
    }
  }

  next();
});

function importLocale(path, menu, route) {
  return () => {
    let urlPath = path.split("/");
    if (urlPath.length === 2) {
      return import(`../views/${urlPath[0]}/${urlPath[1]}/index.vue`).then(item => {
        let componentName = item.default.name;
        if (!componentName) {
          console.warn(`menu component missing name: ${menu.url}`);
        }
        store.commit("sys/setRouteKeepAlive", { url: menu.url, componentName });
        return item;
      });
    } else if (urlPath.length === 3) {
      return import(`../views/${urlPath[0]}/${urlPath[1]}/${urlPath[2]}/index.vue`).then(item => {
        let componentName = item.default.name;
        if (!componentName) {
          console.warn(`menu component missing name: ${menu.url}`);
        }
        store.commit("sys/setRouteKeepAlive", { url: menu.url, componentName });
        return item;
      });
    }
    return Promise.reject(`unsupported dynamic route depth: ${path}`);
  };
}

const resolverMenuToRoutes = urlMenuMaps => {
  Object.values(urlMenuMaps).forEach(menu => {
    let { url, target } = menu;
    let split = url.split("/").filter(item => item !== "");
    let path = split.join("/");

    if (target === "_blank") {
      let resolve = router.resolve(path);
      if (resolve.name === "404") {
        let route = {
          path,
          component: null,
          name: menu.name,
          meta: { closable: true, taskBar: true, keepAlive: null, target }
        };
        route.component = importLocale(path, menu, route);
        router.addRoute(MainName, route);
      }
    } else if (split.length > 1) {
      let route = { path, component: null, name: menu.name, meta: { closable: true, taskBar: true, keepAlive: null } };
      route.component = importLocale(path, menu, route);
      router.addRoute(MainName, route);
    }
  });
};

export { resolverMenuToRoutes };
export default router;
