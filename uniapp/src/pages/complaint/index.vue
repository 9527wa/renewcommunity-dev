<template>
  <view class="page">
    <view class="hero">
      <text class="hero-title">投诉建议</text>
      <text class="hero-sub">提交小区问题反馈，查看处理进度</text>
    </view>

    <view class="content">
      <view class="card">
        <view class="card-head">
          <text class="card-title">提交投诉</text>
        </view>

        <view class="form-item">
          <text class="label">投诉标题</text>
          <input v-model="form.title" class="input" maxlength="100" placeholder="请输入投诉标题" />
        </view>

        <view class="form-item">
          <text class="label">投诉类型</text>
          <picker :range="typeOptions" @change="onTypeChange">
            <view class="picker">
              <text>{{ form.type }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">关联项目</text>
          <picker :range="projectLabels" @change="onProjectChange">
            <view class="picker">
              <text>{{ projectLabel }}</text>
              <text class="picker-arrow">›</text>
            </view>
          </picker>
        </view>

        <view class="form-item">
          <text class="label">投诉内容</text>
          <textarea v-model="form.content" class="textarea" maxlength="1000" placeholder="请详细描述遇到的问题" />
        </view>

        <view class="form-item">
          <text class="label">联系人</text>
          <input v-model="form.contactName" class="input" maxlength="50" placeholder="选填，默认使用账号昵称" />
        </view>

        <view class="form-item">
          <text class="label">联系电话</text>
          <input v-model="form.contactMobile" class="input" type="number" maxlength="20" placeholder="选填" />
        </view>

        <button class="submit-btn" :loading="submitting" :disabled="submitting" @click="handleSubmit">
          提交投诉
        </button>
      </view>

      <view class="card">
        <view class="card-head">
          <text class="card-title">我的投诉</text>
          <text class="refresh" @click="loadMyComplaints">刷新</text>
        </view>

        <view v-for="item in complaints" :key="item.id" class="complaint">
          <view class="complaint-head">
            <text class="complaint-title">{{ item.title }}</text>
            <text class="status" :class="statusClass(item.status)">{{ statusText(item.status) }}</text>
          </view>
          <text class="complaint-desc">{{ item.content }}</text>
          <view class="meta">
            <text>{{ item.projectName || '未关联项目' }}</text>
            <text>{{ formatDate(item.createdAt) }}</text>
          </view>
          <view v-if="item.reply" class="reply">处理回复：{{ item.reply }}</view>
        </view>

        <view v-if="!complaints.length && !loading" class="empty">暂无投诉记录</view>
      </view>
    </view>

    <tab-bar active="complaint" />
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { fetchMyComplaints, fetchProjectList, submitComplaint } from '@/api/app';
import type { AppComplaint, AppProject } from '@/api/app';
import TabBar from '@/components/tab-bar/tab-bar.vue';
import { clearToken, isLoggedIn } from '@/utils/auth';

interface PickerEvent {
  detail: {
    value: string;
  };
}

const typeOptions = ['施工扰民', '环境秩序', '物业维修', '安全隐患', '其他'];

const form = reactive({
  title: '',
  content: '',
  type: '其他',
  projectId: null as number | null,
  community: '',
  contactName: '',
  contactMobile: ''
});

const projects = ref<AppProject[]>([]);
const complaints = ref<AppComplaint[]>([]);
const submitting = ref(false);
const loading = ref(false);
let pendingProjectId: number | null = null;

const projectChoices = computed(() => [
  { id: 0, name: '不关联项目', community: '' },
  ...projects.value.map(item => ({ id: item.id, name: item.name, community: item.community }))
]);

const projectLabels = computed(() => projectChoices.value.map(item => item.name));

const projectLabel = computed(
  () => projectChoices.value.find(item => item.id === (form.projectId ?? 0))?.name ?? '不关联项目'
);

function statusText(status: number) {
  if (status === 0) {
    return '待处理';
  }

  if (status === 1) {
    return '处理中';
  }

  return '已办结';
}

function statusClass(status: number) {
  if (status === 0) {
    return 'status-pending';
  }

  if (status === 1) {
    return 'status-processing';
  }

  return 'status-done';
}

function formatDate(value?: string | null) {
  return value ? value.replace('T', ' ').slice(0, 16) : '';
}

function onTypeChange(event: PickerEvent) {
  form.type = typeOptions[Number(event.detail.value)] ?? '其他';
}

function onProjectChange(event: PickerEvent) {
  const choice = projectChoices.value[Number(event.detail.value)];

  if (!choice) {
    return;
  }

  form.projectId = choice.id || null;
  form.community = choice.community || '';
}

async function loadProjects() {
  try {
    const page = await fetchProjectList({ pageNum: 1, pageSize: 100 });
    projects.value = page.list;
  } catch {
    // 错误提示由 request 统一处理
  }
}

async function loadMyComplaints() {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const page = await fetchMyComplaints({ pageNum: 1, pageSize: 20 });
    complaints.value = page.list;
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    loading.value = false;
  }
}

