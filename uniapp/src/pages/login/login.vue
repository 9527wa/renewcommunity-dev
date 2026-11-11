<template>
  <view class="login-page">
    <view class="header">
      <text class="title">老旧小区改造</text>
      <text class="subtitle">项目资料台账管理系统</text>
    </view>

    <view class="form">
      <view class="form-item">
        <text class="label">用户名</text>
        <input v-model="form.userName" class="input" placeholder="请输入用户名" />
      </view>

      <view class="form-item">
        <text class="label">密码</text>
        <input v-model="form.password" class="input" password placeholder="请输入密码" />
      </view>

      <button class="login-btn" :loading="loading" :disabled="loading" @click="handleLogin">登录</button>
    </view>

    <view class="tip">开发环境默认账号：admin / 123456</view>
  </view>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue';
import { login } from '@/api/auth';
import { setToken } from '@/utils/auth';

const form = reactive({
  userName: 'admin',
  password: '123456'
});

const loading = ref(false);

async function handleLogin() {
  if (!form.userName.trim() || !form.password.trim()) {
    uni.showToast({ title: '请输入用户名和密码', icon: 'none' });
    return;
  }

  loading.value = true;

  try {
    const { token } = await login({ userName: form.userName.trim(), password: form.password });
    setToken(token);
    uni.showToast({ title: '登录成功', icon: 'success' });
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
.login-page {
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
  margin-bottom: 80rpx;
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
  padding: 48rpx 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(31, 45, 61, 0.08);
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-bottom: 32rpx;
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

.login-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 16rpx;
  font-size: 32rpx;
  color: #ffffff;
  background-color: #1677ff;
  border-radius: 12rpx;
}

.tip {
  margin-top: 40rpx;
  text-align: center;
  font-size: 24rpx;
  color: #a0a8b5;
}
</style>
