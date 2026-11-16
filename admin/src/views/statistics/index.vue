<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { NSpace } from 'naive-ui';
import { fetchGetStatisticsOverview } from '@/service/api';
import { $t } from '@/locales';
import { useEcharts } from '@/hooks/common/echarts';

const loading = ref(false);
const stats = ref<Api.Business.Statistics | null>(null);

const project = computed(
  () =>
    stats.value?.project ?? {
      total: 0,
      planning: 0,
      ongoing: 0,
      completed: 0,
      averageProgress: 0,
      categories: []
    }
);
const complaint = computed(
  () => stats.value?.complaint ?? { total: 0, pending: 0, processing: 0, resolved: 0, thisMonth: 0, types: [] }
);
const fund = computed(() => stats.value?.fund ?? { budget: 0, allocated: 0, spent: 0, executionRate: 0 });
const document = computed(() => stats.value?.document ?? { total: 0, archived: 0, categories: [] });
const satisfaction = computed(() => stats.value?.satisfaction ?? { total: 0, averageScore: 0, goodRate: 0 });
const progress = computed(() => stats.value?.progress ?? { total: 0, thisMonth: 0 });

const { domRef: projectChartRef, updateOptions: updateProjectChart } = useEcharts(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      type: 'pie',
      radius: ['42%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderWidth: 0 },
      label: { show: false },
      labelLine: { show: false },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

const { domRef: complaintChartRef, updateOptions: updateComplaintChart } = useEcharts(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '1%', left: 'center' },
  series: [
    {
      type: 'pie',
      radius: '62%',
      itemStyle: { borderRadius: 8, borderWidth: 0 },
      label: { show: false },
      labelLine: { show: false },
      data: [] as { name: string; value: number }[]
    }
  ]
}));

const { domRef: documentChartRef, updateOptions: updateDocumentChart } = useEcharts(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 48, right: 16, top: 24, bottom: 52 },
  xAxis: {
    type: 'category',
    data: [] as string[],
    axisLabel: { interval: 0, rotate: 24 }
  },
  yAxis: { type: 'value' },
  series: [
    {
      type: 'bar',
      data: [] as number[],
      barMaxWidth: 32,
      itemStyle: { borderRadius: [6, 6, 0, 0] }
    }
  ]
}));

function formatWan(value?: number) {
  if (!value) {
    return '0';
  }

  if (value >= 100000000) {
    return `${(value / 100000000).toFixed(1)} 亿元`;
  }

  return `${(value / 10000).toFixed(1)} 万元`;
}

async function load() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetStatisticsOverview();

    if (error) return;

    stats.value = data;

    await updateProjectChart(opts => {
      opts.series[0].data = data.project.categories.map(item => ({ name: item.name, value: item.count }));

      return opts;
    });

    await updateComplaintChart(opts => {
      opts.series[0].data = data.complaint.types.map(item => ({ name: item.name, value: item.count }));

      return opts;
    });

    await updateDocumentChart(opts => {
      opts.xAxis.data = data.document.categories.map(item => item.name);
      opts.series[0].data = data.document.categories.map(item => item.count);

      return opts;
    });
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <div>
    <NSpace vertical :size="16">
      <NCard :bordered="false" class="card-wrapper" size="small">
        <NSpace justify="space-between" align="center">
          <NSpace vertical :size="2">
            <NText class="text-18px font-600">统计分析</NText>
            <NText depth="3" class="text-sm">项目、投诉、资料、资金与满意度数据汇总</NText>
          </NSpace>
          <NButton size="small" :loading="loading" @click="load">{{ $t('common.refresh') }}</NButton>
        </NSpace>
      </NCard>

      <NGrid :x-gap="16" :y-gap="16" cols="2 s:3 l:6" responsive="screen">
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="项目总数" :value="project.total" /></NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="施工中" :value="project.ongoing" /></NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="投诉总数" :value="complaint.total" /></NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="待处理投诉" :value="complaint.pending" /></NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="资料总数" :value="document.total" /></NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" size="small"><NStatistic label="评价总数" :value="satisfaction.total" /></NCard>
        </NGi>
      </NGrid>

      <NGrid :x-gap="16" :y-gap="16" cols="1 m:2" responsive="screen">
        <NGi>
          <NCard :bordered="false" class="card-wrapper" title="项目类别分布" size="small">
            <div ref="projectChartRef" class="h-320px" />
          </NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" class="card-wrapper" title="投诉类型分布" size="small">
            <div ref="complaintChartRef" class="h-320px" />
          </NCard>
        </NGi>
      </NGrid>

      <NGrid :x-gap="16" :y-gap="16" cols="1 m:2" responsive="screen">
        <NGi>
          <NCard :bordered="false" class="card-wrapper" title="资料分类分布" size="small">
            <div ref="documentChartRef" class="h-320px" />
          </NCard>
        </NGi>
        <NGi>
          <NCard :bordered="false" class="card-wrapper" title="资金与满意度" size="small">
            <NSpace vertical :size="18">
              <NGrid :cols="2" :x-gap="12" :y-gap="12">
                <NGi><NStatistic label="预算总额" :value="formatWan(fund.budget)" /></NGi>
                <NGi><NStatistic label="已拨款" :value="formatWan(fund.allocated)" /></NGi>
                <NGi><NStatistic label="已支出" :value="formatWan(fund.spent)" /></NGi>
                <NGi><NStatistic label="预算执行率" :value="`${fund.executionRate}%`" /></NGi>
              </NGrid>
              <NProgress
                type="line"
                :percentage="Math.min(fund.executionRate, 100)"
                :show-indicator="false"
                :height="10"
              />
              <NDivider class="my-0" />
              <NGrid :cols="3" :x-gap="12">
                <NGi><NStatistic label="平均评分" :value="satisfaction.averageScore.toFixed(1)" /></NGi>
                <NGi><NStatistic label="好评率" :value="`${satisfaction.goodRate}%`" /></NGi>
                <NGi><NStatistic label="本月进度上报" :value="progress.thisMonth" /></NGi>
              </NGrid>
            </NSpace>
          </NCard>
        </NGi>
      </NGrid>
    </NSpace>
  </div>
</template>

<style scoped></style>
