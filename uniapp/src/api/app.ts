import { request, BASE_URL } from '@/utils/request';
import { getToken } from '@/utils/auth';

export interface PageData<T> {
  total: number;
  list: T[];
}

export interface AppAnnouncement {
  id: number;
  title: string;
  type: string;
  summary: string;
  publishedAt: string | null;
}

export interface AppAnnouncementDetail {
  id: number;
  title: string;
  content: string;
  type: string;
  publishedAt: string | null;
}

export interface AppProject {
  id: number;
  name: string;
  community: string;
  category: string;
  status: 0 | 1 | 2;
  progress: number;
  startDate: string | null;
  endDate: string | null;
  constructionUnit?: string;
  manager?: string;
  description: string;
}

export interface AppCommunity {
  id: number;
  name: string;
  address: string;
  buildingCount: number;
  householdCount: number;
}

export interface AppComplaint {
  id: number;
  title: string;
  content: string;
  type: string;
  status: 0 | 1 | 2;
  kind?: string;
  community?: string;
  location?: string;
  projectId: number | null;
  projectName?: string | null;
  reply?: string | null;
  createdAt: string;
  handledAt?: string | null;
  rated?: boolean;
  score?: number | null;
  images?: AppFileRef[];
  remindCount?: number;
  orderNo?: string;
}

export interface SatisfactionSubmitParams {
  complaintId: number;
  score: number;
  comment?: string;
}

export interface AppFileRef {
  fileId: number;
  fileName?: string | null;
  contentType?: string | null;
}

export interface AppProgress {
  id: number;
  stage: string;
  title: string;
  content: string;
  progress: number;
  reportDate: string;
  createdAt: string;
  images: AppFileRef[];
}

export interface AppProfile {
  userId: number;
  userName: string;
  username: string;
  mobile: string;
  email: string;
  roles: string[];
  communityId: number | null;
  communityName?: string | null;
  createdAt: string;
  complaintTotal: number;
  complaintPending: number;
  complaintProcessing: number;
  complaintResolved: number;
  satisfactionTotal: number;
}

export interface MySatisfaction {
  id: number;
  complaintId: number;
  complaintTitle?: string | null;
  userName: string;
  score: number;
  comment: string;
  createdAt: string;
}

export interface AppComplaintSubmitParams {
  title: string;
  content: string;
  type?: string;
  kind?: string;
  location?: string;
  projectId?: number | null;
  community?: string;
  contactName?: string;
  contactMobile?: string;
}

/** 已发布公告列表 */
export function fetchAnnouncementList(params: { keyword?: string; pageNum: number; pageSize: number }) {
  return request<PageData<AppAnnouncement>>({ url: '/app/announcement/list', data: params });
}

/** 公告详情 */
export function fetchAnnouncementDetail(id: number) {
  return request<AppAnnouncementDetail>({ url: `/app/announcement/${id}` });
}

/** 改造项目列表 */
export function fetchProjectList(params: {
  status?: number;
  community?: string;
  keyword?: string;
  pageNum: number;
  pageSize: number;
}) {
  return request<PageData<AppProject>>({ url: '/app/project/list', data: params });
}

/** 项目详情 */
export function fetchProjectDetail(id: number) {
  return request<AppProject>({ url: `/app/project/${id}` });
}

/** 小区列表 */
export function fetchCommunityList() {
  return request<AppCommunity[]>({ url: '/app/community/list' });
}

/** 提交投诉 */
export function submitComplaint(data: AppComplaintSubmitParams) {
  return request<number>({ url: '/app/complaint', method: 'POST', data });
}

/** 我的投诉 */
export function fetchMyComplaints(params: { status?: number; pageNum: number; pageSize: number }) {
  return request<PageData<AppComplaint>>({ url: '/app/complaint/my', data: params });
}

/** 提交满意度评价 */
export function submitSatisfaction(data: SatisfactionSubmitParams) {
  return request<number>({ url: '/app/satisfaction', method: 'POST', data });
}

/** 项目进度动态 */
export function fetchProjectProgress(id: number, params: { pageNum: number; pageSize: number }) {
  return request<PageData<AppProgress>>({ url: `/app/project/${id}/progress`, data: params });
}

/** 我的投诉详情 */
export function fetchComplaintDetail(id: number) {
  return request<AppComplaint>({ url: `/app/complaint/${id}` });
}

/** 我的评价 */
export function fetchMyRatings(params: { pageNum: number; pageSize: number }) {
  return request<PageData<MySatisfaction>>({ url: '/app/satisfaction/my', data: params });
}

/** 个人资料与统计 */
export function fetchProfile() {
  return request<AppProfile>({ url: '/app/profile' });
}

/** 修改个人资料 */
export function updateProfile(data: { nickname?: string; mobile?: string; email?: string }) {
  return request<void>({ url: '/app/profile', method: 'PUT', data });
}

/** 绑定 / 切换小区 */
export function bindCommunity(communityId: number) {
  return request<void>({ url: '/app/profile/community', method: 'PUT', data: { communityId } });
}

/** 上传投诉图片（返回 Promise，上传失败时 reject） */
export function uploadComplaintImage(complaintId: number, filePath: string) {
  const token = getToken();

  return new Promise<void>((resolve, reject) => {
    uni.uploadFile({
      url: `${BASE_URL}/app/complaint/${complaintId}/images`,
      filePath,
      name: 'files',
      header: token ? { satoken: token } : {},
      success: res => {
        let body: { code?: number; msg?: string } = {};

        try {
          body = JSON.parse(res.data || '{}');
        } catch {
          // 非 JSON 响应按失败处理
        }

        if (res.statusCode === 200 && body.code === 0) {
          resolve();
          return;
        }

        reject(new Error(body.msg || `上传失败(${res.statusCode})`));
      },
      fail: reject
    });
  });
}

/** 催办投诉 */
export function remindComplaint(id: number) {
  return request<number>({ url: `/app/complaint/${id}/remind`, method: 'POST' });
}
