<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { NButton, NProgress, NSpace, NTag, NText } from 'naive-ui';
import { fetchGetDashboard } from '@/service/api';
import { $t } from '@/locales';

const router = useRouter();

const loading = ref(false);
const dashboard = ref<Api.Business.Dashboard | null>(null);

const project = computed(
  () => dashboard.value?.project ?? { total: 0, planning: 0, ongoing: 0, completed: 0, averageProgress: 0 }
);
const complaint = computed(
  () => dashboard.value?.complaint ?? { total: 0, pending: 0, processing: 0, resolved: 0, thisMonth: 0 }
);
const fund = computed(() => dashboard.value?.fund ?? { budget: 0, allocated: 0, spent: 0, executionRate: 0 });
const recentProjects = computed(() => dashboard.value?.recentProjects ?? []);
const recentComplaints = computed(() => dashboard.value?.recentComplaints ?? []);

const projectStatusMap: Record<number, { label: string; type: 'default' | 'info' | 'success' }> = {
  0: { label: '规划中', type: 'default' },
  1: { label: '施工中', type: 'info' },
  2: { label: '已完成', type: 'success' }
};

const complaintStatusMap: Record<number, { label: string; type: 'warning' | 'info' | 'success' }> = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '处理中', type: 'info' },
  2: { label: '已办结', type: 'success' }
};

const resolvedRate = computed(() => ratio(complaint.value.resolved, complaint.value.total));

async function load() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetDashboard();

    if (error) return;

    dashboard.value = data;
  } finally {
    loading.value = false;
  }
}

function ratio(part?: number, total?: number) {
  if (!part || !total) {
    return 0;
  }

  return Math.round((part * 1000) / total) / 10;
}

