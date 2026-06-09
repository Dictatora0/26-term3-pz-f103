<template>
  <div class="iv-cropper">
    <ARow :gutter="10" class="iv-cropper-img">
      <ACol span="16">
        <vueCropper class="iv-cropper-cut" ref="cropper" :img="imgUrl" :output-size="size" :output-type="outputType" :info="true" :full="full" :fixed="fixed"
                    :can-move="canMove" :can-move-box="canMoveBox" :fixed-box="fixedBox" :original="original" :fixed-number="fixedNumber"
                    :auto-crop="autoCrop" :auto-crop-width="autoCropWidth" :auto-crop-height="autoCropHeight" :center-box="centerBox"
                    @real-time="realTime" :high="high" @img-load="imgLoad" mode="cover" :max-img-size="max" @crop-moving="cropMoving">
        </vueCropper>
      </ACol>
      <ACol span="8">
        <div style="width: 220px; height: 220px">
          <div class="iv-cropper-cut iv-cut-background" :class="shape == 'circle' ? 'iv-cropper-circle' : null"
               :style="{width: `${previews['w']}px`, height: `${previews['h']}px`}">
            <div :style="previews['div']">
              <img :src="previews['url']" :style="previews['img']"/>
            </div>
          </div>
        </div>
        <div style="margin-top: 16px; text-align: center; height: 100%; padding-top: 38px">
          <ASpace direction="vertical" align="center">
            <AButton type="primary" @click="submitFile">确定上传</AButton>
            <AButton type="dashed" @click="chooseFile">选择图片</AButton>
          </ASpace>
        </div>
      </ACol>
    </ARow>
    <div class="iv-cropper-func">
      <ASpace>
        <AButton @click="startCrop" v-if="!crap" class="btn">开始</AButton>
        <AButton @click="stopCrop" v-else class="btn">停止</AButton>
        <AButton @click="clearCrop" class="btn">清除</AButton>
        <AButton @click="refreshCrop" class="btn">刷新</AButton>
        <AButton @click="changeScale(1)" v-if="isScale">放大</AButton>
        <AButton @click="changeScale(-1)" v-if="isScale">缩小</AButton>
        <AButton @click="rotateLeft" v-if="isRotate">左转</AButton>
        <AButton @click="rotateRight" v-if="isRotate">右转</AButton>
  <!--      <AButton @click="down('base64')" class="btn">下载(base64)</AButton>-->
        <AButton @click="down('blob')" v-if="isDownload">下载(blob)</AButton>
      </ASpace>
      <input type="file" style="position:absolute; clip:rect(0 0 0 0);" accept="image/png, image/jpeg, image/gif, image/jpg" @change="uploadImg($event, 1)" ref="file">
    </div>
  </div>
</template>

