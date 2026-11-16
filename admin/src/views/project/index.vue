<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NProgress, NSpace, NTag } from 'naive-ui';
import { fetchCreateProject, fetchDeleteProject, fetchGetProjectList, fetchUpdateProject } from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const projectStatusMap: Record<number, { label: string; type: 'default' | 'info' | 'success' }> = {
  0: { label: '规划中', type: 'default' },
  1: { label: '施工中', type: 'info' },
  2: { label: '已完成', type: 'success' }
};

const statusOptions = Object.entries(projectStatusMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const loading = ref(false);
const keyword = ref('');
const statusFilter = ref<number | null>(null);
const projects = ref<Api.Business.Project[]>([]);

const pagination = reactive({
  page: 1,
  pageSize: 10,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  onUpdatePage(page: number) {
    pagination.page = page;
    getProjects();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getProjects();
  }
});

const columns = computed<DataTableColumns<Api.Business.Project>>(() => [
  {
    key: 'name',
    title: '项目名称',
    minWidth: 190,
    ellipsis: { tooltip: true },
    render: row =>
      h('div', {}, [
        h('div', row.name),
        row.constructionUnit
          ? h('div', { style: 'font-size: 12px; color: #9aa4b2; margin-top: 2px;' }, row.constructionUnit)
          : null
      ])
  },
  { key: 'community', title: '所属小区', width: 140, ellipsis: { tooltip: true } },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row => {
      const status = projectStatusMap[row.status];

      return h(NTag, { type: status.type, size: 'small', bordered: false }, () => status.label);
    }
  },
  {
    key: 'progress',
    title: '进度',
    width: 190,
    render: row =>
      h(NSpace, { align: 'center', size: 8, wrap: false }, () => [
        h(NProgress, {
          type: 'line',
          percentage: row.progress,
          height: 8,
          showIndicator: false,
          style: 'width: 110px'
        }),
        h('span', { style: 'width: 38px; text-align: right; font-size: 12px; color: #4b5563;' }, `${row.progress}%`)
      ])
  },
  {
    key: 'budget',
    title: '预算',
    width: 120,
    align: 'right',
    render: row => formatWan(row.budget)
  },
  {
    key: 'plan',
    title: '计划时间',
    width: 210,
    render: row => `${row.startDate || '-'} ~ ${row.endDate || '-'}`
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 140,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('project:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('project:delete')) {
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

function formatWan(value?: number) {
  if (!value) {
    return '-';
  }

  return `${(value / 10000).toFixed(1)} 万元`;
}

async function getProjects() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetProjectList({
      keyword: keyword.value || undefined,
      status: (statusFilter.value ?? undefined) as Api.Business.ProjectStatus | undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    projects.value = data.list;
    pagination.itemCount = data.total;
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  pagination.page = 1;
  getProjects();
}

function handleReset() {
  keyword.value = '';
  statusFilter.value = null;
  pagination.page = 1;
  getProjects();
}

async function handleDelete(row: Api.Business.Project) {
  const { error } = await fetchDeleteProject(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getProjects();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive({
  name: '',
  community: '',
  category: '',
  status: 0 as Api.Business.ProjectStatus,
  progress: 0,
  budgetWan: 0,
  startDate: null as string | null,
  endDate: null as string | null,
  manager: '',
  constructionUnit: '',
  description: ''
});

const formRules: FormRules = {
  name: { required: true, message: '请输入项目名称', trigger: ['blur', 'input'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑项目' : '新增项目'));

function resetModel() {
  model.name = '';
  model.community = '';
  model.category = '';
  model.status = 0;
  model.progress = 0;
  model.budgetWan = 0;
  model.startDate = null;
  model.endDate = null;
  model.manager = '';
  model.constructionUnit = '';
  model.description = '';
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Project) {
  editingId.value = row.id;
  model.name = row.name;
  model.community = row.community;
  model.category = row.category;
  model.status = row.status;
  model.progress = row.progress;
  model.budgetWan = row.budget / 10000;
  model.startDate = row.startDate;
  model.endDate = row.endDate;
  model.manager = row.manager;
  model.constructionUnit = row.constructionUnit;
  model.description = row.description;
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  const payload: Api.Business.ProjectSaveParams = {
    name: model.name,
    community: model.community,
    category: model.category,
    status: model.status,
    progress: model.progress,
    budget: Math.round(model.budgetWan * 10000 * 100) / 100,
    startDate: model.startDate,
    endDate: model.endDate,
    manager: model.manager,
    constructionUnit: model.constructionUnit,
    description: model.description
  };

  submitLoading.value = true;

  try {
    const { error } = editingId.value
      ? await fetchUpdateProject(editingId.value, payload)
      : await fetchCreateProject(payload);

    if (error) return;

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : $t('common.addSuccess'));
    modalVisible.value = false;
    await getProjects();
  } finally {
    submitLoading.value = false;
  }
}

onMounted(getProjects);
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
              placeholder="搜索项目名称 / 小区 / 负责人"
              class="w-260px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="statusFilter"
              :options="statusOptions"
              clearable
              placeholder="项目状态"
              class="w-140px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('project:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="projects"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1080"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 640px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="项目名称" path="name">
          <NInput v-model:value="model.name" placeholder="请输入项目名称" />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="所属小区">
              <NInput v-model:value="model.community" placeholder="如 幸福家园小区" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="改造类别">
              <NInput v-model:value="model.category" placeholder="如 市政管网" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="项目状态">
              <NSelect v-model:value="model.status" :options="statusOptions" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="负责人">
              <NInput v-model:value="model.manager" placeholder="请输入负责人" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="项目进度">
              <NInputNumber v-model:value="model.progress" :min="0" :max="100" class="w-full" suffix="%" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="预算">
              <NInputNumber v-model:value="model.budgetWan" :min="0" :precision="2" class="w-full" suffix="万元" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="开始日期">
              <NDatePicker
                v-model:formatted-value="model.startDate"
                value-format="yyyy-MM-dd"
                type="date"
                clearable
                class="w-full"
              />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="结束日期">
              <NDatePicker
                v-model:formatted-value="model.endDate"
                value-format="yyyy-MM-dd"
                type="date"
                clearable
                class="w-full"
              />
            </NFormItem>
          </NGi>
        </NGrid>
        <NFormItem label="项目简介">
          <NInput v-model:value="model.description" type="textarea" :rows="3" placeholder="请输入项目简介" />
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
