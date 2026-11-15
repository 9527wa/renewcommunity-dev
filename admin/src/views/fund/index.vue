<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import {
  fetchCreateFund,
  fetchDeleteFund,
  fetchGetFundList,
  fetchGetProjectOptions,
  fetchUpdateFund
} from '@/service/api';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';

const { hasAuth } = useAuth();

const fundTypeMap: Record<number, { label: string; type: 'success' | 'warning' }> = {
  1: { label: '拨款', type: 'success' },
  2: { label: '支出', type: 'warning' }
};

const typeOptions = Object.entries(fundTypeMap).map(([value, item]) => ({
  label: item.label,
  value: Number(value)
}));

const loading = ref(false);
const keyword = ref('');
const typeFilter = ref<number | null>(null);
const projectFilter = ref<number | null>(null);
const funds = ref<Api.Business.Fund[]>([]);
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
    getFunds();
  },
  onUpdatePageSize(pageSize: number) {
    pagination.pageSize = pageSize;
    pagination.page = 1;
    getFunds();
  }
});

function formatWan(value?: number) {
  if (!value) {
    return '-';
  }

  return `${(value / 10000).toFixed(1)} 万元`;
}

const columns = computed<DataTableColumns<Api.Business.Fund>>(() => [
  { key: 'name', title: '资金名称', minWidth: 200, ellipsis: { tooltip: true } },
  {
    key: 'projectName',
    title: '关联项目',
    width: 180,
    ellipsis: { tooltip: true },
    render: row => row.projectName || '-'
  },
  {
    key: 'type',
    title: '类型',
    width: 90,
    align: 'center',
    render: row => {
      const type = fundTypeMap[row.type];

      return h(NTag, { type: type.type, size: 'small', bordered: false }, () => type.label);
    }
  },
  { key: 'amount', title: '金额', width: 140, align: 'right', render: row => formatWan(row.amount) },
  { key: 'occurredAt', title: '发生日期', width: 120 },
  { key: 'remark', title: '备注', minWidth: 180, ellipsis: { tooltip: true }, render: row => row.remark || '-' },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 140,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (hasAuth('fund:edit')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) }, () =>
              $t('common.edit')
            )
          );
        }

        if (hasAuth('fund:delete')) {
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

async function getFunds() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetFundList({
      keyword: keyword.value || undefined,
      type: (typeFilter.value ?? undefined) as Api.Business.FundType | undefined,
      projectId: projectFilter.value ?? undefined,
      pageNum: pagination.page,
      pageSize: pagination.pageSize
    });

    if (error) return;

    funds.value = data.list;
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
  getFunds();
}

function handleReset() {
  keyword.value = '';
  typeFilter.value = null;
  projectFilter.value = null;
  pagination.page = 1;
  getFunds();
}

async function handleDelete(row: Api.Business.Fund) {
  const { error } = await fetchDeleteFund(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getFunds();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

function today() {
  return new Date().toISOString().slice(0, 10);
}

const model = reactive({
  name: '',
  projectId: null as number | null,
  type: 1 as Api.Business.FundType,
  amountWan: 0,
  occurredAt: today(),
  remark: ''
});

const formRules: FormRules = {
  name: { required: true, message: '请输入资金名称', trigger: ['blur', 'input'] },
  amountWan: { required: true, type: 'number', message: '请输入金额', trigger: ['blur', 'change'] },
  occurredAt: { required: true, message: '请选择发生日期', trigger: ['blur', 'change'] }
};

const modalTitle = computed(() => (editingId.value ? '编辑资金记录' : '新增资金记录'));

function resetModel() {
  model.name = '';
  model.projectId = null;
  model.type = 1;
  model.amountWan = 0;
  model.occurredAt = today();
  model.remark = '';
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
}

function handleEdit(row: Api.Business.Fund) {
  editingId.value = row.id;
  model.name = row.name;
  model.projectId = row.projectId;
  model.type = row.type;
  model.amountWan = row.amount / 10000;
  model.occurredAt = row.occurredAt;
  model.remark = row.remark;
  modalVisible.value = true;
}

async function handleSubmit() {
  await formRef.value?.validate();

  const payload: Api.Business.FundSaveParams = {
    projectId: model.projectId,
    name: model.name,
    type: model.type,
    amount: Math.round(model.amountWan * 10000 * 100) / 100,
    occurredAt: model.occurredAt,
    remark: model.remark
  };

  submitLoading.value = true;

  try {
    const { error } = editingId.value
      ? await fetchUpdateFund(editingId.value, payload)
      : await fetchCreateFund(payload);

    if (error) return;

    window.$message?.success(editingId.value ? $t('common.modifySuccess') : $t('common.addSuccess'));
    modalVisible.value = false;
    await getFunds();
  } finally {
    submitLoading.value = false;
  }
}

onMounted(() => {
  getFunds();
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
              placeholder="搜索资金名称 / 备注"
              class="w-240px"
              @keyup.enter="handleSearch"
            />
            <NSelect
              v-model:value="typeFilter"
              :options="typeOptions"
              clearable
              placeholder="资金类型"
              class="w-120px"
            />
            <NSelect
              v-model:value="projectFilter"
              :options="projectOptions"
              clearable
              filterable
              placeholder="关联项目"
              class="w-220px"
            />
            <NButton type="primary" @click="handleSearch">{{ $t('common.search') }}</NButton>
            <NButton @click="handleReset">{{ $t('common.reset') }}</NButton>
          </NSpace>
          <NButton v-if="hasAuth('fund:add')" type="primary" @click="handleAdd">{{ $t('common.add') }}</NButton>
        </NSpace>

        <NDataTable
          remote
          :columns="columns"
          :data="funds"
          :loading="loading"
          :pagination="pagination"
          :row-key="row => row.id"
          :scroll-x="1060"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 560px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="资金名称" path="name">
          <NInput v-model:value="model.name" placeholder="如 道路改造首期拨款" />
        </NFormItem>
        <NFormItem label="关联项目">
          <NSelect
            v-model:value="model.projectId"
            :options="projectOptions"
            clearable
            filterable
            placeholder="可不选"
          />
        </NFormItem>
        <NGrid :cols="2" :x-gap="16">
          <NGi>
            <NFormItem label="资金类型">
              <NSelect v-model:value="model.type" :options="typeOptions" />
            </NFormItem>
          </NGi>
          <NGi>
            <NFormItem label="金额" path="amountWan">
              <NInputNumber v-model:value="model.amountWan" :min="0" :precision="2" class="w-full" suffix="万元" />
            </NFormItem>
          </NGi>
        </NGrid>
        <NFormItem label="发生日期" path="occurredAt">
          <NDatePicker
            v-model:formatted-value="model.occurredAt"
            value-format="yyyy-MM-dd"
            type="date"
            clearable
            class="w-full"
          />
        </NFormItem>
        <NFormItem label="备注">
          <NInput v-model:value="model.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
