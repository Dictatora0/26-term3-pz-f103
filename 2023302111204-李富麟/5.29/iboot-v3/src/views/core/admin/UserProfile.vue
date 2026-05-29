<template>
  <ARow class="u-user-profile">
    <ACol span="6" class="iv-profile-detail">
      <div style="text-align: center; padding-top: 8px;">
        <ASpin :spinning="spinning">
          <a-avatar :size="160" :src="avatar" @click="avatarClick" style="cursor: pointer;">
            <template #icon><UserOutlined /></template>
          </a-avatar>
        </ASpin>
        <h2 class="u-user-name">{{user.name}}</h2>
        <div class="iv-user-desc">{{user.remark}}</div>
      </div>
      <div class="iv-user-list">
        <div class="iv-list-item">
          <UIcon type="iz-icon-dept" style="font-size: 18px; color: #a8a8a8"/>&nbsp;&nbsp;{{user['orgName']}}
        </div>
        <div class="iv-list-item">
          <UIcon type="iz-icon-post" style="font-size: 18px; color: #a8a8a8"/>&nbsp;&nbsp;{{user['postName']}}
        </div>
        <div class="iv-list-item">
          <UIcon type="iz-icon-phone" style="font-size: 18px; color: #a8a8a8"/>&nbsp;&nbsp;{{user['phone']}}
        </div>
      </div>
    </ACol>
    <ACol span="18" class="iv-profile-config">
      <div style="background: #ffffff; padding: 16px">
        <ATabs v-model:activeKey="activeKey">
          <a-tab-pane key="profile" tab="基本信息">
            <UForm v-model="user" :span="[2, 8]">
              <AFormItem label="账号">{{user['account']}}</AFormItem>
              <UInputItem field="name" label="昵称" size="large" hasFeedback @blur="() => blurSubmit('name')" :validateStatus="validStatus.name" />
              <UInputItem field="phone" label="手机" size="large" hasFeedback @blur="() => blurSubmit('phone')" :validateStatus="validStatus.phone" />
              <UInputItem field="email" label="邮箱" size="large" hasFeedback @blur="() => blurSubmit('email')" :validateStatus="validStatus.email" />
              <URadioItem field="sex" label="用户性别" dict="core_sex" hasFeedback @change="() => blurSubmit('sex')" :validateStatus="validStatus.sex"/>
              <UTextareaItem field="remark" label="个人简介" hasFeedback @blur="() => blurSubmit('remark')" :validateStatus="validStatus.remark" />
<!--              <UInputItem field="address" label="地址" />-->
            </UForm>
          </a-tab-pane>
          <a-tab-pane key="pwd" tab="修改密码">
            <UForm v-model="pwdModel" :span="[6, 12]" :rules="pwdRules" ref="pwdRef">
              <UInputPasswordItem label="原密码" field="oldPwd" />
              <UInputPasswordItem label="密码" field="password" />
              <UInputPasswordItem label="确认密码" field="surePwd" />
            </UForm>
            <div style="text-align: center">
              <AButton type="primary" @click="submitPwd">确定</AButton>
            </div>
          </a-tab-pane>
        </ATabs>
      </div>
    </ACol>
    <a-modal v-model:open="visible" :footer="null" title="裁剪图片" :width="780">
      <UCropper :img="avatar" :autoCropWidth="160" :autoCropHeight="160" @finished="finishedHandle"/>
    </a-modal>
  </ARow>
