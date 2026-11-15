<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import {
  fetchCreateAnnouncement,
  fetchDeleteAnnouncement,
  fetchGetAnnouncementList,
  fetchOfflineAnnouncement,
  fetchPublishAnnouncement,
  fetchUpdateAnnouncement
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const statusMap: Record<number, { label: string; type: 'default' | 'success' | 'warning' }> = {
  0: { label: '草稿', type: 'default' },
  1: { label: '已发布', type: 'success' },
  2: { label: '已下架', type: 'warning' }
};

const statusOptions = Object.entries(statusMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const typeOptions = ['施工公告', '通知公告', '公示公告', '其他'].map(item => ({ label: item, value: item }));

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | null>(null);
const announcements = ref<Api.Business.Announcement[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getAnnouncements();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getAnnouncements();
  }
});

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

const columns = computed<DataTableColumns<Api.Business.Announcement>>(() => [
  { key: 'title', title: '公告标题', minWidth: 240, ellipsis: { tooltip: true } },
  { key: 'type', title: '类型', width: 110 },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row => {
      const status = statusMap[row.status];

      return h(NTag, { type: status.type, size: 'small', bordered: false }, () => status.label);
    }
  },
  { key: 'publishedAt', title: '发布时间', width: 160, render: row => formatDateTime(row.publishedAt) },
  { key: 'createdAt', title: '创建时间', width: 160, render: row => formatDateTime(row.createdAt) },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 220,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('announcement:edit')) {
          if (row.status === 1) {
            buttons.push(
              h(
                NButton,
                { size: 'small', text: true, type: 'warning', onClick: () => handleOffline(row) },
                () => '下架'
              )
            );
          } else {
            buttons.push(
              h(
                NButton,
                { size: 'small', text: true, type: 'success', onClick: () => handlePublish(row) },
                () => '发布'
              )
            );
          }

          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('announcement:delete')) {
          buttons.push(
            h(
              NPopconfirm,
              { onPositiveClick: () => handleDelete(row) },
              {
                trigger: () => h(NButton, { size: 'small', text: true, type: 'error' }, () => $t('common.delete')),
                default: () => $t('common.confirmDelete')
              }
            )
          );
        }

        return buttons;
      })
  }
]);

async function getAnnouncements() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetAnnouncementList({
      keyword: keyword.value || undefined,
      status: (statusFilter.value ?? undefined) as Api.Business.AnnouncementStatus | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    announcements.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  getAnnouncements();
}

function handleReset() {
  keyword.value = '';
  statusFilter.value = null;
  pagination.page = 1;
  getAnnouncements();
}

async function handlePublish(row: Api.Business.Announcement) {
  const { error } = await fetchPublishAnnouncement(row.id);

  if (error) return;

  window.$message?.success('公告已发布');
  await getAnnouncements();
}

async function handleOffline(row: Api.Business.Announcement) {
  const { error } = await fetchOfflineAnnouncement(row.id);

  if (error) return;

  window.$message?.success('公告已下架');
  await getAnnouncements();
}

async function handleDelete(row: Api.Business.Announcement) {
  const { error } = await fetchDeleteAnnouncement(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getAnnouncements();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive({
  title: '',
  content: '',
  type: '公告通知'
});

const formRules: FormRules = {
  title: { required: true, message: '请输入公告标题', trigger: ['blur', 'input'] },
  content: { required: true, message: '请输入公告内容', trigger: ['blur', 'input'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑公告' : '新增公告'));

function resetModel() {
  model.title = '';
  model.content = '';
  model.type = '公告通知';
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Announcement) {
  editingId.value = row.id;
  model.title = row.title;
  model.content = row.content;
  model.type = row.type;
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  const payload: Api.Business.AnnouncementSaveParams = {
    title: model.title,
    content: model.content,
    type: model.type
  };

  submitLoading.value = true;

  try {
    const { error } = editingId.value
      ? await fetchUpdateAnnouncement(editingId.value, payload)
      : await fetchCreateAnnouncement(payload);

    if (error) return;

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : $t('common.addSuccess'));
    modalVisible.value = false;
    await getAnnouncements();
  } finally {
    submitLoading.value = false;
  }
}

onMounted(getAnnouncements);
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="space-between" align="center">
          <NSpace align="center" :size="12">
            <NInput
              v-model:value="keyword"
              clearable
              placeholder="搜索公告标题 / 内容"
              class="w-240px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="statusFilter"
              :options="statusOptions"
              clearable
              placeholder="公告状态"
              class="w-130px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('announcement:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="announcements"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1000"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 680px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="公告标题" path="title">
          <NInput v-model:value="model.title" placeholder="请输入公告标题" />
        </NFormItem>
        <NFormItem label="公告类型">
          <NSelect v-model:value="model.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem label="公告内容" path="content">
          <NInput v-model:value="model.content" type="textarea" :rows="8" placeholder="请输入公告内容" />
        </NFormItem>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
