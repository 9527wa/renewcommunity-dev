<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { NDescriptions, NDescriptionsItem, NProgress, NText } from 'naive-ui';
import { fetchGetServerStatus } from '@/service/api';
import { $t } from '@/locales';

const AUTO_REFRESH_INTERVAL = 5000;

const loading = ref(false);
const autoRefresh = ref(true);
const status = ref<Api.SystemManage.ServerStatus | null>(null);
const lastUpdated = ref('');
let timer: ReturnType<typeof setInterval> | null = null;

const os = computed(() => status.value?.os);
const memory = computed(() => status.value?.memory);
const jvm = computed(() => status.value?.jvm);
const app = computed(() => status.value?.app);

const refreshHint = computed(() => {
  const suffix = lastUpdated.value ? ` · 最近更新 ${lastUpdated.value}` : '';

  return autoRefresh.value ? `每 5 秒自动刷新${suffix}` : `自动刷新已暂停${suffix}`;
});

const cpuPercentage = computed(() => clampPercent(os.value?.cpuUsage));
const physicalPercentage = computed(() => clampPercent(memory.value?.physicalUsage));
const heapPercentage = computed(() => clampPercent(memory.value?.heapUsage));

function clampPercent(value?: number) {
  if (value === undefined || value < 0) {
    return 0;
  }

  return Math.min(value, 100);
}

function progressStatus(value?: number): 'default' | 'success' | 'warning' | 'error' {
  if (value === undefined || value < 0) {
    return 'default';
  }

  if (value >= 90) {
    return 'error';
  }

  if (value >= 70) {
    return 'warning';
  }

  return 'success';
}

function formatPercent(value?: number) {
  if (value === undefined || value < 0) {
    return '不可用';
  }

  return `${value.toFixed(1)}%`;
}

function formatLoad(value?: number) {
  if (value === undefined || value < 0) {
    return '不可用';
  }

  return value.toFixed(2);
}

function formatBytes(value?: number) {
  if (value === undefined || value < 0) {
    return '-';
  }

  const units = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
  let size = value;
  let index = 0;

  while (size >= 1024 && index < units.length - 1) {
    size /= 1024;
    index += 1;
  }

  return `${size.toFixed(index === 0 ? 0 : 1)} ${units[index]}`;
}

