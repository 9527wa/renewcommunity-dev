<template>
  <view class="tabbar-wrap">
    <view class="tabbar">
      <view
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: active === tab.key }"
        @click="switchTab(tab.key, tab.url)"
      >
        <image class="tab-icon" :src="tab.icon" mode="aspectFit" />
        <text class="tab-label" :class="{ active: active === tab.key }">{{ tab.label }}</text>
      </view>
    </view>

    <view class="scan-btn" @click="handleScan">
      <image class="scan-icon" src="/static/home/icon-scan.png" mode="aspectFit" />
      <text class="scan-text">OCR</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue';

type TabKey = 'home' | 'complaint' | 'profile';

interface Props {
  active: TabKey;
}

const props = defineProps<Props>();

const tabs = computed(() => [
  {
    key: 'home' as const,
    label: '首页',
    url: '/pages/index/index',
    icon: props.active === 'home' ? '/static/home/tab-home-active.png' : '/static/home/tab-home.png'
  },
  {
    key: 'complaint' as const,
    label: '投诉',
    url: '/pages/complaint/index',
    icon: props.active === 'complaint' ? '/static/home/tab-complaint-active.png' : '/static/home/tab-complaint.png'
  },
  {
    key: 'profile' as const,
    label: '我的',
    url: '/pages/profile/index',
    icon: props.active === 'profile' ? '/static/home/tab-profile-active.png' : '/static/home/tab-profile.png'
  }
]);

function switchTab(key: TabKey, url: string) {
  if (key === props.active) {
    return;
  }

  uni.reLaunch({ url });
}

function handleScan() {
  uni.showToast({ title: '扫码功能建设中', icon: 'none' });
}
</script>

<style scoped>
.tabbar-wrap {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
}

.tabbar {
  display: flex;
  height: 112rpx;
  padding-bottom: env(safe-area-inset-bottom);
  background-color: #ffffff;
  border-top: 1rpx solid #eef2f7;
  box-shadow: 0 -6rpx 20rpx rgba(31, 45, 61, 0.04);
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6rpx;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-icon {
  width: 46rpx;
  height: 46rpx;
}

.tab-label {
  font-size: 22rpx;
  color: #5b6472;
}

.tab-label.active {
  color: #1677ff;
  font-weight: 600;
}

.scan-btn {
  position: absolute;
  right: 26rpx;
  bottom: calc(112rpx + env(safe-area-inset-bottom) - 44rpx);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 96rpx;
  height: 96rpx;
  background-color: #ffffff;
  border: 2rpx solid #dbeafe;
  border-radius: 50%;
  box-shadow: 0 8rpx 24rpx rgba(22, 119, 255, 0.18);
}

.scan-icon {
  width: 42rpx;
  height: 42rpx;
}

.scan-text {
  margin-top: 2rpx;
  font-size: 18rpx;
  font-weight: 600;
  color: #1677ff;
  letter-spacing: 1rpx;
}
</style>
