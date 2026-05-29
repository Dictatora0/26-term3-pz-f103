<template>
  <div class="iv-search-result">
    <div v-if="visible" style="text-align: center; padding-top: 8px; height: 100%">
      <div style="padding: 16px 0px; color: #a0a0a0">{{resultData.title}}</div>
      <pre>
        <code>{{resultData.data}}</code>
      </pre>
    </div>
    <div v-else style="text-align: center; padding-top: 120px">
      <h3>暂无搜索数据</h3>
    </div>
  </div>
</template>

<script>
import {useStore} from "vuex";
import {ref} from "vue";

export default {
  name: "index",
  setup() {
    let store = useStore();
    let resultData = store.state.sys.searchResultData;
    let visible = ref(resultData['data'] != undefined);
    return {resultData, visible};
  }
}
</script>

<style scoped>
.iv-search-result {
  width: 100%;
  height: 100%;
  padding: 32px;
  border-radius: 5px;
  background-color: #ffffff;
}
</style>
