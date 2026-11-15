<script setup lang="ts">
import { computed, h, nextTick, onMounted, reactive, ref } from 'vue';
import type { DataTableColumns, FormInst, FormRules, TreeInst, TreeOption } from 'naive-ui';
import { NButton, NPopconfirm, NSpace, NTag } from 'naive-ui';
import { $t } from '@/locales';
import { useAuth } from '@/hooks/business/auth';
import {
  fetchAssignRoleMenus,
  fetchCreateRole,
  fetchDeleteRole,
  fetchGetMenuTree,
  fetchGetRoleList,
  fetchGetRoleMenuIds,
  fetchUpdateRole
} from '@/service/api';

const { hasAuth } = useAuth();

const loading = ref(false);
const roles = ref<Api.SystemManage.Role[]>([]);
const menuTree = ref<Api.SystemManage.Menu[]>([]);

const columns = computed<DataTableColumns<Api.SystemManage.Role>>(() => [
  { key: 'code', title: '角色标识', width: 160 },
  { key: 'name', title: '角色名称', width: 160 },
  { key: 'sort', title: '排序', width: 80, align: 'center' },
  { key: 'remark', title: '备注', minWidth: 200, ellipsis: { tooltip: true }, render: row => row.remark || '-' },
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

        if (hasAuth('system:role:edit')) {
          buttons.push(
            h(
              NButton,
              { size: 'small', text: true, type: 'primary', onClick: () => handleEdit(row) },
              () => $t('common.edit')
            )
          );
        }

        if (hasAuth('system:role:assign')) {
          buttons.push(
            h(NButton, { size: 'small', text: true, type: 'info', onClick: () => openAssign(row) }, () => '分配权限')
          );
        }

        if (hasAuth('system:role:delete')) {
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

async function getRoles() {
  loading.value = true;

  try {
    const { data, error } = await fetchGetRoleList();

    if (error) return;

    roles.value = data;
  } finally {
    loading.value = false;
  }
}

async function getMenuTree() {
  const { data, error } = await fetchGetMenuTree();

  if (!error) {
    menuTree.value = data;
  }
}

async function handleDelete(row: Api.SystemManage.Role) {
  const { error } = await fetchDeleteRole(row.id);

  if (error) return;

  window.$message?.success($t('common.deleteSuccess'));
  await getRoles();
}

const modalVisible = ref(false);
const submitLoading = ref(false);
const editingId = ref<number | null>(null);
const formRef = ref<FormInst | null>(null);

const model = reactive<Api.SystemManage.RoleSaveParams>({
  code: '',
  name: '',
  sort: 0,
  status: 1,
  remark: ''
});

const modalTitle = computed(() => (editingId.value ? '编辑角色' : '新增角色'));

const formRules: FormRules = {
  code: { required: true, message: '请输入角色标识', trigger: ['input', 'blur'] },
  name: { required: true, message: '请输入角色名称', trigger: ['input', 'blur'] }
};

function handleAdd() {
  editingId.value = null;
  Object.assign(model, { code: '', name: '', sort: 0, status: 1, remark: '' });
  modalVisible.value = true;
  nextTick(() => formRef.value?.restoreValidation());
}

function handleEdit(row: Api.SystemManage.Role) {
  editingId.value = row.id;
  Object.assign(model, {
    code: row.code,
    name: row.name,
    sort: row.sort,
    status: row.status,
    remark: row.remark ?? ''
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

  try {
    if (editingId.value) {
      const { error } = await fetchUpdateRole(editingId.value, { ...model });

      if (error) return;

      window.$message?.success($t('common.updateSuccess'));
    } else {
      const { error } = await fetchCreateRole({ ...model });

      if (error) return;

      window.$message?.success($t('common.addSuccess'));
    }

    modalVisible.value = false;
    await getRoles();
  } finally {
    submitLoading.value = false;
  }
}

const assignVisible = ref(false);
const assignLoading = ref(false);
const assignRole = ref<Api.SystemManage.Role | null>(null);
const treeRef = ref<TreeInst | null>(null);
const checkedMenuKeys = ref<Array<string | number>>([]);

function toTreeOptions(nodes: Api.SystemManage.Menu[]): TreeOption[] {
  return nodes.map(node => ({
    key: node.id,
    label: node.type === 'B' ? `${node.name}（按钮）` : node.name,
    children: node.children.length ? toTreeOptions(node.children) : undefined
  }));
}

function collectLeafIds(nodes: Api.SystemManage.Menu[], ids: Set<number>) {
  nodes.forEach(node => {
    if (node.children.length) {
      collectLeafIds(node.children, ids);
    } else {
      ids.add(node.id);
    }
  });
}

const menuTreeOptions = computed(() => toTreeOptions(menuTree.value));

const leafMenuIds = computed(() => {
  const ids = new Set<number>();
  collectLeafIds(menuTree.value, ids);

  return ids;
});

async function openAssign(row: Api.SystemManage.Role) {
  assignRole.value = row;
  checkedMenuKeys.value = [];
  assignVisible.value = true;

  if (!menuTree.value.length) {
    await getMenuTree();
  }

  const { data, error } = await fetchGetRoleMenuIds(row.id);

  if (!error) {
    // 仅回填叶子节点，父节点的勾选状态由级联计算得出，避免误选整棵子树
    checkedMenuKeys.value = data.filter(id => leafMenuIds.value.has(id));
  }
}

async function handleAssign() {
  if (!assignRole.value) return;

  const checked = treeRef.value?.getCheckedData();
  const indeterminate = treeRef.value?.getIndeterminateData();

  const menuIds = Array.from(
    new Set([...(checked?.keys ?? []), ...(indeterminate?.keys ?? [])].map(Number))
  );

  assignLoading.value = true;

  try {
    const { error } = await fetchAssignRoleMenus(assignRole.value.id, menuIds);

    if (error) return;

    window.$message?.success('权限分配成功');
    assignVisible.value = false;
    await getRoles();
  } finally {
    assignLoading.value = false;
  }
}

onMounted(() => {
  getRoles();
  getMenuTree();
});
</script>

<template>
  <div>
    <NCard :bordered="false" class="card-wrapper">
      <NSpace vertical :size="16">
        <NSpace justify="end" align="center">
          <NButton @click="getRoles">{{ $t('common.refresh') }}</NButton>
          <NButton v-if="hasAuth('system:role:add')" type="primary" @click="handleAdd">
            {{ $t('common.add') }}
          </NButton>
        </NSpace>
        <NDataTable
          :columns="columns"
          :data="roles"
          :loading="loading"
          :row-key="row => row.id"
          :scroll-x="900"
        />
      </NSpace>
    </NCard>

    <NModal v-model:show="modalVisible" preset="card" :title="modalTitle" style="width: 520px">
      <NForm ref="formRef" :model="model" :rules="formRules" label-placement="left" :label-width="90">
        <NFormItem label="角色标识" path="code">
          <NInput v-model:value="model.code" placeholder="如 R_ADMIN" />
        </NFormItem>
        <NFormItem label="角色名称" path="name">
          <NInput v-model:value="model.name" placeholder="请输入角色名称" />
        </NFormItem>
        <NFormItem label="排序" path="sort">
          <NInputNumber v-model:value="model.sort" :min="0" class="w-full" />
        </NFormItem>
        <NFormItem label="状态" path="status">
          <NSwitch v-model:value="model.status" :checked-value="1" :unchecked-value="0" />
        </NFormItem>
        <NFormItem label="备注" path="remark">
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

    <NModal v-model:show="assignVisible" preset="card" title="分配权限" style="width: 480px">
      <NSpace vertical :size="12">
        <NAlert type="info" :bordered="false">当前角色：{{ assignRole?.name }}</NAlert>
        <NTree
          ref="treeRef"
          v-model:checked-keys="checkedMenuKeys"
          :data="menuTreeOptions"
          checkable
          cascade
          block-line
          default-expand-all
        />
      </NSpace>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="assignVisible = false">{{ $t('common.cancel') }}</NButton>
          <NButton type="primary" :loading="assignLoading" @click="handleAssign">{{ $t('common.confirm') }}</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped></style>
