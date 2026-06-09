<template>
  <div class="oauth-callback">
    <a-card :bordered="false" class="oauth-card">
      <a-spin :spinning="loading">
        <div class="oauth-title">OAuth2 登录处理中</div>
        <div class="oauth-desc">{{ message }}</div>
      </a-spin>
    </a-card>
  </div>
</template>

<script>
import { exchangeOAuthToken } from "@/api";
import { clearAuthSession, saveAuthSession } from "@/utils/request";
import { clearPendingOAuth, getPendingOAuth } from "@/utils/oauth2";

export default {
  name: "OAuthCallback",
  data() {
    return {
      loading: true,
      message: "正在交换授权码"
    };
  },
  mounted() {
    this.finishOAuth();
  },
  methods: {
    async finishOAuth() {
      const code = this.$route.query.code;
      const state = this.$route.query.state;
      const error = this.$route.query.error;
      const pending = getPendingOAuth();

      if (error) {
        clearPendingOAuth();
        clearAuthSession();
        this.loading = false;
        this.message = `授权失败: ${error}`;
        return;
      }

      if (!code || !pending.verifier) {
        clearPendingOAuth();
        clearAuthSession();
        this.loading = false;
        this.message = "缺少授权码或 PKCE 参数";
        return;
      }

      if (pending.state && state && pending.state !== state) {
        clearPendingOAuth();
        clearAuthSession();
        this.loading = false;
        this.message = "state 校验失败";
        return;
      }

      try {
        const resp = await exchangeOAuthToken({
          code,
          codeVerifier: pending.verifier
        });
        if (resp.code !== 200) {
          throw new Error(resp.message || "授权码换 token 失败");
        }
        saveAuthSession(resp.data || {});
        clearPendingOAuth();
        this.message = "登录成功，正在跳转";
        this.$router.replace("/").finally(() => {});
      } catch (errorResp) {
        clearPendingOAuth();
        clearAuthSession();
        this.loading = false;
        this.message = errorResp?.message || errorResp?.response?.data?.message || "OAuth2 登录失败";
      }
    }
  }
};
</script>

<style scoped>
.oauth-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef4f8, #dbe7ef);
}

.oauth-card {
  width: 420px;
  border-radius: 18px;
}

.oauth-title {
  font-size: 20px;
  font-weight: 700;
  color: #18344f;
  text-align: center;
}

.oauth-desc {
  margin-top: 12px;
  text-align: center;
  color: #5c7386;
}
</style>
