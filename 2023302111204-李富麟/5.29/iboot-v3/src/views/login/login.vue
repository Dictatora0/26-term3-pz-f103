<template>
  <div class="iz-login iz-login-bg">
    <div class="iz-login-card">
      <a-card :bordered="false" @keyup.enter.native="submit()">
        <img slot="title" src="/img/login_logo.png" style="margin-bottom: 12px" />
        <a-form layout="horizontal" :label-col="labelCol" :wrapper-col="wrapperCol">
          <a-form-item has-feedback v-bind="validateInfos.username">
            <a-input v-model:value="user.username" placeholder="请输入用户名" size="large">
              <template #prefix>
                <UIcon type="iz-icon-account" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item has-feedback v-bind="validateInfos.password">
            <a-input v-model:value="user.password" placeholder="请输入密码" type="password" size="large">
              <template #prefix>
                <UIcon type="iz-icon-password" />
              </template>
            </a-input>
          </a-form-item>
          <a-form-item name="captcha" v-bind="validateInfos.captcha">
            <a-input v-model:value="user.captcha" placeholder="请输入验证码" size="large" class="ivz-form-valid">
              <template #prefix>
                <UIcon type="iz-icon-validate" />
              </template>
            </a-input>
            <img v-if="loginModel.captchaImg" :src="loginModel.captchaImg" class="ivz-valid-img" @click="clickImg" />
          </a-form-item>
          <a-form-item style="margin-bottom: 2px">
            <a-button type="primary" size="large" shape="round" block :loading="loginModel.loading" @click="submit">
              登录
            </a-button>
          </a-form-item>
        </a-form>
        <div style="text-align: center; padding-top: 3px">
          <span :class="loginModel.class">{{ loginModel.errMsg }}</span>
        </div>
        <a-divider style="margin: 12px 0">其他登录方式</a-divider>
        <a-row type="flex" justify="space-between" :gutter="3">
          <a-col span="6" style="text-align: right; cursor: pointer" />
          <a-col span="6" style="text-align: center; cursor: pointer">
            <UIcon type="iz-icon-gitee" style="font-size: 28px" @click="startOAuth2Login" />
          </a-col>
          <a-col span="6" style="text-align: left; cursor: pointer" />
        </a-row>
      </a-card>
    </div>
    <div style="position: absolute; bottom: 0; text-align: center; width: 100%">
      Copyright 2020-2022 iteaj.com All Rights Reserved.
    </div>
  </div>
</template>

<script>
import { reactive } from "vue";
import { Form } from "ant-design-vue";
import { captchaUri, getCode, oauth2Login } from "@/api";
import CoreConsts from "@/components/CoreConsts";

export default {
  name: "login",
  data() {
    return {
      form: null,
      iconStyle: { fontSize: "36px", cursor: "pointer" }
    };
  },
  setup() {
    const labelCol = { span: 0 };
    const wrapperCol = { span: 24 };
    const user = reactive({
      code: null,
      captcha: null,
      username: "admin",
      password: "admin123456"
    });
    const rules = reactive({
      username: [{ type: "string", required: true, message: "" }],
      password: [{ type: "string", required: true, message: "" }]
    });

    const loginModel = reactive({
      loading: false,
      errMsg: "",
      izCaptcha: false,
      captchaImg: null,
      class: "iz-login-tip-error"
    });
    const { validateInfos, validate } = Form.useForm(user, rules);

    const clickImg = () => {
      getCode().then(({ message, code, data }) => {
        if (code === CoreConsts.SuccessCode) {
          user.code = data;
          loginModel.captchaImg = `${captchaUri}?code=${data}`;
        } else {
          loginModel.errMsg = message;
        }
      });
    };

    clickImg();
    return { user, labelCol, wrapperCol, validateInfos, loginModel, clickImg, validate };
  },
  methods: {
    async startOAuth2Login() {
      await oauth2Login();
    },
    submit() {
      this.validate().then(() => {
        this.loginModel.loading = true;
        oauth2Login({
          username: this.user.username,
          password: this.user.password
        })
          .catch(error => {
            this.clickImg();
            this.loginModel.errMsg = error?.message || "登录失败";
          })
          .finally(() => {
            this.loginModel.loading = false;
          });
      });
    }
  }
};
</script>

<style>
.iz-login-bg {
  width: 100%;
  height: 100%;
  overflow-y: hidden;
  background-attachment: fixed;
  background-repeat: no-repeat;
  background-size: cover;
  background-image: url("/img/login.jpg");
}
.iz-login-card {
  top: 18%;
  right: 10%;
  width: 342px;
  position: absolute;
  background-color: rgba(255, 255, 255, 0);
}
.iz-login .ant-input {
  padding-left: 12px !important;
}
.iz-login .ant-input.ant-input-lg {
  border-radius: 20px;
}
.iz-login .ivz-form-valid {
  width: 158px;
  text-align: center;
}
.iz-login .ivz-valid-img {
  height: 40px;
  width: 118px;
  cursor: pointer;
  border-radius: 6px;
  margin-left: 16px;
  vertical-align: top;
}
.iz-login-card .ant-card {
  background-color: rgba(255, 255, 255, 0);
}
.iz-login-card .ant-card-body {
  padding: 8px 24px 16px;
  text-align: center;
  border-radius: 12px;
  background-color: #ffffffb5;
}
.iz-login-card .ant-card-head {
  line-height: 48px;
  background-color: #ffffffd4;
  border-radius: 16px 16px 0 0;
}
.iz-login-card .ant-card-head-title {
  padding: 0;
  text-align: center;
}
.iz-login-tip {
  text-align: center;
}
.iz-login-tip-error {
  color: #ed4014;
}
.iz-login-tip-success {
  color: #19be6b;
}
</style>