async function handleSubmit() {
  if (!form.title.trim()) {
    uni.showToast({ title: '请输入投诉标题', icon: 'none' });
    return;
  }

  if (!form.content.trim()) {
    uni.showToast({ title: '请输入投诉内容', icon: 'none' });
    return;
  }

  submitting.value = true;

  try {
    await submitComplaint({
      title: form.title.trim(),
      content: form.content.trim(),
      type: form.type,
      projectId: form.projectId,
      community: form.community || undefined,
      contactName: form.contactName.trim() || undefined,
      contactMobile: form.contactMobile.trim() || undefined
    });

    uni.showToast({ title: '提交成功', icon: 'success' });
    form.title = '';
    form.content = '';
    form.contactName = '';
    form.contactMobile = '';
    await loadMyComplaints();
  } catch {
    // 错误提示由 request 统一处理
  } finally {
    submitting.value = false;
  }
}

function redirectToLogin() {
  clearToken();
  uni.reLaunch({ url: '/pages/login/login' });
}

onLoad(options => {
  const projectId = Number(options?.projectId);

  if (projectId) {
    pendingProjectId = projectId;
  }
});

onShow(async () => {
  if (!isLoggedIn()) {
    redirectToLogin();
    return;
  }

  await loadProjects();

  if (pendingProjectId) {
    const target = projects.value.find(item => item.id === pendingProjectId);

    if (target) {
      form.projectId = target.id;
      form.community = target.community;
    }

    pendingProjectId = null;
  }

  loadMyComplaints();
});
</script>

<style scoped>
.page {
  min-height: 100vh;
  box-sizing: border-box;
  background-color: #eef2f7;
}

.hero {
  display: flex;
  flex-direction: column;
  padding: calc(40rpx + var(--status-bar-height)) 36rpx 56rpx;
  background: linear-gradient(180deg, #3d92ef 0%, #5aa6f3 55%, #86c0f9 100%);
}

.hero-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #ffffff;
}

.hero-sub {
  margin-top: 12rpx;
  font-size: 25rpx;
  color: rgba(255, 255, 255, 0.86);
}

.content {
  margin-top: -32rpx;
  padding: 0 24rpx 200rpx;
}

.card {
  margin-bottom: 24rpx;
  padding: 28rpx 24rpx;
  background-color: #ffffff;
  border-radius: 20rpx;
  box-shadow: 0 8rpx 24rpx rgba(31, 45, 61, 0.05);
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8rpx;
}

.card-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.refresh {
  font-size: 24rpx;
  color: #1677ff;
}

.form-item {
  display: flex;
  flex-direction: column;
  margin-top: 22rpx;
}

.label {
  margin-bottom: 10rpx;
  font-size: 26rpx;
  color: #48505e;
}

.input,
.picker,
.textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 0 22rpx;
  font-size: 27rpx;
  background-color: #f5f7fa;
  border: 2rpx solid #e4e8ef;
  border-radius: 12rpx;
}

.input {
  height: 84rpx;
}

.picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 84rpx;
  color: #1f2d3d;
}

.picker-arrow {
  font-size: 28rpx;
  color: #9aa4b2;
}

.textarea {
  height: 200rpx;
  padding: 18rpx 22rpx;
  line-height: 1.6;
}

.submit-btn {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  margin-top: 36rpx;
  font-size: 30rpx;
  color: #ffffff;
  background: linear-gradient(135deg, #3b9cf7 0%, #1677ff 100%);
  border-radius: 44rpx;
  box-shadow: 0 8rpx 20rpx rgba(22, 119, 255, 0.24);
}

.complaint {
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f1f4f8;
}

.complaint:last-child {
  border-bottom: none;
}

.complaint-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.complaint-title {
  flex: 1;
  font-size: 28rpx;
  font-weight: 600;
  color: #1f2d3d;
}

.status {
  flex-shrink: 0;
  margin-left: 16rpx;
  padding: 4rpx 14rpx;
  border-radius: 16rpx;
  font-size: 22rpx;
}

.status-pending {
  background-color: #fef3c7;
  color: #d97706;
}

.status-processing {
  background-color: #eaf3ff;
  color: #1677ff;
}

.status-done {
  background-color: #e9f8ef;
  color: #16a34a;
}

.complaint-desc {
  display: block;
  margin-top: 12rpx;
  font-size: 24rpx;
  line-height: 1.6;
  color: #8b95a4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12rpx;
  font-size: 22rpx;
  color: #9aa4b2;
}

.reply {
  margin-top: 14rpx;
  padding: 16rpx 20rpx;
  border-radius: 12rpx;
  background-color: #f5f9ff;
  font-size: 24rpx;
  line-height: 1.6;
  color: #1677ff;
}

.empty {
  padding: 60rpx 0;
  text-align: center;
  font-size: 26rpx;
  color: #9aa4b2;
}
</style>
