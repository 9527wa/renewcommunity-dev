<template>
  <view class="register-page">
    <view class="header">
      <text class="title">注册账号</text>
      <text class="subtitle">注册后可登录查看项目资料台账</text>
    </view>

    <view class="form">
      <view class="form-item">
        <text class="label">用户名</text>
        <input v-model="form.username" class="input" placeholder="3-50 位，登录时使用" />
      </view>

      <view class="form-item">
        <text class="label">密码</text>
        <input v-model="form.password" class="input" password placeholder="6-32 位" />
      </view>

      <view class="form-item">
        <text class="label">确认密码</text>
        <input v-model="form.confirmPassword" class="input" password placeholder="请再次输入密码" />
      </view>

      <view class="form-item">
        <text class="label">昵称</text>
        <input v-model="form.nickname" class="input" placeholder="选填，默认使用用户名" />
      </view>

      <view class="form-item">
        <text class="label">手机号</text>
        <input v-model="form.mobile" class="input" type="number" maxlength="20" placeholder="选填" />
      </view>

      <button class="register-btn" :loading="loading" :disabled="loading" @click="handleRegister">注册并登录</button>
    </view>

    <view class="footer">
      <text class="link" @click="goLogin">已有账号？去登录</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { register } from '@/api/auth';
import { setToken } from '@/utils/auth';

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  mobile: ''
});

const loading = ref(false);

function goLogin() {
  uni.reLaunch({ url: '/pages/login/login' });
}

function validate() {
  const username = form.username.trim();

  if (username.length < 3 || username.length > 50) {
    uni.showToast({ title: '用户名长度需在 3-50 位之间', icon: 'none' });
    return false;
  }

  if (form.password.length < 6 || form.password.length > 32) {
    uni.showToast({ title: '密码长度需在 6-32 位之间', icon: 'none' });
    return false;
  }

  if (form.password !== form.confirmPassword) {
    uni.showToast({ title: '两次输入的密码不一致', icon: 'none' });
    return false;
  }

  if (form.nickname.trim().length > 50) {
    uni.showToast({ title: '昵称长度不能超过 50', icon: 'none' });
    return false;
  }

  if (form.mobile.trim().length > 20) {
    uni.showToast({ title: '手机号长度不能超过 20', icon: 'none' });
    return false;
  }

  return true;
}

async function handleRegister() {
  if (!validate()) {
    return;
  }

  loading.value = true;

  try {
    const { token } = await register({
      username: form.username.trim(),
      password: form.password,
      nickname: form.nickname.trim() || undefined,
      mobile: form.mobile.trim() || undefined
    });

    setToken(token);
    uni.showToast({ title: '注册成功', icon: 'success' });
    setTimeout(() => {
      uni.reLaunch({ url: '/pages/index/index' });
    }, 300);
  } catch {
    // 错误提示已在 request 中统一处理
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  padding: 0 60rpx;
  box-sizing: border-box;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.header {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 48rpx;
}

.title {
  font-size: 48rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.subtitle {
  margin-top: 16rpx;
  font-size: 28rpx;
  color: #7b8794;
}

.form {
  background-color: #ffffff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 45, 61, 0.08);
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 28rpx;
}

.label {
  font-size: 28rpx;
  color: #48505e;
  margin-bottom: 12rpx;
}

.input {
  height: 88rpx;
  padding: 0 24rpx;
  font-size: 30rpx;
  background-color: #f5f7fa;
  border-radius: 12rpx;
  border: 2rpx solid #e4e8ef;
}

.register-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 16rpx;
  font-size: 32rpx;
  color: #ffffff;
  background-color: #1677ff;
  border-radius: 12rpx;
}

.footer {
  margin-top: 40rpx;
  text-align: center;
}

.link {
  font-size: 28rpx;
  color: #1677ff;
}
</style>