</template>
<!--https://github.com/xyxiao001/vue-cropper-->
<script>
import {
  DeploymentUnitOutlined,
  EditOutlined,
  EllipsisOutlined,
  FieldTimeOutlined,
  MailFilled,
  PhoneFilled,
  SettingOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import {mapGetters, useStore} from "vuex";
import {computed, reactive, ref, watch} from "vue";
import {urlConfig} from "@/utils/request";
import CoreConsts from "@/components/CoreConsts";
import {useRouter} from "vue-router";

export default {
    name: "UserProfile",
    components: {
        SettingOutlined, EditOutlined, EllipsisOutlined, UserOutlined
        , PhoneFilled, MailFilled, DeploymentUnitOutlined, FieldTimeOutlined
    },
    computed: {
        ...mapGetters({
            user: 'sys/user'
        })
    },
    setup () {
        let timestamp = ref(new Date().getTime());
        let user = useStore().getters['sys/user'];
        let avatar = computed(() => {
          return urlConfig.httpUrl + user.avatar + "?t=" + timestamp.value;
        })

        let validStatus = reactive({
          sex: null,
          name: null,
          email: null,
          phone: null,
          remark: null
        })

        let spinning = ref(false);
        let fileList = ref([]);
        let visible = ref(false);
        let apiUrl = urlConfig.httpUrl;
        let activeKey = ref("profile")

        let currentRoute = useRouter().currentRoute;
        watch(() => currentRoute.value.query, (newValue) => {
          activeKey.value = newValue.key
        }, {immediate: true})

        let pwdModel = ref({oldPwd: null, password: null, surePwd: null});
        let validator = (a,b,c) => {
          return new Promise((resolve, reject) => {
            if(b == null || b == '') {
              reject("请输入确认密码");
            } else if(pwdModel.value.password != pwdModel.value.surePwd) {
              reject("两次密码不一致");
            }else {
              resolve();
            }
          })
        }
        let pwdRules = {
          oldPwd: {required: true, message: '请输入原密码'},
          surePwd: {required: true, validator},
          password: {required: true, message: '请输入新密码'}
        }

        return { avatar, fileList, visible, apiUrl, spinning, timestamp, validStatus, activeKey, pwdModel, pwdRules}
    },
    methods: {
      avatarClick() {
        this.visible = true;
      },
      blurSubmit(field) {
        this.validStatus[field] = 'validating';
        this.$http.post("/core/admin/modUserInfo", this.user).then(({code, data, message}) => {
          if(code == CoreConsts.SuccessCode) {
            this.validStatus[field] = 'success';
          } else {
            this.$msg.error(message);
          }
        }).catch(reason => {
          console.error(reason);
        })
      },
      submitPwd() {
        this.$refs['pwdRef'].validateFields().then(resp => {
          this.pwdModel['id'] = this.user.id;
          this.$http.post("/core/admin/updateCurrentPwd", this.pwdModel).then(({code, message}) => {
            if(code == CoreConsts.SuccessCode) {
              this.$msg.success("修改密码成功");
            } else {
              this.$msg.error(message);
            }
          }).catch(reason => {
            this.$msg.error(reason);
          })
        })
      },
      finishedHandle({data, file}) {
        const formData = new FormData();
        formData.append("file", new File([data], 'avatar.png', {type: data.type}));

        this.visible = false;
        this.spinning = true;
        this.$http.post("/common/upload/avatar", formData, {
          headers: {
            'Content-Type': 'multipart/form-data'
          }
        }).then(response => {
          if(response['code'] == CoreConsts.SuccessCode) {
            this.user.avatar = response.data;
            this.timestamp = new Date().getTime();
            this.$http.post("/core/admin/modUserInfo", this.user).then(({code, data, message}) => {
              if(code != CoreConsts.SuccessCode) {
                this.$msg.error(message);
              }
            }).catch(reason => {
              console.error(reason);
            }).finally(() => {
              this.spinning = false;
            })
          } else {
            this.$msg.error(response['message']);
          }
        }).catch(error => {
          this.spinning = false;
          this.$msg.error(error);
        });
      }
    }
}
</script>

<style scoped>
.u-user-profile {
  padding: 16px;
  margin: 16px;
  background-color: #ffffff;
  border-radius: var(--primary-radius);
}
.u-user-name {
  font-size: 24px;
  margin-top: 20px;
  margin-bottom: 0.1em;
}
.iv-profile-detail {
  min-width: 230px;
}
.iv-profile-config {
  padding-left: 36px;
  border-left: 1px solid var(--divider-color);
}
.iv-user-list {
  margin-top: 32px;
  padding: 16px;
}
.iv-list-item {
  padding: 6px;
  font-size: 16px
}
.iv-user-desc {
  margin-top: 12px;
}
</style>