function formatDuration(seconds?: number) {
  if (seconds === undefined || seconds < 0) {
    return '-';
  }

  const days = Math.floor(seconds / 86400);
  const hours = Math.floor((seconds % 86400) / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const parts: string[] = [];

  if (days > 0) {
    parts.push(`${days} 天`);
  }

  if (hours > 0) {
    parts.push(`${hours} 小时`);
  }

  parts.push(`${minutes} 分钟`);

  return parts.join(' ');
}

function formatDateTime(timestamp?: number) {
  if (!timestamp) {
    return '-';
  }

  return new Date(timestamp).toLocaleString('zh-CN', { hour12: false });
}

async function load() {
  if (loading.value) {
    return;
  }

  loading.value = true;

  try {
    const { data, error } = await fetchGetServerStatus();

    if (error) {
      return;
    }

    status.value = data;
    lastUpdated.value = new Date().toLocaleTimeString('zh-CN', { hour12: false });
  } finally {
    loading.value = false;
  }
}

function stopTimer() {
  if (timer) {
    clearInterval(timer);
    timer = null;
  }
}

function startTimer() {
  stopTimer();
  timer = setInterval(load, AUTO_REFRESH_INTERVAL);
}

watch(autoRefresh, value => {
  if (value) {
    startTimer();
  } else {
    stopTimer();
  }
});

onMounted(() => {
  load();
  startTimer();
});

onBeforeUnmount(stopTimer);
</script>

<template>
  <NCard :bordered="false" class="card-wrapper">
    <NSpace vertical :size="16">
      <NSpace justify="space-between" align="center">
        <NText depth="3" class="text-sm">{{ refreshHint }}</NText>
        <NSpace align="center" :size="16">
          <NSpace align="center" :size="6">
            <NText depth="3" class="text-sm">自动刷新</NText>
            <NSwitch v-model:value="autoRefresh" size="small" />
          </NSpace>
          <NButton size="small" :loading="loading" @click="load">{{ $t('common.refresh') }}</NButton>
        </NSpace>
      </NSpace>

      <NGrid :x-gap="16" :y-gap="16" cols="1 s:2 m:4" responsive="screen">
        <NGi>
          <NCard size="small" title="CPU 使用率" :bordered="false" class="h-full">
            <NSpace vertical :size="12">
              <NProgress
                type="line"
                :percentage="cpuPercentage"
                :status="progressStatus(os?.cpuUsage)"
                :height="10"
                :show-indicator="false"
              />
              <NSpace justify="space-between" align="baseline">
                <NStatistic :value="formatPercent(os?.cpuUsage)" />
                <NText depth="3" class="text-xs">负载 {{ formatLoad(os?.systemLoadAverage) }}</NText>
              </NSpace>
              <NText depth="3" class="text-xs">{{ os?.availableProcessors ?? 0 }} 核处理器</NText>
            </NSpace>
          </NCard>
        </NGi>

        <NGi>
          <NCard size="small" title="物理内存" :bordered="false" class="h-full">
            <NSpace vertical :size="12">
              <NProgress
                type="line"
                :percentage="physicalPercentage"
                :status="progressStatus(memory?.physicalUsage)"
                :height="10"
                :show-indicator="false"
              />
              <NSpace justify="space-between" align="baseline">
                <NStatistic :value="formatPercent(memory?.physicalUsage)" />
                <NText depth="3" class="text-xs">空闲 {{ formatBytes(memory?.physicalFree) }}</NText>
              </NSpace>
              <NText depth="3" class="text-xs">
                {{ formatBytes(memory?.physicalUsed) }} / {{ formatBytes(memory?.physicalTotal) }}
              </NText>
            </NSpace>
          </NCard>
        </NGi>

        <NGi>
          <NCard size="small" title="JVM 堆内存" :bordered="false" class="h-full">
            <NSpace vertical :size="12">
              <NProgress
                type="line"
                :percentage="heapPercentage"
                :status="progressStatus(memory?.heapUsage)"
                :height="10"
                :show-indicator="false"
              />
              <NSpace justify="space-between" align="baseline">
                <NStatistic :value="formatPercent(memory?.heapUsage)" />
                <NText depth="3" class="text-xs">已提交 {{ formatBytes(memory?.heapCommitted) }}</NText>
              </NSpace>
              <NText depth="3" class="text-xs">
                {{ formatBytes(memory?.heapUsed) }} / {{ formatBytes(memory?.heapMax) }}
              </NText>
            </NSpace>
          </NCard>
        </NGi>

        <NGi>
          <NCard size="small" title="运行状态" :bordered="false" class="h-full">
            <NSpace vertical :size="12">
              <NStatistic :value="formatDuration(jvm?.uptimeSeconds)" />
              <NText depth="3" class="text-xs">启动于 {{ formatDateTime(jvm?.startTime) }}</NText>
              <NSpace justify="space-between" class="text-xs">
                <NText depth="3">线程 {{ jvm?.threadCount ?? 0 }}</NText>
                <NText depth="3">峰值 {{ jvm?.peakThreadCount ?? 0 }}</NText>
                <NText depth="3">守护 {{ jvm?.daemonThreadCount ?? 0 }}</NText>
              </NSpace>
            </NSpace>
          </NCard>
        </NGi>
      </NGrid>

      <NGrid :x-gap="16" :y-gap="16" cols="1 m:2" responsive="screen">
        <NGi>
          <NCard size="small" title="JVM 信息" :bordered="false">
            <NDescriptions :column="2" size="small" label-placement="left">
              <NDescriptionsItem label="Java 版本">{{ jvm?.javaVersion ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="Java 供应商">{{ jvm?.javaVendor ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="JVM 名称">{{ jvm?.jvmName ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="已加载类">{{ jvm?.loadedClassCount ?? 0 }}</NDescriptionsItem>
              <NDescriptionsItem label="非堆已用">{{ formatBytes(memory?.nonHeapUsed) }}</NDescriptionsItem>
              <NDescriptionsItem label="非堆已提交">{{ formatBytes(memory?.nonHeapCommitted) }}</NDescriptionsItem>
            </NDescriptions>
          </NCard>
        </NGi>

        <NGi>
          <NCard size="small" title="系统与应用" :bordered="false">
            <NDescriptions :column="2" size="small" label-placement="left">
              <NDescriptionsItem label="应用名称">{{ app?.name ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="激活 Profile">{{ app?.activeProfiles || '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="操作系统">{{ os?.name ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="系统架构">{{ os?.arch ?? '-' }}</NDescriptionsItem>
              <NDescriptionsItem label="系统版本" :span="2">{{ os?.version ?? '-' }}</NDescriptionsItem>
            </NDescriptions>
          </NCard>
        </NGi>
      </NGrid>
    </NSpace>
  </NCard>
</template>

<style scoped></style>
