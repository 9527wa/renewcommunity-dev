<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import {
  fetchCreateComplaint,
  fetchDeleteComplaint,
  fetchGetComplaintList,
  fetchGetProjectOptions,
  fetchHandleComplaint,
  fetchUpdateComplaint
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const complaintStatusMap: Record<number, { label: string; type: 'warning' | 'info' | 'success' }> = {
  0: { label: '待处理', type: 'warning' },
  1: { label: '处理中', type: 'info' },
  2: { label: '已办结', type: 'success' }
};

const statusOptions = Object.entries(complaintStatusMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const complaintTypes = ['施工扰民', '环境秩序', '物业维修', '安全隐患', '其他'];

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | null>(null);
const complaints = ref<Api.Business.Complaint[]>([]);
const projects = ref<Api.Business.Project[]>([]);

const projectOptions = computed(() => projects.value.map(item => ({ label: item.name, value: item.id })));

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getComplaints();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getComplaints();
  }
});

function formatDateTime(value?: string | null) {
  if (!value) {
    return '-';
  }

  return value.replace('T', ' ').slice(0, 16);
}

const columns = computed<DataTableColumns<Api.Business.Complaint>>(() => [
  { key: 'title', title: '投诉标题', minWidth: 190, ellipsis: { tooltip: true } },
  { key: 'community', title: '所属小区', width: 130, ellipsis: { tooltip: true } },
  { key: 'type', title: '类型', width: 90 },
  {
    key: 'projectName',
    title: '关联项目',
    width: 160,
    ellipsis: { tooltip: true },
    render: row => row.projectName || '-'
  },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row => {
      const status = complaintStatusMap[row.status];

      return h(NTag, { type: status.type, size: 'small', bordered: false }, () => status.label);
    }
  },
  { key: 'createdAt', title: '提交时间', width: 160, render: row => formatDateTime(row.createdAt) },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 190,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('complaint:handle')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'info', onClick: () => openHandle(row) }, () => '处理')
          );
        }

        if (hasAuth('complaint:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('complaint:delete')) {
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

async function getComplaints() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetComplaintList({
      keyword: keyword.value || undefined,
      status: (statusFilter.value ?? undefined) as Api.Business.ComplaintStatus | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    complaints.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

async function getProjects() {
  const { data, error } = await fetchGetProjectOptions();

  if (!error) {
    projects.value = data;
  }
}

function handleSearch() {
  pagination.page = 1;
  getComplaints();
}

function handleReset() {
  keyword.value = '';
  statusFilter.value = null;
  pagination.page = 1;
  getComplaints();
}

async function handleDelete(row: Api.Business.Complaint) {
  const { error } = await fetchDeleteComplaint(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getComplaints();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive({
  title: '',
  content: '',
  community: '',
  type: '其他',
  projectId: null as number | null,
  contactName: '',
  contactMobile: ''
});

const formRules: FormRules = {
  title: { required: true, message: '请输入投诉标题', trigger: ['blur', 'input'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑投诉' : '新增投诉'));

function resetModel() {
  model.title = '';
  model.content = '';
  model.community = '';
  model.type = '其他';
  model.projectId = null;
  model.contactName = '';
  model.contactMobile = '';
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Complaint) {
  editingId.value = row.id;
  model.title = row.title;
  model.content = row.content;
  model.community = row.community;
  model.type = row.type;
  model.projectId = row.projectId;
  model.contactName = row.contactName;
  model.contactMobile = row.contactMobile;
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  const payload: Api.Business.ComplaintSaveParams = {
    title: model.title,
    content: model.content,
    community: model.community,
    type: model.type,
    projectId: model.projectId,
    contactName: model.contactName,
    contactMobile: model.contactMobile
  };

  submitLoading.value = true;

  try {
    const { error } = editingId.value
      ? await fetchUpdateComplaint(editingId.value, payload)
      : await fetchCreateComplaint(payload);

    if (error) return;

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : $t('common.addSuccess'));
    modalVisible.value = false;
    await getComplaints();
  } finally {
    submitLoading.value = false;
  }
}

const handleVisible = ref(false);
const handleLoading = ref(false);
const handlingComplaint = ref<Api.Business.Complaint | null>(null);
const handleFormRef = ref<FormInst | null>(null);

const handleModel = reactive({
  status: 1 as 1 | 2,
  reply: ''
});

const handleRules: FormRules = {
  status: { required: true, type: 'number', message: '请选择处理状态', trigger: ['blur', 'change'] }
};

function openHandle(row: Api.Business.Complaint) {
  handlingComplaint.value = row;
  handleModel.status = row.status === 0 ? 1 : (row.status as 1 | 2);
  handleModel.reply = row.reply || '';
  handleVisible.value = true;
}

async function handleHandleSubmit() {
  if (!handlingComplaint.value) {
    return;
  }

  await handleFormRef.value?.validate();

  handleLoading.value = true;

  try {
    const { error } = await fetchHandleComplaint(handlingComplaint.value.id, {
      status: handleModel.status,
      reply: handleModel.reply
    });

    if (error) return;

    window.$message?.success('投诉处理已保存');
    handleVisible.value = false;
    await getComplaints();
  } finally {
    handleLoading.value = false;
  }
}

onMounted(() => {
  getComplaints();
  getProjects();
});
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
              placeholder="搜索标题 / 内容 / 小区"
              class="w-260px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="statusFilter"
              :options="statusOptions"
              clearable
              placeholder="处理状态"
              class="w-140px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('complaint:add')" type="primary" @click="handleAdd">{{ $t('common.add') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="complaints"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1040"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 640px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="投诉标题" path="title">
          <NInput v-model:value="model.title" placeholder="请输入投诉标题" />
        </NFormItem>
        <NFormItem label="投诉内容">
          <NInput v-model:value="model.content" type="textarea" :rows="3" placeholder="请输入投诉内容" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="所属小区">
              <NInput v-model:value="model.community" placeholder="如 幸福家园小区" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="投诉类型">
              <NSelect
                v-model:value="model.type"
                :options="complaintTypes.map(item => ({ label: item, value: item }))"
              />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="关联项目">
              <NSelect v-model:value="model.projectId" :options="projectOptions" clearable placeholder="可不选" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="联系人">
              <NInput v-model:value="model.contactName" placeholder="请输入联系人" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="联系电话">
              <NInput v-model:value="model.contactMobile" placeholder="请输入联系电话" />
            </NFormItem>
          </NGi>
        </NGrid>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="modalVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="submitLoading" @click="handleSubmit">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>

    <NModal v-model:show="handleVisible" preset="card" title="投诉处理" style="width: 560px">
      <NSpace vertical :size="16">
        <NAlert type="info" :bordered="false">{{ handlingComplaint?.title }}</NAlert>
        <NForm ref="handleFormRef" :model="handleModel" :rules="handleRules" label-placement="left" :label-width="90">
          <NFormItem label="处理状态" path="status">
            <NSelect
              v-model:value="handleModel.status"
              :options="[
                { label: '处理中', value: 1 },
                { label: '已办结', value: 2 }
              ]"
            />
          </NFormItem>
          <NFormItem label="处理回复">
            <NInput v-model:value="handleModel.reply" type="textarea" :rows="4" placeholder="请输入处理回复" />
          </NFormItem>
        </NForm>
      </NSpace>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="handleVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="handleLoading" @click="handleHandleSubmit">
            {{ $t('common.confirm') }}
          </NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
