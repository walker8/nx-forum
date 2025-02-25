import { ID_INJECTION_KEY } from "element-plus";

export default defineNuxtPlugin((nuxtApp) => {
  nuxtApp.vueApp.provide(ID_INJECTION_KEY, {
    prefix: 1000, // 服务器端使用固定前缀
    current: 0,
  });
}); 