function formatWan(value?: number) {
  if (!value) {
    return '0';
  }

  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(1)} 亿元`;
  }

  return `${(value / 10000).toFixed(1)} 万元`;
}

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

function go(path: string) {
  router.push(path);
}

onMounted(load);
</script>

<template>
  <NSpace vertical :size="16">
    <NCard :bordered="false" class="card-wrapper" size="small">
      <NSpace justify="space-between" align="center">
        <NSpace vertical :size="2">
          <NText class="text-18px font-600">工作台</NText>
          <NText depth="3" class="text-sm">项目、投诉与资金整体概览</NText>
        </NSpace>
        <NButton size="small" :loading="loading" @click="load">{{ $t('common.refresh') }}</NButton>
      </NSpace>
    </NCard>

    <NGrid :x-gap="16" :y-gap="16" cols="1 m:3" responsive="screen">
      <NGi>
        <NCard :bordered="false" class="card-wrapper h-full" title="项目统计" size="small">
          <template #header-extra>
            <NButton text type="primary" size="small" @click="go('/project')">查看全部</NButton>
          </template>
          <NGrid :cols="2" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="项目总数" :value="project.total" /></NGi>
            <NGi><NStatistic label="施工中" :value="project.ongoing" /></NGi>
            <NGi><NStatistic label="已完成" :value="project.completed" /></NGi>
            <NGi><NStatistic label="规划中" :value="project.planning" /></NGi>
          </NGrid>
          <NSpace vertical :size="10" class="mt-16px">
            <NSpace justify="space-between" align="center">
              <NText depth="3" class="text-xs">平均进度</NText>
              <NText class="text-xs font-600">{{ project.averageProgress }}%</NText>
            </NSpace>
            <NProgress type="line" :percentage="project.averageProgress" :height="8" :show-indicator="false" />
          </NSpace>
        </NCard>
      </NGi>

      <NGi>
        <NCard :bordered="false" class="card-wrapper h-full" title="投诉统计" size="small">
          <template #header-extra>
            <NButton text type="primary" size="small" @click="go('/complaint')">查看全部</NButton>
          </template>
          <NGrid :cols="2" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="投诉总数" :value="complaint.total" /></NGi>
            <NGi><NStatistic label="待处理" :value="complaint.pending" /></NGi>
            <NGi><NStatistic label="处理中" :value="complaint.processing" /></NGi>
            <NGi><NStatistic label="已办结" :value="complaint.resolved" /></NGi>
          </NGrid>
          <NSpace vertical :size="10" class="mt-16px">
            <NSpace justify="space-between" align="center">
              <NText depth="3" class="text-xs">办结率 · 本月新增 {{ complaint.thisMonth }} 件</NText>
              <NText class="text-xs font-600">{{ resolvedRate }}%</NText>
            </NSpace>
            <NProgress type="line" :percentage="resolvedRate" status="success" :height="8" :show-indicator="false" />
          </NSpace>
        </NCard>
      </NGi>

      <NGi>
        <NCard :bordered="false" class="card-wrapper h-full" title="资金统计" size="small">
          <template #header-extra>
            <NButton text type="primary" size="small" @click="go('/fund')">查看全部</NButton>
          </template>
          <NGrid :cols="2" :x-gap="12" :y-gap="12">
            <NGi><NStatistic label="预算总额" :value="formatWan(fund.budget)" /></NGi>
            <NGi><NStatistic label="已拨付" :value="formatWan(fund.allocated)" /></NGi>
            <NGi><NStatistic label="已支出" :value="formatWan(fund.spent)" /></NGi>
            <NGi><NStatistic label="结余" :value="formatWan(fund.budget - fund.spent)" /></NGi>
          </NGrid>
          <NSpace vertical :size="10" class="mt-16px">
            <NSpace justify="space-between" align="center">
              <NText depth="3" class="text-xs">预算执行率</NText>
              <NText class="text-xs font-600">{{ fund.executionRate }}%</NText>
            </NSpace>
            <NProgress
              type="line"
              :percentage="Math.min(fund.executionRate, 100)"
              :status="fund.executionRate >= 100 ? 'error' : 'warning'"
              :height="8"
              :show-indicator="false"
            />
          </NSpace>
        </NCard>
      </NGi>
    </NGrid>

    <NGrid :x-gap="16" :y-gap="16" cols="1 m:2" responsive="screen">
      <NGi>
        <NCard :bordered="false" class="card-wrapper" title="最新项目" size="small">
          <template #header-extra>
            <NButton text type="primary" size="small" @click="go('/project')">更多</NButton>
          </template>
          <NSpace vertical :size="14">
            <NText v-if="!recentProjects.length" depth="3" class="text-sm">暂无项目数据</NText>
            <NSpace v-for="item in recentProjects" :key="item.id" justify="space-between" align="center" class="py-4px">
              <NSpace vertical :size="2">
                <NText class="text-sm font-600">{{ item.name }}</NText>
                <NText depth="3" class="text-xs">{{ item.community || '-' }}</NText>
              </NSpace>
              <NSpace align="center" :size="12">
                <NTag size="small" :type="projectStatusMap[item.status].type" :bordered="false">
                  {{ projectStatusMap[item.status].label }}
                </NTag>
                <NProgress type="line" :percentage="item.progress" :show-indicator="false" :height="6" class="w-90px" />
                <NText class="text-xs">{{ item.progress }}%</NText>
              </NSpace>
            </NSpace>
          </NSpace>
        </NCard>
      </NGi>

      <NGi>
        <NCard :bordered="false" class="card-wrapper" title="最新投诉" size="small">
          <template #header-extra>
            <NButton text type="primary" size="small" @click="go('/complaint')">更多</NButton>
          </template>
          <NSpace vertical :size="14">
            <NText v-if="!recentComplaints.length" depth="3" class="text-sm">暂无投诉数据</NText>
            <NSpace
              v-for="item in recentComplaints"
              :key="item.id"
              justify="space-between"
              align="center"
              class="py-4px"
            >
              <NSpace vertical :size="2">
                <NText class="text-sm font-600">{{ item.title }}</NText>
                <NText depth="3" class="text-xs">
                  {{ item.community || '-' }} · {{ formatDateTime(item.createdAt) }}
                </NText>
              </NSpace>
              <NTag size="small" :type="complaintStatusMap[item.status].type" :bordered="false">
                {{ complaintStatusMap[item.status].label }}
              </NTag>
            </NSpace>
          </NSpace>
        </NCard>
      </NGi>
    </NGrid>
  </NSpace>
</template>

<style scoped>
.w-90px {
  width: 90px;
}

.mt-16px {
  margin-top: 16px;
}

.py-4px {
  padding: 4px 0;
}
</style>
