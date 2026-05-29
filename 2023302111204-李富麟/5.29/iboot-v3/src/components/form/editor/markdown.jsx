import {defineComponent, ref} from "vue";
import 'md-editor-v3/lib/style.css';
import {MdEditor} from 'md-editor-v3';

export default defineComponent({
    name: 'UMarkdown',
    props: {
        placeholder: {default: ""},
        showCodeRowNumber: {default: true}, // 显示行号
        toolbarsExclude: {default: ['github', 'mermaid', 'save', 'catalog']}
    },
    setup(props, {slots, attrs}) {

    },
    render() {
        return <MdEditor {...this.$attrs} showCodeRowNumber={this.showCodeRowNumber} placeholder={this.placeholder}
                 toolbarsExclude={this.toolbarsExclude} class="iv-markdown" v-slots={this.$slots}/>;
    }
})
