<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns } from 'naive-ui';
import { NButton, NSpace, NTag } from 'naive-ui';
import {
  fetchBindResidentCommunity,
  fetchGetCommunityOptions,
  fetchGetResidentBindings,
  fetchGetResidentList
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const loading = ref(false);
const keyword = ref('');
const communityFilter = ref<number | null>(null);
const residents = ref<Api.Business.Resident[]>([]);
const communities = ref<Api.Business.Community[]>([]);

const communityOptions = computed(() => communities.value.map(item => ({ label: item.name, value: item.id })));

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getResidents();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getResidents();
  }
});

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

const columns = computed<DataTableColumns<Api.Business.Resident>>(() => [
  { key: 'username', title: '用户名', width: 140, ellipsis: { tooltip: true } },
  { key: 'nickname', title: '昵称', width: 120, render: row => row.nickname || '-' },
  { key: 'mobile', title: '手机号', width: 130, render: row => row.mobile || '-' },
  {
    key: 'communityName',
    title: '绑定小区',
    width: 150,
    ellipsis: { tooltip: true },
    render: row => row.communityName || '-'
  },
  {
    key: 'status',
    title: '状态',
    width: 80,
    align: 'center',
    render: row =>
      h(NTag, { type: row.status === 1 ? 'success' : 'error', size: 'small', bordered: false }, () =>
        row.status === 1 ? '启用' : '停用'
      )
  },
  { key: 'createdAt', title: '注册时间', width: 160, render: row => formatDateTime(row.createdAt) },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 200,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [
          h(NButton, { size: 'small', text: true, type: 'info', onClick: () => openBindings(row) }, () => '绑定记录')
        ];

        if (hasAuth('resident:bind')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => openBind(row) }, () => '绑定小区')
          );
        }

        return buttons;
      })
  }
]);

async function getResidents() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetResidentList({
      keyword: keyword.value || undefined,
      communityId: communityFilter.value ?? undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    residents.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

async function getCommunities() {
  const { data, error } = await fetchGetCommunityOptions();

  if (!error) {
    communities.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getResidents();
}

function handleReset() {
  keyword.value = '';
  communityFilter.value = null;
  pagination.page = 1;
  getResidents();
}

const bindVisible = ref(false);
const bindLoading = ref(false);
const bindingResident = ref<Api.Business.Resident | null>(null);
const bindCommunityId = ref<number | null>(null);

function openBind(row: Api.Business.Resident) {
  bindingResident.value = row;
  bindCommunityId.value = row.communityId;
  bindVisible.value = true;
}

async function handleBind() {
  if (!bindingResident.value || !bindCommunityId.value) {
    window.$message?.warning('请选择要绑定的小区');
    return;
  }

  bindLoading.value = true;

  try {
    const { error } = await fetchBindResidentCommunity(bindingResident.value.id, bindCommunityId.value);

    if (error) return;

    window.$message?.success('小区绑定成功');
    bindVisible.value = false;
    await getResidents();
  } finally {
    bindLoading.value = false;
  }
}

const bindingsVisible = ref(false);
const bindingsLoading = ref(false);
const bindings = ref<Api.Business.CommunityBinding[]>([]);
const bindingsTitle = ref('');

async function openBindings(row: Api.Business.Resident) {
  bindingsTitle.value = row.nickname || row.username;
  bindings.value = [];
  bindingsVisible.value = true;
  bindingsLoading.value = true;

  try {
    const { data, error } = await fetchGetResidentBindings(row.id);

    if (!error) {
      bindings.value = data;
    }
  } finally {
    bindingsLoading.value = false;
  }
}

onMounted(() => {
  getResidents();
  getCommunities();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace align="center" :size="12">
          <NInput
            v-model:value="keyword"
            clearable
            placeholder="搜索用户名 / 昵称 / 手机号"
            class="w-260px"
            @keyup.enter="handleSearch"
          />
          <NSelect
            v-model:value="communityFilter"
            :options="communityOptions"
            clearable
            filterable
            placeholder="绑定小区"
            class="w-200px"
          />
          <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
          <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="residents"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1030"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="bindVisible" preset="card" title="绑定小区" style="width: 460px">
      <NSpace vertical :size="16">
        <NAlert type="info" :bordered="false">
          {{ bindingResident?.nickname || bindingResident?.username }}
        </NAlert>
        <NSelect v-model:value="bindCommunityId" :options="communityOptions" filterable placeholder="请选择小区" />
      </NSpace>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="bindVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="bindLoading" @click="handleBind">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="bindingsVisible" preset="card" :title="`绑定记录 · ${bindingsTitle}`" style="width: 520px">
      <NSpace v-if="bindings.length" vertical :size="12">
        <NSpace v-for="item in bindings" :key="item.id" justify="space-between" align="center" class="py-4px">
          <NSpace vertical :size="2">
            <NText class="text-sm">{{ item.communityName || '-' }}</NText>
            <NText depth="3" class="text-xs">操作人：{{ item.operatorName || '-' }}</NText>
          </NSpace>
          <NText depth="3" class="text-xs">{{ formatDateTime(item.createdAt) }}</NText>
        </NSpace>
      </NSpace>
      <NText v-else-if="!bindingsLoading" depth="3" class="text-sm">暂无绑定记录</NText>
    </NModal>
  </div>
</template>

<style scoped></style>
