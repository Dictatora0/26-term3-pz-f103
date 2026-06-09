<template>
  <div style="display: none">
    <div ref="content">
      <slot />
    </div>
  </div>
</template>

<script>
import {inject, ref} from "vue";

export default {
  name: "AMarkerLabel",
  props: {
    offset: {type: Array},
    direction: {default: 'top'},
  },
  setup() {
    let content = ref();
    /**
     * @type {{register: (config) => {}, updated: (config) => {}
     */
    let callback = inject("AMARKER");
    return {callback, content};
  },
  mounted() {
    this.callback.register({
      offset: this.offset,
      content: this.content.outerHTML,
      direction: this.direction,
    })
  },
  updated() {
    this.callback.updated({
      offset: this.offset,
      content: this.content.outerHTML,
      direction: this.direction,
    });
  }
}
</script>

<style scoped>

</style>