<script>
import {computed, reactive, ref, watch, watchEffect} from "vue";
import { VueCropper }  from "vue-cropper";
import 'vue-cropper/dist/index.css'
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "UCropper",
  props: {
    img: null, // 图片地址
    name: {default: 'image-cropper'}, // 下载文件名
    shape: {default: 'square'}, // 形状[square、circle]
    isScale: {default: true}, // 是否显示旋转
    isRotate: {default: true}, // 是否显示旋转
    isDownload: {default: false}, // 是否显示下载
    size: {default: 1},
    full: {default: false}, // 是否输出原图比例的截图
    outputType: {default: 'png'}, // 文件保存类型(jpg、webp)
    canMove: {default: true}, // 能否拖动图片
    fixedBox: {default: false}, // 固定截图框
    original: {default: true}, // 上传图片是否显示原始宽高 (针对大图 可以铺满)
    canMoveBox: {default: true}, // 是否可以拖动截图框
    autoCrop: {default: true}, // 是否自动生成截图框
    // 只有自动截图开启 宽度高度才生效
    autoCropWidth: {default: 160}, // 截图框宽度
    autoCropHeight: {default: 160}, // 截图框高度
    centerBox: {default: true}, // 截图框是否限制在图片里
    high: {default: true}, // 是否根据dpr生成适合屏幕的高清图片
    max: {default: 99999},
  },
  components: {VueCropper},
  setup(props, {attrs}) {

    let imgUrl = ref(props.img);
    let show = ref(true);
    let fixed = ref(false);
    let fixedNumber = ref([75, 34]);
    let model = ref(false);
    let modelSrc = ref('');
    let crap = ref(false);
    let previews = ref({});
    let oriFile = null;
    watch(() => props.img, (newValue) => {
      imgUrl.value = newValue;
    });

    return {imgUrl, show, fixed, fixedNumber, modelSrc, model, crap, previews, oriFile};
  },
  methods: {
    startCrop() {
      // start
      this.crap = true;
      this.$refs.cropper.startCrop();
    },
    stopCrop() {
      //  stop
      this.crap = false;
      this.$refs.cropper.stopCrop();
    },
    clearCrop() {
      this.$refs.cropper.clearCrop();
    },
    refreshCrop() {
      this.$refs.cropper.refresh();
    },
    changeScale(num) {
      num = num || 1;
      this.$refs.cropper.changeScale(num);
    },
    rotateLeft() {
      this.$refs.cropper.rotateLeft();
    },
    rotateRight() {
      this.$refs.cropper.rotateRight();
    },
    finish(type) {
      // 输出
      // var test = window.open('about:blank')
      // test.document.body.innerHTML = '图片生成中..'
      if (type === 'blob') {
        this.$refs.cropper.getCropBlob((data) => {
          let img = window.URL.createObjectURL(data);
          this.model = true;
          this.modelSrc = img;
        });
      } else {
        this.$refs.cropper.getCropData((data) => {
          this.model = true;
          this.modelSrc = data;
        });
      }
    },
    // 实时预览函数
    realTime(data) {
      this.previews = data;
    },

    finish2(type) {
      this.$refs.cropper2.getCropData((data) => {
        this.model = true;
        this.modelSrc = data;
      });
    },
    finish3(type) {
      this.$refs.cropper3.getCropData((data) => {
        this.model = true;
        this.modelSrc = data;
      });
    },
    down(type) {
      // event.preventDefault()
      let aLink = document.createElement('a');
      aLink.download = this.name;
      // 输出
      if (type === 'blob') {
        this.$refs.cropper.getCropBlob((data) => {
          aLink.href = window.URL.createObjectURL(data);
          aLink.click();
        });
      } else { // base64 下载
        this.$refs.cropper.getCropData((data) => {
          aLink.href = data;
          aLink.click();
        });
      }

      aLink.remove();
    },
    chooseFile() {
      this.$refs['file'].click();
    },
    submitFile() {
      this.$refs.cropper.getCropBlob((data) => {
        this.$emit("finished", {data, file: this.oriFile})
      });
    },
    uploadImg(e, num) {
      //上传图片
      // this.option.img
      let file = e.target.files[0];
      if(!file) {
        return false;
      }

      this.oriFile = file;
      if (!/\.(gif|jpg|jpeg|png|bmp|GIF|JPG|PNG)$/.test(e.target.value)) {
        this.$msg.warn('图片类型必须是.gif,jpeg,jpg,png,bmp中的一种')
        return false;
      }

      let reader = new FileReader();
      reader.onload = (e) => {
        let data;
        if (typeof e.target.result === 'object') {
          // 把Array Buffer转化为blob 如果是base64不需要
          data = window.URL.createObjectURL(new Blob([e.target.result]));
        } else {
          data = e.target.result;
        }

        this.imgUrl = data;
      };
      // 转化为base64
      // reader.readAsDataURL(file)
      // 转化为blob
      reader.readAsArrayBuffer(file);
    },
    imgLoad(msg) {

    },
    cropMoving(data) {

    },
  }
}
</script>

<style scoped>
.iv-cropper-img {
  height: 420px;
}
.iv-cropper-cut {
  overflow: hidden;
  margin: 0px auto;
  border: 1px solid #dedede;
}
.iv-cut-background {
  /*background-image: url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABAAAAAQAQMAAAAlPW0iAAAAA3NCSVQICAjb4U/gAAAABlBMVEXMzMz////TjRV2AAAACXBIWXMAAArrAAAK6wGCiw1aAAAAHHRFWHRTb2Z0d2FyZQBBZG9iZSBGaXJld29ya3MgQ1M26LyyjAAAABFJREFUCJlj+M/AgBVhF/0PAH6/D/HkDxOGAAAAAElFTkSuQmCC)*/
}
.iv-cropper-circle {
  border-radius: 50%;
}
.iv-cropper-func {
  padding: 24px 3px;
}
</style>
