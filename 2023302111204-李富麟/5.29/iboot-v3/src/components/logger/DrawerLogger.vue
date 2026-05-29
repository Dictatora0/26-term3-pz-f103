<template>
  <UDrawer title="运行日志" placement="bottom" :maskClosable="true" height="70%">
    <URunningLogger />
  </UDrawer>
</template>
<script>
import URunningLogger from "@/components/logger/RunningLogger.vue";

export default {
  name: "UDrawerLogger",
  components: {URunningLogger}
}
</script>
<style scoped>

</style>