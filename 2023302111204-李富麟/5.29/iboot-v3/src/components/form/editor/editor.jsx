import {defineComponent, onBeforeUnmount, ref, shallowRef} from "vue";
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'

export default defineComponent({
  name: 'UEditor',
  props: {},
  setup(props, {slots, attrs}) {
    // 编辑器实例，必须用 shallowRef
    const editorRef = shallowRef()

    let mode = ref('default');
    let html = ref("");
    const toolbarConfig = {};
    const editorConfig = {placeholder: '请输入内容...'};

    // 销毁编辑器对象
    onBeforeUnmount(() => {
      const editor = editorRef.value
      if (editor != null) {
        editor.destroy()
      }
    });

    const handleCreated = (editor) => {
      editorRef.value = editor // 记录 editor 实例
    }

    return {mode, editorRef, html, toolbarConfig, editorConfig, handleCreated}
  },
  render() {
    console.log('asdf')
    return <div class="iv-editor" style="overflow-y: hidden;">
            <Toolbar style="border-bottom: 1px solid #ccc" editor={this.editorRef} defaultConfig={this.toolbarConfig} mode={this.mode}/>
            <Editor {...this.$attrs} defaultConfig={this.editorConfig} mode={this.mode} onOnCreated={this.handleCreated} />
          </div>
  }
})

