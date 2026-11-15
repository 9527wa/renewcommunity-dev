<script setup lang="ts">
import { computed, h, nextTick, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules, TreeSelectOption } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';
import { fetchCreateMenu, fetchDeleteMenu, fetchGetMenuTree, fetchUpdateMenu } from '@/service/api';

const { hasAuth } = useAuth();

const loading = ref(false);
const menus = ref<Api.SystemManage.Menu[]>([]);

const typeOptions = [
  { label: '目录', value: 'M' },
  { label: '菜单', value: 'C' },
  { label: '按钮', value: 'B' }
];

const typeMap: Record<Api.SystemManage.MenuType, { label: string; type: 'default' | 'info' | 'warning' }> = {
  M: { label: '目录', type: 'default' },
  C: { label: '菜单', type: 'info' },
  B: { label: '按钮', type: 'warning' }
};

const columns = computed<DataTableColumns<Api.SystemManage.Menu>>(() => [
  { key: 'name', title: '菜单名称', width: 200, ellipsis: { tooltip: true } },
  {
    key: 'type',
    title: '类型',
    width: 90,
    align: 'center',
    render: row =>
      h(NTag, { type: typeMap[row.type].type, size: 'small', bordered: false }, () => typeMap[row.type].label)
  },
  { key: 'path', title: '路由路径', width: 160, render: row => row.path || '-' },
  { key: 'component', title: '组件路径', width: 160, render: row => row.component || '-' },
  { key: 'permission', title: '权限标识', width: 200, render: row => row.permission || '-' },
  { key: 'icon', title: '图标', width: 140, render: row => row.icon || '-' },
  { key: 'sort', title: '排序', width: 80, align: 'center' },
  {
    key: 'status',
    title: '状态',
    width: 90,
    align: 'center',
    render: row =>
      h(
        NTag,
        { type: row.status === 1 ? 'success' : 'error', size: 'small', bordered: false },
        () => (row.status === 1 ? '启用' : '停用')
      )
  },
  {
    key: 'operate',
    title: $t('common.operate'),
    width: 220,
    align: 'center',
    fixed: 'right',
    render: row =>
      h(NSpace, { justify: 'center', size: 4 }, () => {
        const buttons = [];

        if (row.type !== 'B' && hasAuth('system:menu:add')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'info', onClick: () => handleAddChild(row) }, () => '新增下级')
          );
        }

        if (hasAuth('system:menu:edit')) {
          buttons.push(
            h(
              NButton,
              { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) },
              () => $t('common.edit')
            )
          );
        }

        if (hasAuth('system:menu:delete')) {
          buttons.push(
            h(NPopconfirm, { onPositiveClick: () => handleDelete(row) }, {
              trigger: () => h(NButton, { size: 'small', text: true, type: 'error' }, () => $t('common.delete')),
              default: () => $t('common.confirmDelete')
            })
          );
        }

        return buttons;
      })
  }
]);

async function getMenus() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetMenuTree();

    if (error) return;

    menus.value = data;
  } finally {
    loading.value = false;
  }
}

async function handleDelete(row: Api.SystemManage.Menu) {
  const { error } = await fetchDeleteMenu(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getMenus();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive<Api.SystemManage.MenuSaveParams>({
  parentId: 0,
  name: '',
  type: 'C',
  path: '',
  component: '',
  permission: '',
  icon: '',
  sort: 0,
  status: 1
});

const modalTitle = computed(() => (editingId.value ? '编辑菜单' : '新增菜单'));

const formRules = computed<FormRules>(() => {
  const rules: FormRules = {
    name: { required: true, message: '请输入菜单名称', trigger: ['input', 'blur'] },
    type: { required: true, message: '请选择菜单类型', trigger: ['change', 'blur'] }
  };

  if (model.type !== 'B') {
    rules.path = { required: true, message: '请输入路由路径', trigger: ['input', 'blur'] };
  }

  return rules;
});

function toParentOptions(nodes: Api.SystemManage.Menu[], excludeId: number | null): TreeSelectOption[] {
  return nodes
    .filter(node => node.id !== excludeId && node.type !== 'B')
    .map(node => ({
      key: node.id,
      label: node.name,
      children: node.children.length ? toParentOptions(node.children, excludeId) : undefined
    }));
}

const parentOptions = computed<TreeSelectOption[]>(() => [
  { key: 0, label: '顶级菜单' },
  ...toParentOptions(menus.value, editingId.value)
]);

function resetModel() {
  Object.assign(model, {
    parentId: 0,
    name: '',
    type: 'C',
    path: '',
    component: '',
    permission: '',
    icon: '',
    sort: 0,
    status: 1
  });
}

function handleAdd() {
  editingId.value = null;
  resetModel();
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

function handleAddChild(row: Api.SystemManage.Menu) {
  editingId.value = null;
  resetModel();
  model.parentId = row.id;
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

function handleEdit(row: Api.SystemManage.Menu) {
  editingId.value = row.id;
  Object.assign(model, {
    parentId: row.parentId,
    name: row.name,
    type: row.type,
    path: row.path,
    component: row.component,
    permission: row.permission,
    icon: row.icon,
    sort: row.sort,
    status: row.status
  });
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

async function handleSubmit() {
  try {
    await formRef.value?.validate();
  } catch {
    return;
  }

  submitLoading.value = true;

  const payload: Api.SystemManage.MenuSaveParams = {
    ...model,
    path: model.type === 'B' ? '' : model.path,
    component: model.type === 'C' ? model.component : ''
  };

  try {
    if (editingId.value) {
      const { error } = await fetchUpdateMenu(editingId.value, payload);

      if (error) return;

      window.$message?.success($t('common.updateSuccess'));
    } else {
      const { error } = await fetchCreateMenu(payload);

      if (error) return;

      window.$message?.success($t('common.addSuccess'));
    }

    modalVisible.value = false;
    await getMenus();
  } finally {
    submitLoading.value = false;
  }
}

onMounted(() => {
  getMenus();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="end" align="center">
          <NButton @click="getMenus">{{ $t('common.refresh') }}</NButton>
          <NButton v-if="hasAuth('system:menu:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>
        <NDataTable
          :columns="columns"
          :data="menus"
          :loading="loading"
          :row-key="row => row.id"
          :scroll-x="1200"
          default-expand-all
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 560px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="上级菜单" path="parentId">
          <NTreeSelect v-model:value="model.parentId" :options="parentOptions" placeholder="请选择上级菜单" />
        </NFormItem>
        <NFormItem label="菜单名称" path="name">
          <NInput v-model:value="model.name" placeholder="请输入菜单名称" />
        </NFormItem>
        <NFormItem label="类型" path="type">
          <NSelect v-model:value="model.type" :options="typeOptions" />
        </NFormItem>
        <NFormItem v-if="model.type !== 'B'" label="路由路径" path="path">
          <NInput v-model:value="model.path" placeholder="如 /system/user" />
        </NFormItem>
        <NFormItem v-if="model.type === 'C'" label="组件路径" path="component">
          <NInput v-model:value="model.component" placeholder="如 system/user" />
        </NFormItem>
        <NFormItem v-if="model.type !== 'M'" label="权限标识" path="permission">
          <NInput v-model:value="model.permission" placeholder="如 system:user:list" />
        </NFormItem>
        <NFormItem label="图标" path="icon">
          <NInput v-model:value="model.icon" placeholder="如 mdi:cog" />
        </NFormItem>
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="model.sort" :min="0" class="w-full" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="model.status" :checked-value="1" :unchecked-value="0" />
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
